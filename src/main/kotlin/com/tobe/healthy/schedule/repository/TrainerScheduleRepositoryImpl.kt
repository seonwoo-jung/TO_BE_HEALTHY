package com.tobe.healthy.schedule.repository

import com.querydsl.core.types.Projections.constructor
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.DatePath
import com.querydsl.core.types.dsl.Expressions.stringTemplate
import com.querydsl.jpa.impl.JPAQueryFactory
import com.tobe.healthy.lessonhistory.domain.dto.`in`.UnwrittenLessonHistorySearchCond
import com.tobe.healthy.lessonhistory.domain.entity.QLessonHistory.lessonHistory
import com.tobe.healthy.lessonhistory.domain.entity.WritingStatus
import com.tobe.healthy.lessonhistory.domain.entity.WritingStatus.UNWRITTEN
import com.tobe.healthy.lessonhistory.domain.entity.WritingStatus.WRITTEN
import com.tobe.healthy.member.domain.entity.AlarmStatus.ENABLED
import com.tobe.healthy.member.domain.entity.QMember
import com.tobe.healthy.member.domain.entity.QMember.member
import com.tobe.healthy.schedule.domain.dto.`in`.CommandRegisterSchedule
import com.tobe.healthy.schedule.domain.dto.out.FeedbackNotificationToTrainer
import com.tobe.healthy.schedule.domain.dto.out.RetrieveTrainerScheduleByLessonDtResult
import com.tobe.healthy.schedule.domain.dto.out.RetrieveTrainerScheduleByLessonInfoResult
import com.tobe.healthy.schedule.domain.entity.QSchedule.schedule
import com.tobe.healthy.schedule.domain.entity.QScheduleWaiting.scheduleWaiting
import com.tobe.healthy.schedule.domain.entity.ReservationStatus
import com.tobe.healthy.schedule.domain.entity.ReservationStatus.COMPLETED
import com.tobe.healthy.schedule.domain.entity.ReservationStatus.DISABLED
import com.tobe.healthy.schedule.domain.entity.Schedule
import com.tobe.healthy.schedule.domain.entity.TrainerScheduleInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import org.springframework.util.ObjectUtils
import org.springframework.util.StringUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@Repository
class TrainerScheduleRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : TrainerScheduleRepositoryCustom {

    override fun findAllSchedule(
        lessonDt: String?,
        lessonStartDt: LocalDate?,
        lessonEndDt: LocalDate?,
        trainerId: Long
    ): List<Schedule> {
        return queryFactory
            .select(schedule)
            .from(schedule)
            .leftJoin(schedule.trainer, QMember("trainer")).fetchJoin()
            .leftJoin(schedule.applicant, QMember("applicant")).fetchJoin()
            .leftJoin(schedule.scheduleWaiting, scheduleWaiting).fetchJoin()
            .where(
                lessonDtMonthEq(lessonDt),
                lessonDtBetween(
                    lessonStartDt,
                    lessonEndDt
                ),
                trainerIdEq(trainerId)
            )
            .orderBy(schedule.lessonDt.asc(), schedule.lessonStartTime.asc())
            .fetch()
    }

    override fun findOneTrainerTodaySchedule(
        lessonDt: String?,
        trainerId: Long
    ): RetrieveTrainerScheduleByLessonDtResult? {
        val results = queryFactory
            .select(schedule)
            .from(schedule)
            .leftJoin(schedule.trainer, QMember("trainer")).fetchJoin()
            .leftJoin(schedule.applicant, QMember("applicant")).fetchJoin()
            .where(
                lessonDtEq(lessonDt),
                trainerIdEq(trainerId),
                reservationStatusEq(COMPLETED)
            )
            .orderBy(schedule.lessonDt.asc(), schedule.lessonStartTime.asc())
            .fetch()

        val scheduleCount = queryFactory
            .select(schedule.count())
            .from(schedule)
            .where(
                lessonDtEq(lessonDt),
                trainerIdEq(trainerId),
                reservationStatusEq(COMPLETED)
            )
            .fetchOne()

        val response = RetrieveTrainerScheduleByLessonInfoResult.from(results)

        response.let {
            val trainerTodaySchedule = RetrieveTrainerScheduleByLessonDtResult(
                trainerName = response.trainerName,
                scheduleTotalCount = scheduleCount!!,
            )

            response.schedule?.forEach { (key, value) ->
                value.filter {
                    it.lessonStartTime?.isAfter(LocalTime.now()) == true
                }.forEach {
                    trainerTodaySchedule.schedule.add(it)
                }
            }
            
            return trainerTodaySchedule
        }
    }

    override fun validateDuplicateSchedule(
        trainerScheduleInfo: TrainerScheduleInfo,
        request: CommandRegisterSchedule,
        trainerId: Long
    ): Boolean {
        val count = queryFactory
            .select(schedule.count())
            .from(schedule)
            .where(
                lessonDtBetween(request.lessonStartDt, request.lessonEndDt),
                trainerIdEq(trainerId),
                notDayOfWeek(schedule.lessonDt, trainerScheduleInfo.trainerScheduleClosedDays.map { it.closedDays }),
                schedule.lessonStartTime.between(
                    trainerScheduleInfo.lessonStartTime,
                    trainerScheduleInfo.lessonEndTime
                ),
                schedule.lessonEndTime.between(trainerScheduleInfo.lessonStartTime, trainerScheduleInfo.lessonEndTime)
            )
            .fetchOne() ?: 0L
        return count > 0
    }

    private fun notDayOfWeek(dateTimePath: DatePath<LocalDate>, dayOfWeek: List<DayOfWeek>): BooleanExpression {
        return dateTimePath.dayOfWeek().notIn(dayOfWeek.map { it.value })
    }

    override fun findAvailableWaitingId(scheduleId: Long): Optional<Schedule> {
        val result = queryFactory
            .select(schedule)
            .from(schedule)
            .leftJoin(schedule.scheduleWaiting, scheduleWaiting)
            .where(
                scheduleIdEq(scheduleId),
                reservationStatusEq(COMPLETED),
                applicantIsNotNull()
            )
            .fetchOne()
        return Optional.ofNullable(result)
    }

    override fun findAllSchedule(
        scheduleIds: List<Long>,
        reservationStatus: List<ReservationStatus>,
        trainerId: Long
    ): List<Schedule> {
        return queryFactory
            .select(schedule)
            .from(schedule)
            .where(
                scheduleIdIn(scheduleIds),
                trainerIdEq(trainerId),
                reservationStatusIn(reservationStatus)
            )
            .fetch()
    }

    override fun findAllSchedule(scheduleId: Long, reservationStatus: ReservationStatus, trainerId: Long): Schedule? {
        return queryFactory
            .select(schedule)
            .from(schedule)
            .where(
                scheduleIdEq(scheduleId),
                trainerIdEq(trainerId),
                reservationStatusEq(reservationStatus)
            )
            .fetchOne()
    }

    override fun findAllDisabledSchedule(lessonStartDt: LocalDate, lessonEndDt: LocalDate): List<Schedule?> {
        return queryFactory
            .select(schedule)
            .from(schedule)
            .leftJoin(schedule.scheduleWaiting).fetchJoin()
            .where(
                lessonDtBetween(lessonStartDt, lessonEndDt),
                reservationStatusEq(DISABLED)
            )
            .fetch()
    }

    override fun findAllUnwrittenLessonHistory(
        request: UnwrittenLessonHistorySearchCond,
        memberId: Long
    ): List<Schedule> {
        return queryFactory
            .select(schedule)
            .from(schedule)
            .leftJoin(schedule.lessonHistories, lessonHistory).fetchJoin()
            .where(
                trainerIdEq(memberId),
                schedule.applicant.isNotNull,
                reservationStatusEq(COMPLETED),
                lessonDateTimeEq(request.lessonDate),
                applicantIdEq(request.studentId),
                writtenStatusEq(request.writingStatus)
            )
            .orderBy(schedule.lessonDt.asc(), schedule.lessonStartTime.asc())
            .fetch()
    }

    private fun writtenStatusEq(writingStatus: WritingStatus?): BooleanExpression? {
        return when (writingStatus) {
            WRITTEN -> {
                lessonHistory.isNotNull
            }

            UNWRITTEN -> {
                lessonHistory.isNull
            }

            else -> null
        }
    }

    private fun applicantIdEq(studentId: Long?): BooleanExpression? {
        return studentId?.let {
            schedule.applicant.id.eq(studentId)
        }
    }

    override fun findAllSimpleLessonHistoryByMemberId(studentId: Long, trainerId: Long): List<Schedule> {
        return queryFactory
            .select(schedule)
            .from(schedule)
            .leftJoin(schedule.lessonHistories, lessonHistory).fetchJoin()
            .where(
                schedule.applicant.id.eq(studentId),
                trainerIdEq(trainerId),
                schedule.applicant.isNotNull,
                reservationStatusEq(COMPLETED)
            )
            .orderBy(
                schedule.lessonDt.asc(),
                schedule.lessonStartTime.asc()
            )
            .fetch()
    }

    override fun findAllScheduleByStudentId(
        studentId: Long,
        pageable: Pageable,
        trainerId: Long
    ): Page<Schedule> {
        val results = queryFactory
            .select(schedule)
            .from(schedule)
            .leftJoin(schedule.applicant, QMember("applicant")).fetchJoin()
            .where(
                schedule.applicant.id.eq(studentId),
                trainerIdEq(trainerId)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(
                schedule.lessonDt.desc(),
                schedule.lessonStartTime.desc()
            )
            .fetch()

        val totalCount = queryFactory
            .select(schedule.count())
            .from(schedule)
            .leftJoin(schedule.applicant, QMember("applicant"))
            .where(
                schedule.applicant.id.eq(studentId),
                trainerIdEq(trainerId),
            )

        return PageableExecutionUtils.getPage(results, pageable) { totalCount.fetchOne() ?: 0L }
    }

    override fun findAllFeedbackNotificationToTrainer(): List<FeedbackNotificationToTrainer> {
        return queryFactory
            .select(
                constructor(
                    FeedbackNotificationToTrainer::class.java,
                    schedule.trainer.id,
                    schedule.count()
                )
            )
            .from(schedule)
            .innerJoin(schedule.trainer, member).on(member.feedbackAlarmStatus.eq(ENABLED))
            .leftJoin(schedule.lessonHistories, lessonHistory)
            .where(
                lessonHistory.isNull,
                schedule.lessonDt.eq(LocalDate.now()),
                schedule.reservationStatus.eq(COMPLETED)
            )
            .groupBy(schedule.trainer.id)
            .fetch()
    }

    private fun scheduleIdIn(scheduleIds: List<Long>): BooleanExpression? =
        schedule.id.`in`(scheduleIds)

    private fun applicantIsNotNull(): BooleanExpression? =
        schedule.applicant.isNotNull

    private fun reservationStatusEq(reservationStatus: ReservationStatus): BooleanExpression? =
        schedule.reservationStatus.eq(reservationStatus)

    private fun reservationStatusIn(reservationStatus: List<ReservationStatus>): BooleanExpression? =
        schedule.reservationStatus.`in`(reservationStatus)

    private fun scheduleIdEq(scheduleId: Long): BooleanExpression? =
        schedule.id.eq(scheduleId)

    private fun trainerIdEq(trainerId: Long): BooleanExpression? =
        schedule.trainer.id.eq(trainerId)

    private fun lessonDtBetween(lessonStartDt: LocalDate?, lessonEndDt: LocalDate?): BooleanExpression? {
        if (!ObjectUtils.isEmpty(lessonStartDt) && !ObjectUtils.isEmpty(lessonEndDt)) {
            return schedule.lessonDt.between(lessonStartDt, lessonEndDt)
        }
        return null
    }

    private fun lessonDtMonthEq(lessonDt: String?): BooleanExpression? {
        if (!ObjectUtils.isEmpty(lessonDt)) {
            val formattedDate = stringTemplate("DATE_FORMAT({0}, '%Y-%m')", schedule.lessonDt)
            return formattedDate.eq(lessonDt)
        }
        return null
    }

    private fun lessonDtEq(lessonDt: String?): BooleanExpression? {
        if (!StringUtils.hasText(lessonDt)) {
            return null
        }
        val formattedDate = stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", schedule.lessonDt)
        return formattedDate.eq(lessonDt)
    }

    private fun lessonDateTimeEq(lessonDate: String?): BooleanExpression? {
        return lessonDate?.let {
            val formattedDate = stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", schedule.lessonDt)
            return formattedDate.eq(lessonDate)
        }
    }
}

