package org.wemightmove.movemap.domain.member.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wemightmove.movemap.domain.member.service.MemberQueryService;

@RestController
@Tag(name = "Member")
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberQueryService memberQueryService;

    /**
     * FIXME : memberId 쿼리로 받는 것 로그인 구현 완료 되면 수정
     */
    @GetMapping("/invitations")
    public ResponseEntity<?> getInviteInfoList(@RequestParam("type") String type, @RequestParam("memberId") Long memberId) {
        return type.equals("sent") ? ResponseEntity.ok(memberQueryService.getSentInviteList(memberId))
                : ResponseEntity.ok(memberQueryService.getReceivedInviteList(memberId));
    }
}
