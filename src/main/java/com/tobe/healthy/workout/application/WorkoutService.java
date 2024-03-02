package com.tobe.healthy.workout.application;

import com.tobe.healthy.common.CommonService;
import com.tobe.healthy.config.error.CustomException;
import com.tobe.healthy.config.error.ErrorCode;
import com.tobe.healthy.file.application.FileService;
import com.tobe.healthy.file.domain.dto.WorkoutHistoryFileDto;
import com.tobe.healthy.member.domain.dto.MemberDto;
import com.tobe.healthy.member.domain.entity.Member;
import com.tobe.healthy.workout.domain.dto.WorkoutHistoryDto;
import com.tobe.healthy.workout.domain.dto.in.WorkoutHistoryAddCommand;
import com.tobe.healthy.workout.domain.dto.out.WorkoutHistoryAddCommandResult;
import com.tobe.healthy.workout.domain.entity.WorkoutHistory;
import com.tobe.healthy.workout.repository.WorkoutHistoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutService {

    private final CommonService commonService;
    private final FileService fileService;
    private final WorkoutHistoryRepository workoutHistoryRepository;


    @Transactional
    public WorkoutHistoryAddCommandResult addWorkoutHistory(Member member,
                                                            @Valid WorkoutHistoryAddCommand command) {

        MemberDto memberDto = MemberDto.from(member);
        WorkoutHistoryDto workoutHistoryDto = WorkoutHistoryDto.create(command.getContent(), memberDto, command.getFiles());
        WorkoutHistory history = WorkoutHistory.create(workoutHistoryDto, member);
        workoutHistoryRepository.save(history);
        history = workoutHistoryRepository.findById(history.getWorkoutHistoryId())
            .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_HISTORY_NOT_FOUND));
        fileService.uploadWorkoutFiles(history, command.getFiles());
        return new WorkoutHistoryAddCommandResult(history.getWorkoutHistoryId(),
                history.getMember().getId(),
                history.getContent());
    }

    public List<WorkoutHistoryDto> getWorkoutHistory(Long memberId, Pageable pageable) {
        Page<WorkoutHistoryDto> histories = workoutHistoryRepository.getWorkoutHistory(memberId, pageable);
        List<Long> ids = histories.stream().map(WorkoutHistoryDto::getWorkoutHistoryId).collect(Collectors.toList());
        List<WorkoutHistoryFileDto> files = workoutHistoryRepository.getWorkoutHistoryFile(ids);
        return histories.stream().map(h -> {
            List<WorkoutHistoryFileDto> thisFiles = files.stream()
                    .filter(f -> f.getWorkoutHistoryId() == h.getWorkoutHistoryId()).collect(Collectors.toList());
            h.setFiles(thisFiles);
            return h;
        }).collect(Collectors.toList());
    }
}
