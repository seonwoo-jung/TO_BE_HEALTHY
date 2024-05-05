package com.tobe.healthy.schedule.application

import com.tobe.healthy.config.error.CustomException
import com.tobe.healthy.config.error.ErrorCode.START_DATE_AFTER_END_DATE
import com.tobe.healthy.member.domain.entity.Member
import com.tobe.healthy.member.repository.MemberRepository
import com.tobe.healthy.schedule.domain.dto.out.ScheduleCommandResult
import com.tobe.healthy.schedule.domain.entity.LessonTime.ONE_HOUR
import com.tobe.healthy.schedule.domain.entity.ReservationStatus.AVAILABLE
import com.tobe.healthy.schedule.domain.entity.Schedule
import com.tobe.healthy.schedule.entity.`in`.RegisterScheduleRequest
import com.tobe.healthy.schedule.entity.`in`.ScheduleSearchCond
import com.tobe.healthy.schedule.repository.trainer.TrainerScheduleRepository
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate.of
import java.time.LocalTime

@SpringBootTest
@Transactional
class TrainerScheduleServiceTest(
    private val trainerScheduleService: TrainerScheduleService,
    private val trainerScheduleRepository: TrainerScheduleRepository,
    private val memberRepository: MemberRepository
) : BehaviorSpec({
    val requestSchedule = RegisterScheduleRequest(
        startDt = of(2024, 12, 1),
        endDt = of(2024, 12, 1),
        startTime = LocalTime.of(10, 0),
        endTime = LocalTime.of(13, 0),
        closedDt = mutableListOf(),
        sessionTime = ONE_HOUR
    )

    val searchCondSchedule = ScheduleSearchCond(
        lessonStartDt = of(2024, 12, 1),
        lessonEndDt = of(2024, 12, 1)
    )

    // 특정 상황 설정
    Given("트레이너가 일정을 등록하고") {
        val trainerId = 542L
        trainerScheduleService.registerSchedule(requestSchedule, trainerId)

        // 특정 상황 실행
        When("등록한 일정을 조회 해서") {
            val trainer = findMember(memberRepository, trainerId)
            val schedules = trainerScheduleService.findAllSchedule(searchCondSchedule, trainer)

            // 기대하는 결과 확인
            Then("일정 등록이 잘 되었는지 검증한다") {
                schedules.size shouldBe 3
                schedules.map { it?.lessonDt } shouldContainOnly mutableListOf(of(2024, 12, 1))
            }

            Then("트레이너가 일정을 삭제하고 검증한다") {
                trainerScheduleService.cancelTrainerSchedule(schedules[0]!!.scheduleId!!, trainerId)
                val findSchedule = findSchedule(trainerScheduleRepository, schedules)

                findSchedule.reservationStatus shouldBe AVAILABLE
                findSchedule.applicant shouldBe null
            }
        }
    }

    Given("트레이너가 일정을 등록한 뒤에") {
        val trainerId = 542L
        When("유효하지 않은 일정 정보를 입력하면") {
            Then("예외를 발생시킨다") {
                val copyRequest = requestSchedule.copy(startDt = of(2024, 12, 2))
                val exception = shouldThrow<CustomException> {
                    trainerScheduleService.registerSchedule(copyRequest, trainerId)
                }
                exception.message shouldBe START_DATE_AFTER_END_DATE.message
            }
        }
    }
})

private fun findSchedule(trainerScheduleRepository: TrainerScheduleRepository, schedules: List<ScheduleCommandResult?>): Schedule {
    return trainerScheduleRepository.findByIdOrNull(schedules[0]!!.scheduleId!!) ?: fail("일정을 찾을 수 없음")
}

private fun findMember(memberRepository: MemberRepository, trainerId: Long): Member {
    return memberRepository.findByIdOrNull(trainerId) ?: fail("회원을 찾을 수 없음")
}
