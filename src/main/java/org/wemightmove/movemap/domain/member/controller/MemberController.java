package org.wemightmove.movemap.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.member.dto.request.*;
import org.wemightmove.movemap.domain.member.dto.response.*;
import org.wemightmove.movemap.domain.member.service.MemberCommandService;
import org.wemightmove.movemap.domain.member.service.MemberFacilityQueryService;
import org.wemightmove.movemap.domain.member.service.MemberProgramQueryService;
import org.wemightmove.movemap.domain.member.service.MemberQueryService;
import org.wemightmove.movemap.global.security.CustomUserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "Member")
public class MemberController {

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;
    private final MemberFacilityQueryService memberFacilityQueryService;
    private final MemberProgramQueryService memberProgramQueryService;

    @Operation(summary = "보낸 초대 목록 조회(부모 사용)")
    @GetMapping("/invitations/sent")
    public ResponseEntity<SentInviteResponse> getSentInvitations(
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        return ResponseEntity.ok(memberQueryService.getSentInviteList(member.getId()));
    }

    @Operation(summary = "받은 초대 목록 조회(아이 사용)")
    @GetMapping("/invitations/received")
    public ResponseEntity<ReceivedInviteResponse> getReceivedInvitations(
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        return ResponseEntity.ok(memberQueryService.getReceivedInviteList(member.getId()));
    }

    @Operation(summary = "초대 보내기(부모 사용)")
    @PostMapping("/invitations")
    public ResponseEntity<SendInviteResponse> sendInvitation(
            @AuthenticationPrincipal CustomUserDetails member,
            @RequestBody SendInvitedRequest sendInvitedRequest
            ) {
        return ResponseEntity.ok(memberCommandService.sendInvite(member.getId(), sendInvitedRequest.childUuid()));
    }

    @Operation(summary = "초대 수락")
    @PatchMapping("/invitations/accept")
    public ResponseEntity<AcceptInvitationResponse> acceptInvite(
            @AuthenticationPrincipal CustomUserDetails member,
            @RequestBody AcceptInvitationRequest acceptInvitationRequest) {
        return ResponseEntity.ok(memberCommandService.acceptInvite(member.getId(), acceptInvitationRequest));
    }

    @Operation(summary = "초대 거절")
    @PatchMapping("/invitations/reject")
    public ResponseEntity<Void> rejectInvite(
            @AuthenticationPrincipal CustomUserDetails member,
            @RequestBody RejectInvitationRequest rejectInvitationRequest) {
        memberCommandService.rejectInvite(member.getId(), rejectInvitationRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "찜한 시설 리스트 조회",
            description = """
            - latitude, longitude : 시설과의 거리를 계산하기 위한 사용자 현재 위치(선택)
            - cursor : 이전에 받은 마지막 시설 아이디(초기에는 null)
            - size : 시설 데이터를 몇 개씩 받을 것인지(기본 20)
            """)
    @GetMapping("/bookmarks/facilities")
    public ResponseEntity<FavoriteFacilityPageResponse> getFavoriteFacilityList(
            @AuthenticationPrincipal CustomUserDetails member,
            @RequestParam(value = "latitude", required = false) BigDecimal latitude,
            @RequestParam(value = "longitude", required = false) BigDecimal longitude,
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", required = false, defaultValue = "20") Integer size) {


        FavoriteFacilityPageResponse response = memberFacilityQueryService.getFavoriteList(member.getId(), latitude, longitude, cursor, size);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "찜한 프로그램 리스트 조회",
            description = """
            - latitude, longitude : 프로그램 운영 시설과의 거리를 계산하기 위한 사용자 현재 위치(선택)
            - cursor : 이전에 받은 마지막 프로그램 아이디(초기에는 null)
            - size : 프로그램 데이터를 몇 개씩 받을 것인지(기본 20)
            """)
    @GetMapping("/bookmarks/programs")
    public ResponseEntity<FavoriteProgramListResponse> getFavoriteProgramList(
            @AuthenticationPrincipal CustomUserDetails member,
            @RequestParam(value = "latitude", required = false) BigDecimal latitude,
            @RequestParam(value = "longitude", required = false) BigDecimal longitude,
            @RequestParam(name = "cursor", required = false) Long cursor,
            @RequestParam(name = "size", required = false, defaultValue = "20") Integer size) {

        FavoriteProgramListResponse response = memberProgramQueryService.getFavoritePrograms(member.getId(), latitude, longitude, cursor, size);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping
    public ResponseEntity<MemberWithdrawResponse> updateMember(
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        return ResponseEntity.ok(memberCommandService.withdrawMember(member.getId()));
    }

    @Operation(summary = "회원 정보 수정",
            description = """
            - nickname : 2자 이상 20자 이하. 한글, 영문, 숫자만 가능(공백, 특수문자 불가)
            - school : 100자 이하
            - city, district : 둘 다 입력되어야 함
            - sex : WOMAN, MAN
            """)
    @PatchMapping
    public ResponseEntity<MemberInfoResponse> updateMember(
            @AuthenticationPrincipal CustomUserDetails member,
            @ModelAttribute UpdateMemberRequest updateMemberRequest) {
        return ResponseEntity.ok(
                memberCommandService.updateMember(member.getId(), updateMemberRequest)
        );
    }

    @Operation(summary = "회원 정보 조회")
    @GetMapping
    public ResponseEntity<MemberInfoResponse> getMemberInfo(
            @AuthenticationPrincipal CustomUserDetails member) {
        return ResponseEntity.ok(memberQueryService.getMemberInfo(member.getId()));
    }

    @Operation(summary = "자식 리스트 조회")
    @GetMapping("/children")
    public ResponseEntity<ChildListResponse> getChildList() {
        return ResponseEntity.ok(memberQueryService.getChildList());
    }

    @Operation(summary = "개인 점수 조회", description = "사용자의 운동 기록을 기반으로 계산한 점수를 조회합니다.")
    @GetMapping("/score")
    public ResponseEntity<MemberScoreResponse> getMemberScore(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(memberQueryService.getMemberScore(date));
    }
}