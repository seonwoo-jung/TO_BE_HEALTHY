package com.tobe.healthy.schedule.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.tobe.healthy.schedule.entity.QTrainerScheduleInfo.trainerScheduleInfo
import com.tobe.healthy.schedule.entity.TrainerScheduleInfo

class TrainerScheduleInfoRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : TrainerScheduleInfoRepositoryCustom {

    override fun findByTrainerId(trainerId: Long): TrainerScheduleInfo? {
        return queryFactory.selectDistinct(trainerScheduleInfo)
            .from(trainerScheduleInfo)
            .leftJoin(trainerScheduleInfo.trainerScheduleClosedDays).fetchJoin()
            .where(trainerScheduleInfo.trainer.id.eq(trainerId))
            .fetchOne()
    }
}
