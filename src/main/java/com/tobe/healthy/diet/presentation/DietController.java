package com.tobe.healthy.diet.presentation;

import com.tobe.healthy.common.ResponseHandler;
import com.tobe.healthy.config.security.CustomMemberDetails;
import com.tobe.healthy.diet.application.DietService;
import com.tobe.healthy.diet.domain.dto.DietDto;
import com.tobe.healthy.diet.domain.dto.in.DietAddCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diet/v1")
@Tag(name = "식단 API", description = "식단 API")
@Slf4j
public class DietController {

    private final DietService dietService;

    @Operation(summary = "식단기록 등록", responses = {
            @ApiResponse(responseCode = "400", description = "잘못된 요청 입력"),
            @ApiResponse(responseCode = "200", description = "식단기록 내용을 반환한다.")
    })
    @PostMapping
    public ResponseHandler<DietDto> addDiet(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
                                                      @Valid DietAddCommand command) {
        return ResponseHandler.<DietDto>builder()
                .data(dietService.addDiet(customMemberDetails.getMember(), command))
                .message("식단기록이 등록되었습니다.")
                .build();
    }

    @Operation(summary = "식단기록 상세 조회", responses = {
            @ApiResponse(responseCode = "400", description = "잘못된 요청 입력"),
            @ApiResponse(responseCode = "200", description = "식단기록 내용을 반환한다.")
    })
    @GetMapping("/{dietId}")
    public ResponseHandler<DietDto> getDietDetail(@Parameter(description = "식단기록 ID") @PathVariable("dietId") Long dietId) {
        return ResponseHandler.<DietDto>builder()
                .data(dietService.getDietDetail(dietId))
                .message("식단기록이 등록되었습니다.")
                .build();
    }

    @Operation(summary = "식단기록 좋아요", responses = {
            @ApiResponse(responseCode = "400", description = "잘못된 요청 입력"),
            @ApiResponse(responseCode = "200", description = "좋아요 완료.")
    })
    @PostMapping("/{dietId}/like")
    public ResponseHandler<Void> likeDiet(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
                                                    @Parameter(description = "식단기록 ID") @PathVariable("dietId") Long dietId) {
        dietService.likeDiet(customMemberDetails.getMember(), dietId);
        return ResponseHandler.<Void>builder()
                .message("식단기록 좋아요에 성공하였습니다.")
                .build();
    }

    @Operation(summary = "식단기록 좋아요 취소", responses = {
            @ApiResponse(responseCode = "400", description = "잘못된 요청 입력"),
            @ApiResponse(responseCode = "200", description = "좋아요 취소 완료.")
    })
    @DeleteMapping("/{dietId}/like")
    public ResponseHandler<Void> deleteLikeDiet(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
                                                          @Parameter(description = "식단기록 ID") @PathVariable("dietId") Long dietId) {
        dietService.deleteLikeDiet(customMemberDetails.getMember(), dietId);
        return ResponseHandler.<Void>builder()
                .message("식단기록 좋아요가 취소되었습니다.")
                .build();
    }

    @Operation(summary = "식단기록 삭제", responses = {
            @ApiResponse(responseCode = "200", description = "식단기록 삭제 완료.")
    })
    @DeleteMapping("/{dietId}")
    public ResponseHandler<Void> deleteDiet(@AuthenticationPrincipal CustomMemberDetails customMemberDetails,
                                            @Parameter(description = "식단기록 ID") @PathVariable("dietId") Long dietId) {
        dietService.deleteDiet(customMemberDetails.getMember(), dietId);
        return ResponseHandler.<Void>builder()
                .message("식단기록이 삭제되었습니다.")
                .build();
    }

}