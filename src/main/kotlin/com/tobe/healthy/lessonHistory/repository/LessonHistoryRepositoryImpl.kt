package com.tobe.healthy.lessonHistory.repository

import com.querydsl.core.types.ConstantImpl.create
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions.stringTemplate
import com.querydsl.jpa.impl.JPAQueryFactory
import com.tobe.healthy.config.error.CustomException
import com.tobe.healthy.config.error.ErrorCode.LESSON_HISTORY_NOT_FOUND
import com.tobe.healthy.lessonHistory.domain.dto.out.LessonHistoryResponse
import com.tobe.healthy.lessonHistory.domain.dto.`in`.SearchCondRequest
import com.tobe.healthy.lessonHistory.domain.dto.out.LessonHistoryDetailResponse
import com.tobe.healthy.lessonHistory.domain.entity.FeedbackCheckStatus.READ
import com.tobe.healthy.lessonHistory.domain.entity.LessonHistory
import com.tobe.healthy.lessonHistory.domain.entity.QLessonHistory.lessonHistory
import com.tobe.healthy.member.domain.entity.MemberType
import com.tobe.healthy.member.domain.entity.MemberType.TRAINER
import com.tobe.healthy.trainer.respository.TrainerMemberMappingRepository
import io.micrometer.common.util.StringUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import java.util.stream.Collectors
import java.util.stream.Collectors.toList

@Repository
class LessonHistoryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val trainerMemberMappingRepository: TrainerMemberMappingRepository
) : LessonHistoryRepositoryCustom {

    override fun findAllLessonHistory(request: SearchCondRequest, pageable: Pageable, memberId: Long, memberType: MemberType): Page<LessonHistoryResponse> {
        val entity = queryFactory
            .select(lessonHistory)
            .from(lessonHistory)
            .innerJoin(lessonHistory.trainer).fetchJoin()
            .innerJoin(lessonHistory.student).fetchJoin()
            .innerJoin(lessonHistory.schedule).fetchJoin()
            .where(convertDateFormat(request.searchDate), validateMemberType(memberId, memberType, request.searchMyHistory))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val contents = entity.map {e -> LessonHistoryResponse.from(e) }.toMutableList()

        val totalCount = queryFactory
            .select(lessonHistory.count())
            .from(lessonHistory)
            .innerJoin(lessonHistory.trainer)
            .innerJoin(lessonHistory.student)
            .innerJoin(lessonHistory.schedule)
            .where(convertDateFormat(request.searchDate), validateMemberType(memberId, memberType, request.searchMyHistory))

        return PageableExecutionUtils.getPage(contents, pageable) { totalCount.fetchOne() ?: 0L }
    }

    override fun findOneLessonHistory(lessonHistoryId: Long, memberId: Long, memberType: MemberType): LessonHistoryDetailResponse {
        val results = queryFactory
            .selectDistinct(lessonHistory)
            .from(lessonHistory)
            .leftJoin(lessonHistory.lessonHistoryComment).fetchJoin()
            .innerJoin(lessonHistory.trainer).fetchJoin()
            .innerJoin(lessonHistory.student).fetchJoin()
            .innerJoin(lessonHistory.schedule).fetchJoin()
            .where(lessonHistory.id.eq(lessonHistoryId), validateMemberType(memberId, memberType, null))
            .fetchOne() ?: throw CustomException(LESSON_HISTORY_NOT_FOUND)

        updateFeedbackCheckStatus(results, memberId)
        return LessonHistoryDetailResponse.detailFrom(results)
    }

    override fun findAllLessonHistoryByMemberId(studentId: Long, request: SearchCondRequest, pageable: Pageable): Page<LessonHistoryResponse> {
        val entity = queryFactory
            .select(lessonHistory)
            .from(lessonHistory)
            .innerJoin(lessonHistory.trainer).fetchJoin()
            .innerJoin(lessonHistory.student).fetchJoin()
            .innerJoin(lessonHistory.schedule).fetchJoin()
            .where(convertDateFormat(request.searchDate), lessonHistory.student.id.eq(studentId))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val results = entity.map {LessonHistoryResponse.from(it)}.toMutableList()

        val totalCount = queryFactory
            .select(lessonHistory.count())
            .from(lessonHistory)
            .innerJoin(lessonHistory.trainer)
            .innerJoin(lessonHistory.student)
            .innerJoin(lessonHistory.schedule)
            .where(convertDateFormat(request.searchDate), lessonHistory.student.id.eq(studentId))

        return PageableExecutionUtils.getPage(results, pageable) { totalCount.fetchOne() ?: 0L }
    }

    private fun updateFeedbackCheckStatus(
        results: LessonHistory,
        memberId: Long,
    ) {
        if (results.student.id.equals(memberId)) {
            results.updateFeedbackStatus(READ)
        }
    }

    private fun validateMemberType(memberId: Long, memberType: MemberType, searchMyHistory: String?): BooleanExpression {
        if (memberType == TRAINER) {
            return lessonHistory.trainer.id.eq(memberId)
        } else {
            val result = trainerMemberMappingRepository.findByMemberId(memberId).orElseThrow { throw CustomException(LESSON_HISTORY_NOT_FOUND) }
            return lessonHistory.trainer.id.eq(result.trainer.id)
        }
    }

    private fun convertDateFormat(searchDate: String?): BooleanExpression? {
        if (StringUtils.isEmpty(searchDate)) {
            return null
        }
        val stringTemplate = stringTemplate(
            "DATE_FORMAT({0}, {1})",
            lessonHistory.schedule.lessonDt,
            create("%Y%m")
        )
        return stringTemplate.eq(searchDate)
    }
}

