package com.tobe.healthy.trainer.domain.entity;

import com.tobe.healthy.common.BaseTimeEntity;
import com.tobe.healthy.member.domain.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "trainer_member_mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TrainerMemberMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long mappingId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "trainer_id")
    private Member trainer;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private int lessonCnt;
    private int remainLessonCnt;

    @ColumnDefault("999")
    private int ranking = 999;


    public static TrainerMemberMapping create(Member trainer, Member member, int lessonCnt, int remainLessonCnt) {
        return TrainerMemberMapping.builder()
                .trainer(trainer)
                .member(member)
                .lessonCnt(lessonCnt)
                .remainLessonCnt(remainLessonCnt)
                .build();
    }

    @Builder
    public TrainerMemberMapping(Member trainer, Member member, int lessonCnt, int remainLessonCnt) {
        this.trainer = trainer;
        this.member = member;
        this.lessonCnt = lessonCnt;
        this.remainLessonCnt = remainLessonCnt;
    }

    public void changeRanking(int ranking){
        this.ranking = ranking;
    }
}
