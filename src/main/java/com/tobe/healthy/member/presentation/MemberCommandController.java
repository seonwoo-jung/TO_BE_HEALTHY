package com.tobe.healthy.member.presentation;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import com.tobe.healthy.common.ResponseHandler;
import com.tobe.healthy.config.security.CustomMemberDetails;
import com.tobe.healthy.member.application.MemberCommandService;
import com.tobe.healthy.member.domain.dto.in.CommandAssignNickname;
import com.tobe.healthy.member.domain.dto.in.CommandChangeEmail;
import com.tobe.healthy.member.domain.dto.in.CommandChangeMemberPassword;
import com.tobe.healthy.member.domain.dto.in.CommandChangeName;
import com.tobe.healthy.member.domain.dto.in.CommandUpdateMemo;
import com.tobe.healthy.member.domain.dto.out.CommandAssignNicknameResult;
import com.tobe.healthy.member.domain.dto.out.CommandChangeNameResult;
import com.tobe.healthy.member.domain.dto.out.DeleteMemberProfileResult;
import com.tobe.healthy.member.domain.dto.out.MemberChangeAlarmResult;
import com.tobe.healthy.member.domain.dto.out.RegisterMemberProfileResult;
import com.tobe.healthy.member.domain.entity.AlarmStatus;
import com.tobe.healthy.member.domain.entity.AlarmType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/v1")
@Slf4j
@Valid
@Tag(name = "02. 회원 API", description = "인증이 있어야만 접근 가능한 회원 API")
public class MemberCommandController {

	private final MemberCommandService memberCommandService;

	@Operation(summary = "로그아웃", description = "로그아웃시 refreshToken, fcmToken을 삭제한다.")
	@PostMapping("/logout")
	public void logout(@AuthenticationPrincipal CustomMemberDetails member) {
		memberCommandService.logout(member.getMemberId());
	}

	@Operation(summary = "회원 탈퇴한다.", description = "로그인한 계정의 현재 비밀번호와 일치하다면 회원탈퇴를 시킨다.",
		responses = {
			@ApiResponse(responseCode = "404", description = "등록된 회원이 아닙니다."),
			@ApiResponse(responseCode = "400", description = "확인 비밀번호가 일치하지 않습니다."),
			@ApiResponse(responseCode = "200", description = "회원 탈퇴 되었습니다.")
	})
	@PostMapping("/delete")
	public ResponseHandler<String> deleteMember(@AuthenticationPrincipal CustomMemberDetails customMemberDetails) {
		return ResponseHandler.<String>builder()
				.data(memberCommandService.deleteMember(customMemberDetails.getMember()))
				.message("회원 탈퇴 되었습니다.")
				.build();
	}

	@Operation(summary = "회원이 비밀번호를 변경한다.",
		responses = {
			@ApiResponse(responseCode = "404", description = "등록된 회원이 아닙니다."),
			@ApiResponse(responseCode = "400", description = "확인 비밀번호가 일치하지 않습니다."),
			@ApiResponse(responseCode = "200", description = "비밀번호 변경이 완료 되었습니다.")
	})
	@PatchMapping("/password")
	public ResponseHandler<Boolean> changePassword(@RequestBody @Valid CommandChangeMemberPassword request,
												   @AuthenticationPrincipal CustomMemberDetails member) {
		return ResponseHandler.<Boolean>builder()
				.data(memberCommandService.changePassword(request, member.getMemberId()))
				.message("비밀번호 변경이 완료되었습니다.")
				.build();
	}

	@Operation(summary = "프로필 사진을 등록한다.", responses = {
			@ApiResponse(responseCode = "404", description = "등록된 회원이 아닙니다."),
			@ApiResponse(responseCode = "500", description = "파일 업로드중 에러가 발생하였습니다."),
			@ApiResponse(responseCode = "200", description = "프로필 사진이 등록되었습니다.")
	})
	@PutMapping(value = "/profile", consumes = MULTIPART_FORM_DATA_VALUE)
	public ResponseHandler<RegisterMemberProfileResult> changeProfile(@RequestParam MultipartFile file,
																	  @AuthenticationPrincipal CustomMemberDetails member) {
		return ResponseHandler.<RegisterMemberProfileResult>builder()
				.data(memberCommandService.registerProfile(file, member.getMemberId()))
				.message("프로필 사진이 등록되었습니다.")
				.build();
	}

	@Operation(summary = "프로필 사진을 삭제한다.", responses = {
			@ApiResponse(responseCode = "404", description = "등록된 회원이 아닙니다."),
			@ApiResponse(responseCode = "200", description = "프로필 사진이 삭제되었습니다.")
	})
	@DeleteMapping("/profile")
	public ResponseHandler<DeleteMemberProfileResult> changeProfile(@AuthenticationPrincipal CustomMemberDetails member) {
		return ResponseHandler.<DeleteMemberProfileResult>builder()
				.data(memberCommandService.deleteProfile(member.getMemberId()))
				.message("프로필 사진이 삭제되었습니다.")
				.build();
	}

	@Operation(summary = "회원이 이름을 변경한다.", responses = {
			@ApiResponse(responseCode = "404", description = "등록된 회원이 아닙니다."),
			@ApiResponse(responseCode = "200", description = "이름이 변경되었습니다.")
	})
	@PatchMapping("/name")
	public ResponseHandler<CommandChangeNameResult> changeName(
			@RequestBody @Valid CommandChangeName request,
			@AuthenticationPrincipal CustomMemberDetails member
	) {

		return ResponseHandler.<CommandChangeNameResult>builder()
				.data(memberCommandService.changeName(request, member.getMemberId()))
				.message("이름이 변경되었습니다.")
				.build();
	}

	@Operation(summary = "알림 상태를 변경한다.", responses = {
			@ApiResponse(responseCode = "404", description = "등록된 회원이 아닙니다."),
			@ApiResponse(responseCode = "200", description = "알림 상태가 변경되었습니다.")
	})
	@PatchMapping("/alarm/{type}/{status}")
	public ResponseHandler<MemberChangeAlarmResult> changeAlarm(@PathVariable AlarmType type,
																@PathVariable AlarmStatus status,
																@AuthenticationPrincipal CustomMemberDetails member) {
		return ResponseHandler.<MemberChangeAlarmResult>builder()
				.data(memberCommandService.changeAlarm(type, status, member.getMemberId()))
				.message("알림 상태가 변경되었습니다.")
				.build();
	}

	@Operation(summary = "트레이너가 학생의 메모를 수정한다.", responses = {
			@ApiResponse(responseCode = "404", description = "등록된 학생이 아닙니다."),
			@ApiResponse(responseCode = "200", description = "메모가 수정되었습니다.")
	})
	@PutMapping("/{memberId}/memo")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ResponseHandler<Void> updateMemo(@AuthenticationPrincipal CustomMemberDetails trainer,
										    @PathVariable Long memberId,
										    @RequestBody CommandUpdateMemo command) {
		memberCommandService.updateMemo(trainer.getMemberId(), memberId, command);
		return ResponseHandler.<Void>builder()
				.message("메모가 수정되었습니다.")
				.build();
	}

	@Operation(summary = "트레이너가 학생의 닉네임을 지정한다.", responses = {
			@ApiResponse(responseCode = "404", description = "등록된 학생이 아닙니다."),
			@ApiResponse(responseCode = "200", description = "닉네임이 설정되었습니다.")
	})
	@PostMapping("/nickname/{studentId}")
	@PreAuthorize("hasAuthority('ROLE_TRAINER')")
	public ResponseHandler<CommandAssignNicknameResult> assignNickname(@PathVariable Long studentId,
																	   @RequestBody @Valid CommandAssignNickname request) {
		return ResponseHandler.<CommandAssignNicknameResult>builder()
				.data(memberCommandService.assignNickname(request, studentId))
				.message("닉네임을 지정하였습니다.")
				.build();
	}

	@Operation(summary = "회원이 이메일을 변경한다.", responses = {
			@ApiResponse(responseCode = "404", description = "등록된 학생이 아닙니다."),
			@ApiResponse(responseCode = "200", description = "이메일이 변경되었습니다.")
	})
	@PatchMapping("/email")
	public ResponseHandler<Boolean> changeEmail(@RequestBody @Valid CommandChangeEmail request,
												@AuthenticationPrincipal CustomMemberDetails member) {
		return ResponseHandler.<Boolean>builder()
				.data(memberCommandService.changeEmail(request, member.getMemberId()))
				.message("이메일이 변경되었습니다.")
				.build();
	}

	@Operation(summary = "스케줄 공지 보기 여부를 변경한다.", description = "스케줄 공지 보기 여부를 변경한다.",
			responses = {
					@ApiResponse(responseCode = "404", description = "등록된 회원이 아닙니다."),
					@ApiResponse(responseCode = "200", description = "스케줄 공지 보기 여부가 변경되었습니다.")
			})
	@PatchMapping("/schedule-notice")
	public ResponseHandler<Boolean> changeScheduleNotice(@RequestParam AlarmStatus alarmStatus,
														 @AuthenticationPrincipal CustomMemberDetails member) {
		return ResponseHandler.<Boolean>builder()
				.data(memberCommandService.changeScheduleNotice(alarmStatus, member.getMemberId()))
				.message("스케줄 공지 보기 여부가 변경되었습니다.")
				.build();
	}

	@Operation(summary = "식단 공지 보기 여부를 변경한다.", description = "식단 공지 보기 여부를 변경한다.",
			responses = {
					@ApiResponse(responseCode = "404", description = "등록된 회원이 아닙니다."),
					@ApiResponse(responseCode = "200", description = "식단 공지 보기 여부가 변경되었습니다.")
			})
	@PatchMapping("/diet-notice")
	public ResponseHandler<Boolean> changeDietNotice(@RequestParam AlarmStatus alarmStatus,
													 @AuthenticationPrincipal CustomMemberDetails member) {
		return ResponseHandler.<Boolean>builder()
				.data(memberCommandService.changeDietNotice(alarmStatus, member.getMemberId()))
				.message("식단 공지 보기 여부가 변경되었습니다.")
				.build();
	}
}