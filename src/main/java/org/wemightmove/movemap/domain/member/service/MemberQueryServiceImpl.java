package org.wemightmove.movemap.domain.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.dto.response.*;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.entity.MemberScore;
import org.wemightmove.movemap.domain.member.entity.ParentChild;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.member.repository.MemberScoreRepository;
import org.wemightmove.movemap.domain.member.repository.ParentChildRepository;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.repository.RegionTypeRepository;
import org.wemightmove.movemap.global.security.CustomUserDetails;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberQueryServiceImpl implements MemberQueryService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final ParentChildRepository parentChildRepository;
    private final RegionTypeRepository regionTypeRepository;
    private final MemberScoreRepository memberScoreRepository;

    private static final String INVITE_PREFIX = "invite:";
    private static final String SENT_LIST_PREFIX = "invites:sent:";
    private static final String RECEIVED_LIST_PREFIX = "invites:received:";

    @Transactional(readOnly = true)
    @Override
    public SentInviteResponse getSentInviteList(Long parentId) {
        String sentListKey = SENT_LIST_PREFIX + parentId;
        Set<String> childIdStrings = redisTemplate.opsForSet().members(sentListKey);

        Member parent = getMember(parentId);

        List<InviteInfo> inviteInfoList = new ArrayList<>();
        // 부모와 연결된 자식 리스트 응답 생성
        List<MemberInfo> memberInfoList = new ArrayList<>(parentChildRepository.findAllByParent(parent).stream()
                .map(m -> MemberInfo.of(m.getChild().getId(), m.getChild().getNickname(), m.getChild().getRole().name())).
                toList());

        if(childIdStrings == null || childIdStrings.isEmpty()) {
            return new SentInviteResponse(memberInfoList, inviteInfoList);
        }

        for (String childIdStr : childIdStrings) {
            try {
                Long childId = Long.parseLong(childIdStr);
                String inviteKey = buildInviteKey(parentId, childId);
                String value = redisTemplate.opsForValue().get(inviteKey);

                // 만료된 경우 Set에서 제거
                if (value == null) {
                    redisTemplate.opsForSet().remove(sentListKey, childIdStr);
                    continue;
                }

                // Record 역직렬화
                InviteInfo inviteInfo = objectMapper.readValue(value, InviteInfo.class);

                inviteInfoList.add(inviteInfo);
            } catch (Exception e) {
                // 흠 파싱 오류 어떻게 해결하지.
                log.warn("error message : {}", e.getMessage());
                throw new CustomException(ErrorCode.FAIL_SERIALIZATION);
            }
        }
        return new SentInviteResponse(memberInfoList, inviteInfoList);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceivedInviteResponse getReceivedInviteList(Long childId) {
        String receivedListKey = RECEIVED_LIST_PREFIX + childId;
        Set<String> parentIdStrings = redisTemplate.opsForSet().members(receivedListKey);

        Member child = getMember(childId);

        List<MemberInfo> parentList = parentChildRepository.findAllByChild(child).stream()
                .map(m -> MemberInfo.of(m.getParent().getId(), m.getParent().getNickname(), m.getParent().getRole().name())).toList();
        List<InviteInfo> inviteInfoList = new ArrayList<>();


        if (parentIdStrings == null || parentIdStrings.isEmpty()) {
            return new ReceivedInviteResponse(child.getUuid(), parentList, inviteInfoList);
        }


        for (String parentIdStr : parentIdStrings) {
            try {
                Long parentId = Long.parseLong(parentIdStr);
                String inviteKey = buildInviteKey(parentId, childId);
                String value = redisTemplate.opsForValue().get(inviteKey);

                if(value == null) {
                    redisTemplate.opsForSet().remove(receivedListKey, parentIdStr);
                    continue;
                }

                InviteInfo inviteInfo = objectMapper.readValue(value, InviteInfo.class);

                // Record 생성
                inviteInfoList.add(inviteInfo);

            }
            catch (Exception e) {
                log.warn("error message : {}", e.getMessage());
                throw new CustomException(ErrorCode.FAIL_SERIALIZATION);
            }
        }
        return new ReceivedInviteResponse(child.getUuid(), parentList, inviteInfoList);
    }

    @Override
    public MemberInfoResponse getMemberInfo(Long memberId) {

        Member member = getMember(memberId);

        String city = getCityNameByRegionCode(member.getRegionCode());
        String district = getDistrictNameRegionCode(member.getRegionCode());

        return MemberInfoResponse.from(member, city, district);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberScoreResponse getMemberScore(LocalDate date) {
        Member member = getCurrentMember();

        if(member.getRole().equals(RoleType.STUDENT)) {
            MemberScore memberScore = memberScoreRepository.findByMemberAndDate(member, date)
                    .orElse(null);

            if(memberScore == null) {
                return MemberScoreResponse.builder()
                        .score(0)
                        .build();
            }

            return MemberScoreResponse.builder()
                    .score(memberScore.getTotalScore())
                    .build();
        }

        if(member.getRole().equals(RoleType.PARENT)) {
            List<ParentChild> parentChildList = parentChildRepository.findAllByParent(member);
            if(parentChildList.isEmpty()) { // 연결된 학생이 없을 경우 예외 처리
                throw new CustomException(ErrorCode.CHILD_NOT_FOUND);
            }
            Member child = parentChildList.get(0).getChild();
            MemberScore childScore = memberScoreRepository.findByMemberAndDate(child, date)
                    .orElse(null);

            if(childScore == null) {
                return MemberScoreResponse.builder()
                        .percent(100.0)
                        .build();
            }

            List<MemberScore> peerScores = memberScoreRepository.findAllByDate(date);

            int totalPeers = peerScores.size();
            int lowerThanChild = (int) peerScores.stream()
                    .filter(score -> score.getTotalScore() < childScore.getTotalScore())
                    .count();

            double percentile = (double) lowerThanChild / totalPeers * 100.0;

            return MemberScoreResponse.builder()
                    .percent(percentile)
                    .build();
        }

        throw new CustomException(ErrorCode.SERVER_ERROR);
    }

    @Override
    public ChildListResponse getChildList() {
        Member member = getCurrentMember();
        if(!member.getRole().equals(RoleType.PARENT)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        List<Member> children = parentChildRepository.findChildMembersByParent(member);

        List<ChildListResponse.ChildResponse> childResponses = children.stream()
                .map(child -> new ChildListResponse.ChildResponse(
                        child.getId(),
                        child.getNickname(),
                        child.getRole().name()
                ))
                .toList();

        return new ChildListResponse(childResponses);
    }

    private String buildInviteKey(Long parentId, Long childId) {
        return INVITE_PREFIX + parentId + ":" + childId;
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return memberRepository.findById(((CustomUserDetails) authentication.getPrincipal()).getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private String getCityNameByRegionCode(String regionCode) {
        return regionTypeRepository.findParentRegionTypeByPrefix(regionCode.substring(0, 2)).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_CITY)).getName();
    }

    private String getDistrictNameRegionCode(String regionCode) {
        return regionTypeRepository.findChildRegionTypeByPrefix(regionCode.substring(0, 5)).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REGION_DISTRICT)).getName();
    }
}
