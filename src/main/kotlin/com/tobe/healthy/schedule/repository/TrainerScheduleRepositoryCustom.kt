package com.tobe.healthy.schedule.repository

import com.tobe.healthy.lessonhistory.domain.dto.`in`.UnwrittenLessonHistorySearchCond
import com.tobe.healthy.schedule.domain.dto.`in`.CommandRegisterSchedule
import com.tobe.healthy.schedule.domain.dto.out.FeedbackNotificationToTrainer
import com.tobe.healthy.schedule.domain.dto.out.RetrieveTrainerScheduleByLessonDtResult
import com.tobe.healthy.schedule.domain.entity.ReservationStatus
import com.tobe.healthy.schedule.domain.entity.Schedule
import com.tobe.healthy.schedule.domain.entity.TrainerScheduleInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.*

interface TrainerScheduleRepositoryCustom {
    fun findOneTrainerTodaySchedule(lessonDt: String?, trainerId: Long): RetrieveTrainerScheduleByLessonDtResult?
    fun validateDuplicateSchedule(trainerScheduleInfo: TrainerScheduleInfo, request: CommandRegisterSchedule, trainerId: Long): Boolean
    fun findAvailableWaitingId(scheduleId: Long): Optional<Schedule>
    fun findAllSchedule(lessonDt: String?, lessonStartDt: LocalDate?, lessonEndDt: LocalDate?, trainerId: Long): List<Schedule>
    fun findAllSchedule(scheduleIds: List<Long>, reservationStatus: List<ReservationStatus>, trainerId: Long): List<Schedule>
    fun findAllSchedule(scheduleId: Long, reservationStatus: ReservationStatus, trainerId: Long): Schedule?
    fun findAllDisabledSchedule(lessonStartDt: LocalDate, lessonEndDt: LocalDate): List<Schedule?>
    fun findAllUnwrittenLessonHistory(request: UnwrittenLessonHistorySearchCond, memberId: Long): List<Schedule>
    fun findAllSimpleLessonHistoryByMemberId(studentId: Long, trainerId: Long): List<Schedule>
    fun findAllScheduleByStudentId(studentId: Long, pageable: Pageable, trainerId: Long): Page<Schedule>
    fun findAllFeedbackNotificationToTrainer(): List<FeedbackNotificationToTrainer>
}
