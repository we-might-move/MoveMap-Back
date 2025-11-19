package org.wemightmove.movemap.domain.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.member.dto.response.InviteInfo;
import org.wemightmove.movemap.domain.member.dto.response.SendInviteResponse;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.domain.member.repository.ParentChildRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
/**
 * FIXME: MemberCommand, Query 로 나누는게 나을지 아닐지 결정하기
 */
public class MemberCommandServiceImpl implements MemberCommandService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final ParentChildRepository parentChildRepository;

    private static final String INVITE_PREFIX = "invite:";
    private static final String SENT_LIST_PREFIX = "invites:sent:";
    private static final String RECEIVED_LIST_PREFIX = "invites:received:";

    @Value("${invite.expiration-days:3}")
    private long inviteExpirationDays;

    /**
     * FIXME: 초대 발송 후 알림 전송해야 하는지 결정하기
     * 초대 발송
     */
    @Transactional
    @Override
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
        InviteInfo inviteInfo = InviteInfo.of(parent.getId(), child.getId(), LocalDateTime.now());

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

        return new SendInviteResponse(child.getId(), child.getNickname());
    }

    private String buildInviteKey(Long parentId, Long childId) {
        return INVITE_PREFIX + parentId + ":" + childId;
    }
}
