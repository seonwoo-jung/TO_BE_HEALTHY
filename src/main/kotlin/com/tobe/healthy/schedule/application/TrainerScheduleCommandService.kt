package com.tobe.healthy.schedule.application

import com.tobe.healthy.config.error.CustomException
import com.tobe.healthy.config.error.ErrorCode.*
import com.tobe.healthy.member.repository.MemberRepository
import com.tobe.healthy.schedule.domain.dto.`in`.CommandRegisterDefaultLessonTime
import com.tobe.healthy.schedule.domain.dto.`in`.CommandRegisterSchedule
import com.tobe.healthy.schedule.domain.dto.`in`.CommandUpdateScheduleStatus
import com.tobe.healthy.schedule.domain.dto.out.*
import com.tobe.healthy.schedule.domain.entity.ReservationStatus
import com.tobe.healthy.schedule.domain.entity.ReservationStatus.*
import com.tobe.healthy.schedule.domain.entity.Schedule
import com.tobe.healthy.schedule.domain.entity.TrainerScheduleClosedDaysInfo
import com.tobe.healthy.schedule.domain.entity.TrainerScheduleInfo
import com.tobe.healthy.schedule.repository.TrainerScheduleInfoRepository
import com.tobe.healthy.schedule.repository.schedule_waiting.ScheduleWaitingRepository
import com.tobe.healthy.schedule.repository.trainer.TrainerScheduleRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.Duration.between
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

@Service
@Transactional
class TrainerScheduleCommandService(
    private val memberRepository: MemberRepository,
    private val trainerScheduleRepository: TrainerScheduleRepository,
    private val trainerScheduleInfoRepository: TrainerScheduleInfoRepository,
    private val scheduleWaitingRepository: ScheduleWaitingRepository
) {

    fun registerDefaultLessonTime(
        request: CommandRegisterDefaultLessonTime,
        trainerId: Long
    ): CommandRegisterDefaultLessonTimeResult {
        val findTrainer = findMemberById(trainerId)

        trainerScheduleInfoRepository.findByTrainerId(trainerId)?.let {
            it.changeDefaultLessonTime(request)
            it.trainerScheduleClosedDays.clear()
            it.trainerScheduleClosedDays.addAll(
                request.closedDays?.map { dayOfWeek ->
                    TrainerScheduleClosedDaysInfo.registerClosedDay(
                        dayOfWeek,
                        it,
                    )
                }?.toMutableList() ?: mutableListOf(),
            )
        } ?: let {
            val trainerScheduleInfo = TrainerScheduleInfo.registerDefaultLessonTime(request, findTrainer)
            val trainerScheduleClosedDaysInfos = mutableListOf<TrainerScheduleClosedDaysInfo>()

            request.closedDays?.forEach { dayOfWeek ->
                trainerScheduleClosedDaysInfos.add(
                    TrainerScheduleClosedDaysInfo.registerClosedDay(
                        dayOfWeek,
                        trainerScheduleInfo,
                    ),
                )
            }

            trainerScheduleInfo.registerTrainerScheduleClosedDays(trainerScheduleClosedDaysInfos)
            trainerScheduleInfoRepository.save(trainerScheduleInfo)
        }

        return CommandRegisterDefaultLessonTimeResult.from(request)
    }

    fun registerSchedule(
        request: CommandRegisterSchedule,
        trainerId: Long
    ): CommandRegisterScheduleResult {
        val trainer = findMemberById(trainerId)

        val findTrainerSchedule = trainerScheduleInfoRepository.findByTrainerId(trainerId)
            ?: throw CustomException(TRAINER_SCHEDULE_NOT_FOUND)

        var lessonDt = request.lessonStartDt
        var startTime = findTrainerSchedule.lessonStartTime
        val schedules = mutableListOf<Schedule>()

        while (!lessonDt.isAfter(request.lessonEndDt)) {
            var endTime = startTime.plusMinutes(findTrainerSchedule.lessonTime.description.toLong())

            if (endTime.isAfter(findTrainerSchedule.lessonEndTime)) {
                startTime = findTrainerSchedule.lessonStartTime
                lessonDt = lessonDt.plusDays(ONE_DAY)
                continue
            }

            val isClosedDay = findTrainerSchedule.trainerScheduleClosedDays.any {
                it.closedDays == lessonDt.dayOfWeek
            }

            // 휴무일일 경우
            if (isClosedDay) {
                while (!startTime.isAfter(endTime)) {
                    val schedule = Schedule.registerSchedule(lessonDt, trainer, startTime, endTime, DISABLED)
                    schedules.add(schedule)
                    startTime = endTime
                    endTime = startTime.plusMinutes(findTrainerSchedule.lessonTime.description.toLong())
                }
                startTime = findTrainerSchedule.lessonStartTime
                lessonDt = lessonDt.plusDays(ONE_DAY)
                continue
            }

            // 점심시간일 경우
            if (isStartTimeEqualsLunchStartTime(findTrainerSchedule.lunchStartTime, startTime)) {
                val duration = between(findTrainerSchedule.lunchStartTime, findTrainerSchedule.lunchEndTime)
                val schedule = Schedule.registerSchedule(lessonDt, trainer, findTrainerSchedule.lunchStartTime, findTrainerSchedule.lunchEndTime, DISABLED)
                schedules.add(schedule)
                startTime = startTime.plusMinutes(duration.toMinutes())
                continue
            }

            isScheduleExisting(lessonDt, startTime, endTime, trainerId)

            val schedule = Schedule.registerSchedule(lessonDt, trainer, startTime, endTime, AVAILABLE)
            schedules.add(schedule)

            startTime = endTime
        }

        trainerScheduleRepository.saveAll(schedules)

        return CommandRegisterScheduleResult.from(schedules, findTrainerSchedule)
    }

    fun updateScheduleStatus(
        request: CommandUpdateScheduleStatus,
        status: ReservationStatus,
        memberId: Long
    ): List<CommandScheduleStatusResult> {

        var schedules: List<Schedule>

        when (status) {

            AVAILABLE -> {
                schedules = trainerScheduleRepository.findAllSchedule(request.scheduleIds, DISABLED, memberId)

                if (schedules.isNullOrEmpty()) {
                    throw CustomException(SCHEDULE_NOT_FOUND)
                }
                schedules.forEach {
                    it.updateLessonDtToAvailableDay()
                }
            }

            DISABLED -> {
                schedules = trainerScheduleRepository.findAllSchedule(request.scheduleIds, listOf(AVAILABLE, COMPLETED), memberId)

                if (schedules.isNullOrEmpty()) {
                    throw CustomException(SCHEDULE_NOT_FOUND)
                }

                schedules.forEach {
                    it.updateScheduleToDisabled()
                    if (!it.scheduleWaiting.isNullOrEmpty()) {
                        scheduleWaitingRepository.deleteAll(it.scheduleWaiting!!)
                    }
                }
            }

            else -> {
                throw CustomException(RESERVATION_STATUS_NOT_FOUND)
            }
        }
        return schedules.map { CommandScheduleStatusResult.from(it) }
    }

    fun registerStudentInTrainerSchedule(
        scheduleId: Long,
        studentId: Long,
        trainerId: Long
    ): CommandRegisterScheduleByStudentResult {

        val schedule = trainerScheduleRepository.findAllSchedule(scheduleId, AVAILABLE, trainerId)
            ?: throw CustomException(SCHEDULE_NOT_FOUND)

        val findStudent = memberRepository.findById(studentId)
            .orElseThrow { throw CustomException(MEMBER_NOT_FOUND) }

        schedule.registerSchedule(findStudent)

        return CommandRegisterScheduleByStudentResult.from(schedule, findStudent)
    }

    fun cancelStudentReservation(
        scheduleId: Long,
        trainerId: Long
    ): CommandCancelStudentReservationResult {

        // todo: 2024-05-05 일요일 오후 14:16 등록된 학생이 있는경우 푸시알림등으로 취소되었다는 알림이 필요 - seonwoo_jung
        val entity = trainerScheduleRepository.findAllSchedule(scheduleId, trainerId)
            ?: throw CustomException(SCHEDULE_NOT_FOUND)

        if (!entity.scheduleWaiting.isNullOrEmpty()) {
            entity.cancelMemberSchedule(entity.scheduleWaiting!![0])
            scheduleWaitingRepository.delete(entity.scheduleWaiting!![0])
        } else {
            entity.cancelMemberSchedule()
        }

        return CommandCancelStudentReservationResult.from(entity)
    }

    fun updateReservationStatusToNoShow(
        reservationStatus: ReservationStatus,
        scheduleId: Long,
        trainerId: Long
    ): ScheduleIdInfo {
        val schedule = trainerScheduleRepository.findAllSchedule(scheduleId, reservationStatus, trainerId)
            ?: throw CustomException(SCHEDULE_NOT_FOUND)
        schedule.updateReservationStatusToNoShow(reservationStatus)
        return ScheduleIdInfo.from(schedule)
    }

    private fun isScheduleExisting(
        lessonDt: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        trainerId: Long
    ) {
        val isDuplicateSchedule =
            trainerScheduleRepository.validateRegisterSchedule(
                lessonDt,
                startTime,
                endTime,
                trainerId
            )

        if (isDuplicateSchedule > 0) {
            throw CustomException(SCHEDULE_ALREADY_EXISTS)
        }
    }

    private fun findMemberById(trainerId: Long) = (memberRepository.findByIdOrNull(trainerId)
        ?: throw CustomException(MEMBER_NOT_FOUND))

    fun deleteDisabledSchedule() {
        val today = LocalDate.now()
        val startOfLastWeek = today.with(TemporalAdjusters.previous(MONDAY))
        val endOfLastWeek = startOfLastWeek.with(TemporalAdjusters.nextOrSame(SUNDAY))
        val disabledSchedule = trainerScheduleRepository.findAllDisabledSchedule(startOfLastWeek, endOfLastWeek)
        trainerScheduleRepository.deleteAll(disabledSchedule)
    }

    private fun isStartTimeEqualsLunchStartTime(lunchStartTime: LocalTime?, startTime: LocalTime): Boolean {
        return startTime == lunchStartTime
    }
}