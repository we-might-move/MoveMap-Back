package org.wemightmove.movemap.domain.league.controller;

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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.security.CustomUserDetails;
import org.wemightmove.movemap.global.support.IntegrationTestBase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
        @Sql(value = "/sql/league-controller-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest
@ActiveProfiles("test")
class LeagueControllerTest extends IntegrationTestBase {

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
        when(mockMember.getRegionCode()).thenReturn("11999");

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

    // TODO : 날짜 의존성 제거
//    @Test
//    @DisplayName("사용자는 리그별 주간 지역구 순위를 조회할 수 있다")
//    void 사용자는_리그별_주간_지역구_순위를_조회할_수_있다() throws Exception{
//        //given
//        //when
//        //then
//        mockMvc.perform(get("/league"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.date").value("2026-01-06"))
//                .andExpect(jsonPath("$.ranks").isMap())
//                .andExpect(jsonPath("$.ranks.START").exists())
//                .andExpect(jsonPath("$.ranks.WALK").exists());
//    }

    // ==========================================================================================

    @Test
    @DisplayName("사용자는 본인이 속한 자치구의 리그를 조회할 수 있다")
    void 사용자는_본인이_속한_자치구의_리그를_조회할_수_있다() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/league/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regionId").isNumber())
                .andExpect(jsonPath("$.regionName").value("test"))
                .andExpect(jsonPath("$.leagueType").value("START"))
                .andExpect(jsonPath("$.leagueColorCode").value("#FFB6C1"));
    }
}