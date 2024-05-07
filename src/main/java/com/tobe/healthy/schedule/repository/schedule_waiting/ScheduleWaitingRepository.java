package com.tobe.healthy.schedule.repository.schedule_waiting;

import com.tobe.healthy.schedule.domain.entity.ScheduleWaiting;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleWaitingRepository extends JpaRepository<ScheduleWaiting, Long>, ScheduleWaitingRepositoryCustom {
	Optional<ScheduleWaiting> findByScheduleIdAndMemberId(Long scheduleId, Long memberId);
	@EntityGraph(attributePaths = {"member"})
	Optional<ScheduleWaiting> findByScheduleId(Long scheduleId);
}
