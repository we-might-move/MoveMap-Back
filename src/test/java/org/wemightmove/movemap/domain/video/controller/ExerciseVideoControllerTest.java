package org.wemightmove.movemap.domain.video.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.security.CustomUserDetails;
import org.wemightmove.movemap.global.support.IntegrationTestBase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@ActiveProfiles("test")
class ExerciseVideoControllerTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // SecurityContext에 Mock 사용자 등록
        Member mockMember = mock(Member.class);
        when(mockMember.getId()).thenReturn(1L);
        when(mockMember.getEmail()).thenReturn("student1@test.com");
        when(mockMember.getNickname()).thenReturn("테스트학생1");
        when(mockMember.getRole()).thenReturn(RoleType.STUDENT);
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

    // ==========================================================================================

    @Test
    @DisplayName("홈 화면에서 랜덤 운동 동영상 코드를 조회할 수 있다")
    void 홈_화면에서_랜덤_운동_동영상_코드를_조회할_수_있다() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.videoId").exists())
                .andExpect(jsonPath("$.videoId").isString())
                .andExpect(jsonPath("$.videoId").isNotEmpty());
    }
}