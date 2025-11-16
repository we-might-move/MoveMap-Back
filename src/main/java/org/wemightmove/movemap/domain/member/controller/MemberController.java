package org.wemightmove.movemap.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wemightmove.movemap.domain.member.dto.response.ReceivedInviteResponse;
import org.wemightmove.movemap.domain.member.dto.response.SentInviteResponse;
import org.wemightmove.movemap.domain.member.service.MemberQueryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "회원", description = "회원 관리 API")
public class MemberController {

    private final MemberQueryService memberQueryService;

    /**
     * FIXME : memberId 쿼리로 받는 것 로그인 구현 완료 되면 수정
     */
    @Operation(summary = "보낸 초대 목록 조회(부모 사용)")
    @GetMapping("/invitations/sent")
    public ResponseEntity<SentInviteResponse> getSentInvitations(
            @RequestParam("memberId") Long memberId
    ) {
        return ResponseEntity.ok(memberQueryService.getSentInviteList(memberId));
    }

    /**
     * FIXME : memberId 쿼리로 받는 것 로그인 구현 완료 되면 수정
     */
    @Operation(summary = "받은 초대 목록 조회(아이 사용)")
    @GetMapping("/invitations/received")
    public ResponseEntity<ReceivedInviteResponse> getReceivedInvitations(
            @RequestParam("memberId") Long memberId
    ) {
        return ResponseEntity.ok(memberQueryService.getReceivedInviteList(memberId));
    }
}