package com.tobe.healthy.diet.application;

import com.tobe.healthy.config.error.CustomException;
import com.tobe.healthy.diet.domain.dto.DietCommentDto;
import com.tobe.healthy.diet.domain.dto.in.DietCommentAddCommand;
import com.tobe.healthy.diet.domain.entity.Diet;
import com.tobe.healthy.diet.domain.entity.DietComment;
import com.tobe.healthy.diet.repository.DietCommentRepository;
import com.tobe.healthy.diet.repository.DietRepository;
import com.tobe.healthy.member.domain.entity.Member;
import com.tobe.healthy.workout.domain.dto.WorkoutHistoryCommentDto;
import com.tobe.healthy.workout.domain.entity.WorkoutHistory;
import com.tobe.healthy.workout.domain.entity.WorkoutHistoryComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tobe.healthy.config.error.ErrorCode.*;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DietCommentService {

    private final DietCommentRepository commentRepository;
    private final DietRepository dietRepository;


    public List<DietCommentDto> getCommentsByDietId(Long dietId, Pageable pageable) {
        List<DietComment> comments = commentRepository.getCommentsByDietId(dietId, pageable).stream().toList();
        if(comments.isEmpty()) return null;

        List<DietCommentDto> dtos = comments.stream()
                .map(c -> DietCommentDto.create(c, c.getMember().getProfileId())).toList();
        Map<Boolean, List<DietCommentDto>> dtos2 = dtos.stream()
                .collect(Collectors.partitioningBy(c -> c.getParentCommentId() == null));
        List<DietCommentDto> parent = dtos2.get(true);
        List<DietCommentDto> child = dtos2.get(false);

        Map<Long, List<DietCommentDto>> childByGroupList = child.stream()
                .collect(Collectors.groupingBy(DietCommentDto::getParentCommentId, Collectors.toList()));
        return parent.stream().peek(p -> p.setReply(childByGroupList.get(p.getCommentId()))).toList();
    }

    public void addComment(Long dietId, DietCommentAddCommand command, Member member) {
        Diet diet = dietRepository.findById(dietId)
                .orElseThrow(() -> new CustomException(DIET_NOT_FOUND));
        Long depth = 0L, orderNum = 0L;
        Long commentCnt = commentRepository.countByDiet(diet);
        if(command.getParentCommentId() == null){ //댓글
            depth = 0L;
            orderNum = commentCnt;
        }else{ //대댓글
            DietComment parentComment = commentRepository.findByCommentIdAndDelYnFalse(command.getParentCommentId())
                    .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
            depth = parentComment.getDepth()+1;
            orderNum = parentComment.getOrderNum();
        }
        commentRepository.save(DietComment.create(diet, member, command, depth, orderNum));
        diet.updateCommentCnt(++commentCnt);
    }

    public DietCommentDto updateComment(Member member, Long dietId, Long commentId, DietCommentAddCommand command) {
        DietComment comment = commentRepository.findByCommentIdAndMemberIdAndDelYnFalse(commentId, member.getId())
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
        comment.updateContent(command.getContent());
        return DietCommentDto.from(comment);
    }

    public void deleteComment(Member member, Long dietId, Long commentId) {
        DietComment comment = commentRepository.findByCommentIdAndMemberIdAndDelYnFalse(commentId, member.getId())
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
        comment.deleteComment();
    }
}
