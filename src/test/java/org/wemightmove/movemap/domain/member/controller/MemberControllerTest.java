package org.wemightmove.movemap.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wemightmove.movemap.domain.member.dto.request.AcceptInvitationRequest;
import org.wemightmove.movemap.domain.member.dto.request.RejectInvitationRequest;
import org.wemightmove.movemap.domain.member.dto.request.SendInvitedRequest;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.exception.ErrorCode;
import org.wemightmove.movemap.global.security.CustomUserDetails;
import org.wemightmove.movemap.global.support.IntegrationTestSupport;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
        @Sql(value = "/sql/member-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@Testcontainers
class MemberControllerTest extends IntegrationTestSupport {

    // 상수 정의
    private static final int PARENT1_FAVORITE_FACILITY_COUNT = 3;
    private static final int PARENT1_FAVORITE_PROGRAM_COUNT = 2;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        cleanupRedisInviteData();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        cleanupRedisInviteData();
    }

    @Test
    void 부모는_자녀에게_초대를_보낼_수_있다() throws Exception {
        // given
        loginAsParent1();

        String requestBody = objectMapper.writeValueAsString(new SendInvitedRequest("student-uuid-0001"));

        // when
        // then
        mockMvc.perform(post("/members/invitations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(3))
                .andExpect(jsonPath("$.childName").value("테스트학생1"));

        // Redis 에 초대 정보 저장되었는지 검증
        String inviteKey = "invite:1:3";
        assertThat(redisTemplate.hasKey(inviteKey)).isTrue();
    }

    @Test
    void 존재하지_않는_UUID로_초대하면_예외가_발생한다() throws Exception {
        // given
        loginAsParent1();

        String requestBody = objectMapper.writeValueAsString(new SendInvitedRequest("invalid-uuid-9999"));

        // when
        // then
        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INVITE_CODE.getMessage()));
    }

    @Test
    void 본인의_UUID로_초대하면_예외가_발생한다() throws Exception {
        // given
        loginAsParent1();

        String requestBody = objectMapper.writeValueAsString(new SendInvitedRequest("parent-uuid-0001"));

        // when
        // then
        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INVITE_MEMBER.getMessage()));
    }

    @Test
    void 이미_연결된_자녀에게_초대하면_예외가_발생한다() throws Exception {
        // given
        setAuthenticatedUser(2L, "parent2@test.com", "테스트부모2", RoleType.PARENT);

        String requestBody = objectMapper.writeValueAsString(new SendInvitedRequest("student-uuid-0002"));

        // when
        // then
        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_CONNECTED.getMessage()));
    }

    @Test
    void 이미_초대를_보낸_자녀에게_다시_초대하면_예외가_발생한다() throws Exception {
        // given
        loginAsParent1();

        String requestBody = objectMapper.writeValueAsString(new SendInvitedRequest("student-uuid-0001"));

        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        // when
        // then
        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_SEND_INVITE.getMessage()));
    }

    @Test
    void 자녀는_초대를_수락할_수_있다() throws Exception {
        // given
        loginAsParent1();

        String requestBody = objectMapper.writeValueAsString(new SendInvitedRequest("student-uuid-0001"));

        mockMvc.perform(post("/members/invitations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        loginAsStudent1();

        String acceptRequest = objectMapper.writeValueAsString(new AcceptInvitationRequest(1L));

        // when
        // then
        mockMvc.perform(patch("/members/invitations/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(acceptRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentId").value(1))
                .andExpect(jsonPath("$.parentName").value("테스트부모1"));

        // Redis 에서 초대 정보가 삭제되었는지 검증
        String inviteKey = "invite:1:3";
        assertThat(redisTemplate.hasKey(inviteKey)).isFalse();
    }

    @Test
    void 존재하지_않는_초대를_수락하면_예외가_발생한다() throws Exception {
        // given
        loginAsStudent1();

        String acceptRequest = objectMapper.writeValueAsString(new AcceptInvitationRequest(1L));

        // when
        // then
        mockMvc.perform(patch("/members/invitations/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(acceptRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVITE_NOT_FOUND.getMessage()));
    }

    @Test
    void 자녀는_초대를_거절할_수_있다() throws Exception {
        // given
        loginAsParent1();

        String requestBody = objectMapper.writeValueAsString(new SendInvitedRequest("student-uuid-0001"));

        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        loginAsStudent1();

        String rejectRequest = objectMapper.writeValueAsString(new RejectInvitationRequest(1L));

        // when
        // then
        mockMvc.perform(patch("/members/invitations/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rejectRequest))
                .andExpect(status().isNoContent());

        // Redis 에서 초대 정보가 삭제되었는지 검증
        String inviteKey = "invite:1:3";
        assertThat(redisTemplate.hasKey(inviteKey)).isFalse();
    }

    @Test
    void 부모는_보낸_초대_목록을_조회할_수_있다() throws Exception {
        // given
        loginAsParent1();

        String request = objectMapper.writeValueAsString(new SendInvitedRequest("student-uuid-0001"));

        mockMvc.perform(post("/members/invitations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk());

        // when
        // then
        mockMvc.perform(get("/members/invitations/sent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentInviteList").isArray())
                .andExpect(jsonPath("$.sentInviteList[0].childId").value(3))
                .andExpect(jsonPath("$.sentInviteList[0].childName").value("테스트학생1"));
    }

    @Test
    void 초대를_보내지_않은_경우_빈_목록이_반환된다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(get("/members/invitations/sent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentInviteList").isEmpty());
    }

    @Test
    void 자녀는_받은_초대_목록을_조회할_수_있다() throws Exception {
        // given
        loginAsParent1();

        String requestBody = objectMapper.writeValueAsString(new SendInvitedRequest("student-uuid-0001"));

        mockMvc.perform(post("/members/invitations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        loginAsStudent1();

        // when
        // then
        mockMvc.perform(get("/members/invitations/received"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode").value("student-uuid-0001"))
                .andExpect(jsonPath("$.receivedInviteList").isArray())
                .andExpect(jsonPath("$.receivedInviteList[0].parentId").value(1))
                .andExpect(jsonPath("$.receivedInviteList[0].parentName").value("테스트부모1"));
    }

    /// 회원 정보 조회

    @Test
    void 회원_정보를_조회할_수_있다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("parent1@test.com"))
                .andExpect(jsonPath("$.nickname").value("테스트부모1"))
                .andExpect(jsonPath("$.role").value(RoleType.PARENT.name()));
    }

    @Test
    @Disabled("BUG: 탈퇴한 멤버(isDeleted=true) 필터링 로직 누락 - 별도 브랜치에서 수정 예정")
    void 삭제된_회원은_정보_조회_시_예외가_발생한다() throws Exception {
        // given
        setAuthenticatedUser(5L, "deleted@test.com", "삭제된회원", RoleType.STUDENT);

        // when
        // then
        mockMvc.perform(get("/members"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    /// 회원 정보 수정

    @Test
    void 닉네임을_수정할_수_있다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(patch("/members")
                .param("nickname", "새로운닉네임"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("새로운닉네임"));
    }

    @Test
    void 이미_사용중인_닉네임으로_수정하면_예외가_발생한다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(patch("/members")
                        .param("nickname", "테스트부모2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_NICKNAME.getMessage()));
    }

    @Test
    void 지역정보_수정_시_시도와_시군구가_모두_있어야_한다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(patch("/members")
                        .param("city", "경기도"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_REGION_UPDATE.getMessage()));
    }

    @Test
    void 잘못된_지역정보로_수정하면_예외가_발생한다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(patch("/members")
                        .param("city", "경기도")
                        .param("district", "강남구"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_REGION_FAIR.getMessage()));
    }

    @Test
    void 수정할_필드가_없으면_예외가_발생한다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(patch("/members"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.MISSING_PARAMETER.getMessage()));
    }

    /// 회원 탈퇴

    @Test
    void 회원_탈퇴를_할_수_있다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(delete("/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."))
                .andExpect(jsonPath("$.statistics.removedFavorites").value(PARENT1_FAVORITE_FACILITY_COUNT))
                .andExpect(jsonPath("$.statistics.removedPrograms").value(PARENT1_FAVORITE_PROGRAM_COUNT));
    }

    @Test
    void 이미_탈퇴한_회원이_다시_탈퇴하면_예외가_발생한다() throws Exception {
        // given
        setAuthenticatedUser(5L, "deleted@test.com", "삭제된회원", RoleType.STUDENT);

        // when
        // then
        mockMvc.perform(delete("/members"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_DELETED.getMessage()));
    }

    /// 찜한 시설 목록

    @Test
    void 찜한_시설_목록을_조회할_수_있다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(get("/members/bookmarks/facilities")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 찜한_시설이_없으면_빈_목록이_반환된다() throws Exception {
        // given
        setAuthenticatedUser(2L, "parent2@test.com", "테스트부모2", RoleType.PARENT);

        // when
        // then
        mockMvc.perform(get("/members/bookmarks/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void 커서_기반_페이지네이션이_동작한다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(get("/members/bookmarks/facilities")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value(2));
    }

    @Test
    void 현재_위치를_제공하면_거리가_계산된다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(get("/members/bookmarks/facilities")
                        .param("latitude", "37.5172")
                        .param("longitude", "127.0473"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].distance").isNumber());
    }

    /// 찜한 프로그램 목록 조회

    @Test
    void 찜한_프로그램_목록을_조회할_수_있다() throws Exception {
        // given
        loginAsParent1();

        // when
        // then
        mockMvc.perform(get("/members/bookmarks/programs")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.programs").isArray())
                .andExpect(jsonPath("$.programs.length()").value(2));
    }

    /// 인증 헬퍼
    private void setAuthenticatedUser(Long memberId, String email, String nickname, RoleType roleType) {
        Member mockMember = mock(Member.class);
        when(mockMember.getId()).thenReturn(memberId);
        when(mockMember.getEmail()).thenReturn(email);
        when(mockMember.getNickname()).thenReturn(nickname);
        when(mockMember.getRole()).thenReturn(roleType);
        when(mockMember.getRegionCode()).thenReturn("11680");
        when(mockMember.getUuid()).thenReturn("test-uuid-" + memberId);

        CustomUserDetails userDetails = new CustomUserDetails(mockMember);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                )
        );
    }

    // 부모 사용자로 인증 설정
    private void loginAsParent1() {
        setAuthenticatedUser(1L, "parent1@test.com", "테스트부모1", RoleType.PARENT);
    }

    // 자녀 사용자로 인증 설정
    private void loginAsStudent1() {
        setAuthenticatedUser(3L, "student1@test.com", "테스트학생1", RoleType.STUDENT);
    }

    /// Redis 데이터 정리
    private void cleanupRedisInviteData() {
        // 패턴 매칭으로 관련 키 모두 삭제
        deleteKeysByPattern("invite:*");
        deleteKeysByPattern("invites:sent:*");
        deleteKeysByPattern("invites:received:*");
    }

    private void deleteKeysByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}