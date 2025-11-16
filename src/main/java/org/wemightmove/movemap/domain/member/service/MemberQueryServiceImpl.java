package org.wemightmove.movemap.domain.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.dto.response.InviteInfo;
import org.wemightmove.movemap.domain.member.dto.response.MemberInfo;
import org.wemightmove.movemap.domain.member.dto.response.ReceivedInviteResponse;
import org.wemightmove.movemap.domain.member.dto.response.SentInviteResponse;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.member.repository.ParentChildRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberQueryServiceImpl implements MemberQueryService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final ParentChildRepository parentChildRepository;

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
                .map(m -> MemberInfo.of(m.getChild().getId(), m.getChild().getNickname(), m.getChild().getRole())).
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
                .map(m -> MemberInfo.of(m.getParent().getId(), m.getParent().getNickname(), m.getParent().getRole())).toList();
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

    private String buildInviteKey(Long parentId, Long childId) {
        return INVITE_PREFIX + parentId + ":" + childId;
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
