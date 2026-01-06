package org.wemightmove.movemap.domain.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.dto.request.AcceptInvitationRequest;
import org.wemightmove.movemap.domain.member.dto.request.RejectInvitationRequest;
import org.wemightmove.movemap.domain.member.dto.request.UpdateMemberRequest;
import org.wemightmove.movemap.domain.member.dto.response.*;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.ParentChild;
import org.wemightmove.movemap.domain.member.repository.MemberFacilityRepository;
import org.wemightmove.movemap.domain.member.repository.MemberProgramRepository;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.member.repository.ParentChildRepository;
import org.wemightmove.movemap.domain.notification.dto.response.PushMessageResponse;
import org.wemightmove.movemap.domain.notification.repository.NotificationRepository;
import org.wemightmove.movemap.domain.notification.service.push.PushNotificationService;
import org.wemightmove.movemap.global.entity.RegionType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MemberCommandServiceImpl implements MemberCommandService {

    // 상수들
    private static final String INVITE_PREFIX = "invite:";
    private static final String SENT_LIST_PREFIX = "invites:sent:";
    private static final String RECEIVED_LIST_PREFIX = "invites:received:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final ParentChildRepository parentChildRepository;
    private final MemberFacilityRepository memberFacilityRepository;
    private final MemberProgramRepository memberProgramRepository;
    private final NotificationRepository notificationRepository;
    private final RegionTypeRepository regionTypeRepository;

    // 알림 전송 서비스
    private final PushNotificationService pushNotificationService;

    @Value("${invite.expiration-days:3}")
    private long inviteExpirationDays;

    @Override
    @Transactional
    public SendInviteResponse sendInvite(Long parentId, String inviteCode) {
        // 자식과, 부모 조회
        Member child = memberRepository.findByUuid(inviteCode).orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));
        Member parent = memberRepository.findById(parentId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 본인의 초대 코드를 입력했는지 확인
        if (parentId.equals(child.getId())) {
            throw new CustomException(ErrorCode.INVALID_INVITE_MEMBER);
        }

        // 이미 연결된 부모-자식인지 확인
        if (parentChildRepository.existsByParentAndChild(parent, child)) {
            throw new CustomException(ErrorCode.ALREADY_CONNECTED);
        }

        // 이미 초대 받았는지 확인
        String inviteKey = buildInviteKey(parent.getId(), child.getId());
        if (Boolean.TRUE.equals(redisTemplate.hasKey(inviteKey))) {
            throw new CustomException(ErrorCode.ALREADY_SEND_INVITE);
        }

        // 초대 정보 생성
        InviteInfo inviteInfo = InviteInfo.of(parent.getId(), parent.getNickname(), child.getId(), child.getNickname(), LocalDateTime.now());

        try {
            String value = objectMapper.writeValueAsString(inviteInfo);

            // Redis 에 초대 정보 저장
            redisTemplate.opsForValue().set(
                    inviteKey,
                    value,
                    inviteExpirationDays,
                    TimeUnit.DAYS
            );

            // 부모의 보낸 목록에 추가
            String sentListKey = SENT_LIST_PREFIX + parentId;
            redisTemplate.opsForSet().add(sentListKey, child.getId().toString());
            redisTemplate.expire(sentListKey, inviteExpirationDays, TimeUnit.DAYS);

            // 자식의 받은 목록에 추가
            String receivedListKey = RECEIVED_LIST_PREFIX + child.getId();
            redisTemplate.opsForSet().add(receivedListKey, parentId.toString());
            redisTemplate.expire(receivedListKey, inviteExpirationDays, TimeUnit.DAYS);

        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.FAIL_SERIALIZATION);
        }

        // push 알림 전송 로직 추가
        PushMessageResponse message = PushMessageResponse.inviteResponse(
                parent.getNickname(),
                child.getUuid()
        );
        pushNotificationService.sendToMember(child.getId(), message);

        return new SendInviteResponse(child.getId(), child.getNickname());
    }

    @Override
    @Transactional
    public AcceptInvitationResponse acceptInvite(Long childId, AcceptInvitationRequest acceptInvitationRequest) {
        Long parentId = acceptInvitationRequest.parentId();

        // 이미 연결된 관계인지 확인(멱등성 체크)
        Member parent = memberRepository.findById(parentId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Member child = memberRepository.findById(childId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(parentChildRepository.existsByParentAndChild(parent, child)) {
            throw new CustomException(ErrorCode.ALREADY_CONNECTED);
        }

        // Redis 에서 초대 정보 조회
        String inviteKey = buildInviteKey(parentId, childId);
        String inviteJson = redisTemplate.opsForValue().get(inviteKey);

        if(inviteJson == null) {
            throw new CustomException(ErrorCode.INVITE_NOT_FOUND);
        }

        // ParentChild 에 저장
        ParentChild saved = parentChildRepository.save(ParentChild.builder().parent(parent).child(child).build());

        deleteInviteFromRedis(parentId, childId);

        // 초대 수락 알림 전송
        PushMessageResponse pushMessageResponse = PushMessageResponse.invitedAccepted(child.getNickname());
        pushNotificationService.sendToMember(parent.getId(), pushMessageResponse);

        return new AcceptInvitationResponse(saved.getParent().getId(), saved.getParent().getNickname(), saved.getParent().getRole().name());
    }

    @Override
    @Transactional
    public void rejectInvite(Long childId, RejectInvitationRequest rejectInvitationRequest) {
        Long parentId = rejectInvitationRequest.parentId();

        // Redis에서 초대 정보 존재 확인
        String inviteKey = buildInviteKey(parentId, childId);

        if(Boolean.FALSE.equals(redisTemplate.hasKey(inviteKey))) {
            throw new CustomException(ErrorCode.INVITE_NOT_FOUND);
        }

        Member child = memberRepository.findById(childId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // Redis 에서 관련 초대 정보 모두 삭제
        deleteInviteFromRedis(parentId, childId);

        // 초대 거절 알림 전송
        PushMessageResponse pushMessageResponse = PushMessageResponse.inviteRejected(child.getNickname());
        pushNotificationService.sendToMember(parentId, pushMessageResponse);
    }

    @Override
    @Transactional
    public MemberWithdrawResponse withdrawMember(Long memberId) {
        // 1. 회원 조회 및 검증
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(member.isDeleted()) {
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        }

        // 2. 삭제 전 통계 수집
        List<ParentChild> asParentRelations = parentChildRepository.findAllByParent(member);
        List<ParentChild> asChildRelations = parentChildRepository.findAllByChild(member);

        int parentRelationCount = asParentRelations.size();
        int childRelationsCount = asChildRelations.size();

        // 3. 개인 데이터 삭제(ParentChild, MemberFacility, MemberProgram, Notification)
        int deletedRelations = parentChildRepository.deleteAllByMemberId(memberId);
        int deletedFacilities = memberFacilityRepository.deleteAllByMemberId(memberId);
        int deletedPrograms = memberProgramRepository.deleteAllByMemberId(memberId);
        int deletedNotifications = notificationRepository.deleteAllByMemberId(memberId);

        // 회원 소프트 삭제
        member.withdraw();

        return new MemberWithdrawResponse(
                "회원 탈퇴가 완료되었습니다.",
                LocalDateTime.now(),
                new MemberWithdrawResponse.WithdrawStatistics(
                        parentRelationCount,
                        childRelationsCount,
                        deletedFacilities,
                        deletedPrograms,
                        deletedNotifications,
                        parentRelationCount + childRelationsCount
                )
        );
    }

    @Override
    @Transactional
    public MemberInfoResponse updateMember(Long memberId, UpdateMemberRequest updateMemberRequest) {
        // 1. 수정할 필드가 있는지 검증
        if(!updateMemberRequest.hasAnyFieldToUpdate()) {
            throw new CustomException(ErrorCode.MISSING_PARAMETER);
        }

        // 2. 회원 조회
        Member member = memberRepository.findActiveById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 3. 닉네임 중복 체크 (닉네임 변경 시)
        if (updateMemberRequest.nickname() != null && !updateMemberRequest.nickname().equals(member.getNickname())) {
            validateNicknameDuplication(updateMemberRequest.nickname(), memberId);
        }

        // 2. 지역 정보 원자성 검증
        if (!updateMemberRequest.isValidRegionUpdate()) {
            throw new CustomException(ErrorCode.INVALID_REGION_UPDATE);
        }

        String regionCode = null;
        String city = getCity(member.getRegionCode().substring(0, 2));
        String district = getDistrict(member.getRegionCode().substring(0, 4));

        // 4. 지역 코드로 변경
        if (updateMemberRequest.city() != null && updateMemberRequest.district() != null) {
            RegionType regionType = regionTypeRepository.findRegionByNameAndParentName(updateMemberRequest.district(), updateMemberRequest.city())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_FAIR));

            regionCode = regionType.getPrefix();

            city = getCity(regionCode.substring(0, 2));
            district = getDistrict(regionCode.substring(0, 4));
        }

        // 5. 회원 정보 수정
        member.updateProfile(
                updateMemberRequest.nickname(),
                updateMemberRequest.school(),
                regionCode,
                updateMemberRequest.sex(),
                updateMemberRequest.age(),
                updateMemberRequest.height(),
                updateMemberRequest.weight()
        );

        return MemberInfoResponse.from(member, city, district);
    }

    private void validateNicknameDuplication(String nickname, Long excludeMemberId) {
        if (memberRepository.existsByNicknameExcludingMember(nickname, excludeMemberId)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    private String buildInviteKey(Long parentId, Long childId) {
        return INVITE_PREFIX + parentId + ":" + childId;
    }

    // Redis 에서 초대 관련 데이터 모두 삭제
    private void deleteInviteFromRedis(Long parentId, Long childId) {
        // 초대 상세 정보 삭제
        String inviteKey = buildInviteKey(parentId, childId);
        redisTemplate.delete(inviteKey);

        // 부모의 보낸 목록에서 제거
        String sentListKey = SENT_LIST_PREFIX + parentId;
        redisTemplate.opsForSet().remove(sentListKey, childId.toString());

        // 자식의 받은 목록에서 제거
        String receivedListKey = SENT_LIST_PREFIX + childId;
        redisTemplate.opsForSet().remove(receivedListKey, parentId.toString());
    }

    private String getCity(String cityCode) {
        return regionTypeRepository.findParentRegionTypeByPrefix(cityCode).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_CITY)).getName();
    }

    private String getDistrict(String districtCode) {
        return regionTypeRepository.findChildRegionTypeByPrefix(districtCode).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_DISTRICT)).getName();
    }
}
