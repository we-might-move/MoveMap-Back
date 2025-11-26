package org.wemightmove.movemap.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.member.dto.request.AcceptInvitationRequest;
import org.wemightmove.movemap.domain.member.dto.request.FavoriteProgramRequest;
import org.wemightmove.movemap.domain.member.dto.request.RejectInvitationRequest;
import org.wemightmove.movemap.domain.member.dto.request.SendInvitedRequest;
import org.wemightmove.movemap.domain.member.dto.response.*;
import org.wemightmove.movemap.domain.member.service.MemberCommandService;
import org.wemightmove.movemap.domain.member.service.MemberFacilityQueryService;
import org.wemightmove.movemap.domain.member.service.MemberProgramQueryService;
import org.wemightmove.movemap.domain.member.service.MemberQueryService;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "회원", description = "회원 관리 API")
public class MemberController {

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;
    private final MemberFacilityQueryService memberFacilityQueryService;
    private final MemberProgramQueryService memberProgramQueryService;

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

    /**
     * FIXME : memberId 쿼리로 받는 것 로그인 구현 완료 되면 수정
     */
    @Operation(summary = "초대 보내기(부모 사용)")
    @PostMapping("/invitations")
    public ResponseEntity<SendInviteResponse> sendInvitation(
            @RequestParam("memberId") Long memberId, @RequestBody SendInvitedRequest sendInvitedRequest
            ) {
        return ResponseEntity.ok(memberCommandService.sendInvite(memberId, sendInvitedRequest.childUuid()));
    }

    @Operation(summary = "초대 수락", description = "부모의 초대를 수락하여 관계를 맺습니다")
    @PatchMapping("/invitations/accept")
    public ResponseEntity<AcceptInvitationResponse> acceptInvite(@RequestParam("memberId") Long memberId,
                                                                 @RequestBody AcceptInvitationRequest acceptInvitationRequest) {
        return ResponseEntity.ok(memberCommandService.acceptInvite(memberId, acceptInvitationRequest));
    }

    @Operation(summary = "초대 거절", description = "부모의 초대를 거절합니다")
    @PatchMapping("/invitations/reject")
    public ResponseEntity<Void> rejectInvite(@RequestParam("memberId") Long memberId,
                                               @RequestBody RejectInvitationRequest rejectInvitationRequest) {
        memberCommandService.rejectInvte(memberId, rejectInvitationRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "찜한 시설 리스트 조회", description = "찜한 시설 리스트를 조회합니다")
    @GetMapping("/bookmarks/facilities")
    public ResponseEntity<FavoriteFacilityPageResponse> getFavoriteFacilityList(
            @RequestParam("memberId") Long memberId,
            @RequestParam("latitude") BigDecimal latitude,
            @RequestParam("longitude") BigDecimal longitude,
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", required = false, defaultValue = "20") Integer size) {


        FavoriteFacilityPageResponse response = memberFacilityQueryService.getFavoriteList(memberId, latitude, longitude, cursor, size);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "찜한 프로그램 리스트 조회", description = "찜한 프로그램 리스트를 조회합니다")
    @GetMapping("/bookmarks/programs")
    public ResponseEntity<FavoriteProgramListResponse> getFavoriteProgramList(
            @RequestParam("memberId") Long memberId,
            @RequestParam("latitude") BigDecimal latitude,
            @RequestParam("longitude") BigDecimal longitude,
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", required = false, defaultValue = "20") Integer size) {

        FavoriteProgramListResponse response = memberProgramQueryService.getFavoritePrograms(memberId, latitude, longitude, cursor, size);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다.")
    @DeleteMapping("/members")
    public ResponseEntity<MemberWithdrawResponse> withdrawMember(@RequestParam("memberId") Long memberId) {
        return ResponseEntity.ok(
                memberCommandService.withdrawMember(memberId)
        );
    }

}