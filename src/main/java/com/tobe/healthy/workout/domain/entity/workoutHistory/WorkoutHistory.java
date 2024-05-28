package com.tobe.healthy.workout.domain.entity.workoutHistory;

import com.tobe.healthy.common.BaseTimeEntity;
import com.tobe.healthy.gym.domain.entity.Gym;
import com.tobe.healthy.member.domain.entity.Member;
import com.tobe.healthy.workout.domain.dto.in.HistoryAddCommand;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "workout_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@DynamicUpdate
public class WorkoutHistory extends BaseTimeEntity<WorkoutHistory, Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_history_id")
    private Long workoutHistoryId;

    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @ColumnDefault("false")
    @Builder.Default
    private Boolean delYn = false;

    @ColumnDefault("0")
    @Builder.Default
    private Long likeCnt = 0L;

    @ColumnDefault("0")
    @Builder.Default
    private Long commentCnt = 0L;

    @ColumnDefault("false")
    @Builder.Default
    private Boolean viewMySelf = false;

    @Builder.Default
    @OneToMany(mappedBy = "workoutHistory", cascade = CascadeType.ALL)
    private List<WorkoutHistoryFiles> historyFiles = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "workoutHistory", cascade = CascadeType.ALL)
    private List<WorkoutHistoryComment> historyComments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "workoutHistory", cascade = CascadeType.ALL)
    private List<CompletedExercise> completedExercises = new ArrayList<>();


    public static WorkoutHistory create(HistoryAddCommand command, Member member, Gym gym) {
        return WorkoutHistory.builder()
                .content(command.getContent())
                .viewMySelf(command.isViewMySelf())
                .member(member)
                .gym(gym)
                .build();
    }

    public void updateContent(String content){
        this.content = content;
    }

    public void updateLikeCnt(Long likeCnt){
        this.likeCnt = likeCnt;
    }

    public void updateCommentCnt(Long commentCnt){
        this.commentCnt = commentCnt;
    }

    public void deleteWorkoutHistory() {
        this.delYn = true;
        this.deleteFiles();
        this.deleteComments();
    }

    public void deleteComments() {
        this.historyComments.forEach(WorkoutHistoryComment::deleteComment);
    }

    public void deleteFiles() {
        this.historyFiles.forEach(WorkoutHistoryFiles::deleteWorkoutHistoryFile);
    }

}