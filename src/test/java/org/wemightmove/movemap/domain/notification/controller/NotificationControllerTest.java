package org.wemightmove.movemap.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.notification.dto.request.DeviceRegisterRequest;
import org.wemightmove.movemap.domain.notification.repository.NotificationRepository;
import org.wemightmove.movemap.global.enums.DeviceType;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.security.CustomUserDetails;
import org.wemightmove.movemap.global.support.IntegrationTestSupport;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
        @Sql(value = "/sql/notification-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@Testcontainers
class NotificationControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new ObjectMapper();

    private static final Long TEST_MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        // SecurityContext에 Mock 사용자 등록
        Member mockMember = mock(Member.class);
        when(mockMember.getId()).thenReturn(1L);
        when(mockMember.getEmail()).thenReturn("parent1@test.com");
        when(mockMember.getNickname()).thenReturn("테스트부모1");
        when(mockMember.getRole()).thenReturn(RoleType.PARENT);
        when(mockMember.getRegionCode()).thenReturn("11680");

        CustomUserDetails userDetails = new CustomUserDetails(mockMember);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // 기기 등록
    @Test
    void 새로운_기기를_등록할_수_있다() throws Exception {
        // given
        DeviceRegisterRequest request = new DeviceRegisterRequest("ExponentPushToken[new-device-token]", DeviceType.ANDROID.name(), "new-device-001");

        // when
        // then
        mockMvc.perform(post("/notifications").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // DB 검증
        var saved = notificationRepository.findByMemberIdAndDeviceId(TEST_MEMBER_ID, "new-device-001");
        assertThat(saved).isPresent();
        assertThat(saved.get().getFcmToken()).isEqualTo("ExponentPushToken[new-device-token]");
    }

    @Test
    void 기존_기기의_토큰을_갱신할_수_있다() throws Exception {
        // given
        String newToken = "ExponentPushToken[updated-token]";
        DeviceRegisterRequest request = new DeviceRegisterRequest(
                newToken,
                DeviceType.ANDROID.name(),
                "device-001"
        );

        // when
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        // then
        var updated = notificationRepository.findByMemberIdAndDeviceId(TEST_MEMBER_ID, "device-001");
        assertThat(updated).isPresent();
        assertThat(updated.get().getFcmToken()).isEqualTo(newToken);
    }

    @Test
    void 필수_필드가_없으면_400_에러가_발생한다() throws Exception {
        // given
        DeviceRegisterRequest invalidRequest = new DeviceRegisterRequest(null, DeviceType.ANDROID.name(), "device-001");

        // when
        // then
        mockMvc.perform(post("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // 푸시 수신 설정
    @Test
    void 푸시_수신을_비활성화_할_수_있다() throws Exception {
        // given
        // when
        mockMvc.perform(patch("/notifications/{deviceId}/push", "device-001")
                .param("enabled", "false"))
                .andExpect(status().isOk());

        // then
        var notification = notificationRepository.findByMemberIdAndDeviceId(TEST_MEMBER_ID, "device-001");
        assertThat(notification).isPresent();
        assertThat(notification.get().isPushEnabled()).isFalse();
    }

    @Test
    void 푸시_수신을_다시_활성화_할_수_있다() throws Exception {
        // given
        mockMvc.perform(patch("/notifications/{deviceId}/push", "device-001")
                .param("enabled", "false"))
                .andExpect(status().isOk());

        // when
        mockMvc.perform(patch("/notifications/{deviceId}/push", "device-001")
                        .param("enabled", "true"))
                .andExpect(status().isOk());

        // then
        var notification = notificationRepository.findByMemberIdAndDeviceId(TEST_MEMBER_ID, "device-001");
        assertThat(notification).isPresent();
        assertThat(notification.get().isPushEnabled()).isTrue();
    }

    @Test
    void 존재하지_않는_기기의_푸시_설정을_변경하면_404_에러가_발생한다() throws Exception {
        // when
        // then
        mockMvc.perform(patch("/notifications/{deviceId}/push", "non-existent-device")
                        .param("enabled", "false"))
                .andExpect(status().isNotFound());
    }

    // 기기 등록 해제
    @Test
    void 기기_등록을_해제할_수_있다() throws Exception {
        // given
        // when
        mockMvc.perform(delete("/notifications/{deviceId}", "device-001"))
                .andExpect(status().isOk());

        // then
        var deleted = notificationRepository.findByMemberIdAndDeviceId(TEST_MEMBER_ID, "device-001");
        assertThat(deleted).isEmpty();
    }

    @Test
    void 존재하지_않는_기기를_해제해도_성공한다() throws Exception {
        // when
        // then
        mockMvc.perform(delete("/notifications/{deviceId}", "non-existent-device"))
                .andExpect(status().isOk());
    }
}