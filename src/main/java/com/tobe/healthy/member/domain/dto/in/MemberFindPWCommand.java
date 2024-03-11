package com.tobe.healthy.member.domain.dto.in;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberFindPWCommand {
	@NotEmpty(message = "아이디를 입력해 주세요.")
	private String userId;

	@NotEmpty(message = "실명을 입력해 주세요.")
	private String name;
}