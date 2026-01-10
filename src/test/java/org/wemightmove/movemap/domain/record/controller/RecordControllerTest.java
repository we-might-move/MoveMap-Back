package org.wemightmove.movemap.domain.record.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.CheckInRecordModifyRequest;
import org.wemightmove.movemap.domain.record.dto.request.SelfRecordAddRequest;
import org.wemightmove.movemap.domain.record.dto.request.StepsRecordSyncRequest;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.security.CustomUserDetails;
import org.wemightmove.movemap.global.support.IntegrationTestBase;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
        @Sql(value = "/sql/record-controller-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest
@ActiveProfiles("test")
public class RecordControllerTest extends IntegrationTestBase {

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
    @DisplayName("사용자는 셀프 기록을 추가할 수 있다")
    void 사용자는_셀프_기록을_추가할_수_있다() throws Exception {
        //given
        SelfRecordAddRequest selfRecordAddRequest = new SelfRecordAddRequest(FacilityType.FITNESS,1, 30);
        //when
        //then
        mockMvc.perform(
                        post("/records/self")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(selfRecordAddRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("운동 시간이 0분이면 셀프 기록 추가에 실패한다")
    void 운동_시간이_0분이면_셀프_기록_추가에_실패한다() throws Exception {
        //given
        SelfRecordAddRequest selfRecordAddRequest = new SelfRecordAddRequest(FacilityType.FITNESS,0, 0);
        //when
        //then
        mockMvc.perform(
                        post("/records/self")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(selfRecordAddRequest)))
                .andExpect(status().is4xxClientError());
    }

    // ==========================================================================================

    @Test
    @DisplayName("학생은 본인의 일별 셀프 기록을 조회할 수 있다")
    void 학생은_본인의_일별_셀프_기록을_조회할_수_있다() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/records/self")
                    .param("date", "2026-01-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-01-06"))
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.records.length()").value(2))
                .andExpect(jsonPath("$.records[0].type").value("FITNESS"))
                .andExpect(jsonPath("$.records[0].typeName").value("피트니스"))
                .andExpect(jsonPath("$.records[0].durationMinutes").value(60))
                .andExpect(jsonPath("$.records[1].type").value("LEISURE"))
                .andExpect(jsonPath("$.records[1].typeName").value("레저·야외"))
                .andExpect(jsonPath("$.records[1].durationMinutes").value(30));
    }

    @Test
    @DisplayName("부모는 자식의 일별 셀프 기록을 조회할 수 있다")
    void 부모는_자식의_일별_셀프_기록을_조회할_수_있다() throws Exception {
        // given: 부모로 SecurityContext 덮어쓰기
        Member parent = mock(Member.class);
        when(parent.getId()).thenReturn(4L);
        when(parent.getEmail()).thenReturn("parent2@test.com");
        when(parent.getNickname()).thenReturn("테스트부모2");
        when(parent.getRole()).thenReturn(RoleType.PARENT);
        when(parent.getRegionCode()).thenReturn("11680");

        CustomUserDetails parentDetails = new CustomUserDetails(parent);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(parentDetails, null, parentDetails.getAuthorities())
        );
        //when
        //then
        mockMvc.perform(get("/records/self")
                        .param("date", "2026-01-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-01-05"))
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.records.length()").value(3))
                .andExpect(jsonPath("$.records[0].type").value("DANCE"))
                .andExpect(jsonPath("$.records[0].typeName").value("무용·댄스"))
                .andExpect(jsonPath("$.records[0].durationMinutes").value(70))
                .andExpect(jsonPath("$.records[1].type").value("AQUATIC"))
                .andExpect(jsonPath("$.records[1].typeName").value("수상·빙상"))
                .andExpect(jsonPath("$.records[1].durationMinutes").value(100))
                .andExpect(jsonPath("$.records[2].type").value("COMPLEX"))
                .andExpect(jsonPath("$.records[2].typeName").value("종합체육시설"))
                .andExpect(jsonPath("$.records[2].durationMinutes").value(120));
    }

    // ==========================================================================================

    @Test
    @DisplayName("사용자는 오늘자 걸음수 데이터를 동기화할 수 있다")
    void 사용자는_오늘자_걸음수_데이터를_동기화할_수_있다() throws Exception {
        //given
        LocalDateTime time = LocalDateTime.of(2025, 1, 2, 3, 4, 5);
        StepsRecordSyncRequest stepsRecordSyncRequest = new StepsRecordSyncRequest(100, 0.1, time);

        //when
        //then
        mockMvc.perform(
                        post("/records/steps")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stepsRecordSyncRequest)))
                .andExpect(status().isCreated());
    }

    // ==========================================================================================

    @Test
    @DisplayName("학생은 본인의 일별 걷기 기록을 조회할 수 있다")
    void 학생은_본인의_일별_걷기_기록을_조회할_수_있다() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/records/steps")
                        .param("date", "2026-01-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-01-06"))
                .andExpect(jsonPath("$.count").value(3000))
                .andExpect(jsonPath("$.distance").value(1.52));
    }

    @Test
    @DisplayName("부모는 자식의 일별 걷기 기록을 조회할 수 있다")
    void 부모는_자식의_일별_걷기_기록을_조회할_수_있다() throws Exception {
        // given: 부모로 SecurityContext 덮어쓰기
        Member parent = mock(Member.class);
        when(parent.getId()).thenReturn(4L);
        when(parent.getEmail()).thenReturn("parent2@test.com");
        when(parent.getNickname()).thenReturn("테스트부모2");
        when(parent.getRole()).thenReturn(RoleType.PARENT);
        when(parent.getRegionCode()).thenReturn("11680");

        CustomUserDetails parentDetails = new CustomUserDetails(parent);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(parentDetails, null, parentDetails.getAuthorities())
        );
        //when
        //then
        mockMvc.perform(get("/records/steps")
                        .param("date", "2026-01-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-01-05"))
                .andExpect(jsonPath("$.count").value(20000))
                .andExpect(jsonPath("$.distance").value(10.56));
    }

    // ==========================================================================================

    @Test
    @DisplayName("사용자는 체크인할 수 있다")
    void 사용자는_체크인할_수_있다() throws Exception {
        //given : 다른 학생 계정으로 SecurityContext 덮어쓰기
        Member student2 = mock(Member.class);
        when(student2.getId()).thenReturn(3L);
        when(student2.getEmail()).thenReturn("student2@test.com");
        when(student2.getNickname()).thenReturn("테스트학생2");
        when(student2.getRole()).thenReturn(RoleType.STUDENT);
        when(student2.getRegionCode()).thenReturn("11680");

        CustomUserDetails userDetails = new CustomUserDetails(student2);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        LocalDateTime time = LocalDateTime.now();
        CheckInRecordAddRequest checkInRecordAddRequest = new CheckInRecordAddRequest(1L, time);

        //when
        //then
        mockMvc.perform(
                        post("/records/checkin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(checkInRecordAddRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8));
    }

    // ==========================================================================================

    @Test
    @DisplayName("사용자는 체크아웃할 수 있다")
    void 사용자는_체크아웃할_수_있다() throws Exception {
        //given
        LocalDateTime time = LocalDateTime.of(2026, 1, 6, 12, 4, 5);
        CheckInRecordModifyRequest checkInRecordModifyRequest = new CheckInRecordModifyRequest(6, time);

        //when
        //then
        mockMvc.perform(
                        patch("/records/checkout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(checkInRecordModifyRequest)))
                .andExpect(status().isOk());
    }

    // ==========================================================================================

    @Test
    @DisplayName("사용자가 체크인 상태이면 true를 조회한다")
    void 사용자가_체크인_상태이면_true를_조회한다() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/records/checkin/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCheckedIn").value(true));
    }

    @Test
    @DisplayName("사용자가 체크인 상태가 아니면 false를 조회한다")
    void 사용자가_체크인_상태가_아니면_false를_조회한다() throws Exception {
        // given: 다른 학생 계정으로 SecurityContext 덮어쓰기
        Member student2 = mock(Member.class);
        when(student2.getId()).thenReturn(3L);
        when(student2.getEmail()).thenReturn("student2@test.com");
        when(student2.getNickname()).thenReturn("테스트학생2");
        when(student2.getRole()).thenReturn(RoleType.STUDENT);
        when(student2.getRegionCode()).thenReturn("11680");

        CustomUserDetails studentDetails = new CustomUserDetails(student2);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(studentDetails, null, studentDetails.getAuthorities())
        );

        //when
        //then
        mockMvc.perform(get("/records/checkin/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCheckedIn").value(false));
    }

    // ==========================================================================================

    @Test
    @DisplayName("학생은 본인의 날짜별 체크인 기록을 조회할 수 있다")
    void 학생은_본인의_날짜별_체크인_기록을_조회할_수_있다() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/records/checkin")
                        .param("date", "2026-01-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-01-05"))
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.records.length()").value(5))
                .andExpect(jsonPath("$.records[0].facilityId").value(2))
                .andExpect(jsonPath("$.records[0].facilityName").value("(평창)청성골프타운(임대)"))
                .andExpect(jsonPath("$.records[0].checkInAt").value("09:30"))
                .andExpect(jsonPath("$.records[0].checkOutAt").value("11:20"))
                .andExpect(jsonPath("$.records[0].durationMinutes").value(110))
                .andExpect(jsonPath("$.records[1].facilityId").value(5))
                .andExpect(jsonPath("$.records[1].facilityName").value("88실외연습장"))
                .andExpect(jsonPath("$.records[1].checkInAt").value("11:30"))
                .andExpect(jsonPath("$.records[1].checkOutAt").value("12:00"))
                .andExpect(jsonPath("$.records[1].durationMinutes").value(30))
                .andExpect(jsonPath("$.records[2].facilityId").value(6))
                .andExpect(jsonPath("$.records[2].facilityName").value("test골프12"))
                .andExpect(jsonPath("$.records[2].checkInAt").value("12:30"))
                .andExpect(jsonPath("$.records[2].checkOutAt").value("13:00"))
                .andExpect(jsonPath("$.records[2].durationMinutes").value(30))
                .andExpect(jsonPath("$.records[3].facilityId").value(7))
                .andExpect(jsonPath("$.records[3].facilityName").value("가곡 파크골프장"))
                .andExpect(jsonPath("$.records[3].checkInAt").value("13:30"))
                .andExpect(jsonPath("$.records[3].checkOutAt").value("14:00"))
                .andExpect(jsonPath("$.records[3].durationMinutes").value(30))
                .andExpect(jsonPath("$.records[4].facilityId").value(8))
                .andExpect(jsonPath("$.records[4].facilityName").value("가야그라운드골프장"))
                .andExpect(jsonPath("$.records[4].checkInAt").value("14:30"))
                .andExpect(jsonPath("$.records[4].checkOutAt").value("15:00"))
                .andExpect(jsonPath("$.records[4].durationMinutes").value(30));

    }

    @Test
    @DisplayName("부모는 자식의 날짜별 체크인 기록을 조회할 수 있다")
    void 부모는_자식의_날짜별_체크인_기록을_조회할_수_있다() throws Exception {
        // given: 부모로 SecurityContext 덮어쓰기
        Member parent = mock(Member.class);
        when(parent.getId()).thenReturn(2L);
        when(parent.getEmail()).thenReturn("parent1@test.com");
        when(parent.getNickname()).thenReturn("테스트부모1");
        when(parent.getRole()).thenReturn(RoleType.PARENT);
        when(parent.getRegionCode()).thenReturn("11680");

        CustomUserDetails parentDetails = new CustomUserDetails(parent);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(parentDetails, null, parentDetails.getAuthorities())
        );

        //when
        //then
        mockMvc.perform(get("/records/checkin")
                        .param("date", "2026-01-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-01-05"))
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.records.length()").value(5))
                .andExpect(jsonPath("$.records[0].facilityId").value(2))
                .andExpect(jsonPath("$.records[0].facilityName").value("(평창)청성골프타운(임대)"))
                .andExpect(jsonPath("$.records[0].checkInAt").value("09:30"))
                .andExpect(jsonPath("$.records[0].checkOutAt").value("11:20"))
                .andExpect(jsonPath("$.records[0].durationMinutes").value(110))
                .andExpect(jsonPath("$.records[1].facilityId").value(5))
                .andExpect(jsonPath("$.records[1].facilityName").value("88실외연습장"))
                .andExpect(jsonPath("$.records[1].checkInAt").value("11:30"))
                .andExpect(jsonPath("$.records[1].checkOutAt").value("12:00"))
                .andExpect(jsonPath("$.records[1].durationMinutes").value(30))
                .andExpect(jsonPath("$.records[2].facilityId").value(6))
                .andExpect(jsonPath("$.records[2].facilityName").value("test골프12"))
                .andExpect(jsonPath("$.records[2].checkInAt").value("12:30"))
                .andExpect(jsonPath("$.records[2].checkOutAt").value("13:00"))
                .andExpect(jsonPath("$.records[2].durationMinutes").value(30))
                .andExpect(jsonPath("$.records[3].facilityId").value(7))
                .andExpect(jsonPath("$.records[3].facilityName").value("가곡 파크골프장"))
                .andExpect(jsonPath("$.records[3].checkInAt").value("13:30"))
                .andExpect(jsonPath("$.records[3].checkOutAt").value("14:00"))
                .andExpect(jsonPath("$.records[3].durationMinutes").value(30))
                .andExpect(jsonPath("$.records[4].facilityId").value(8))
                .andExpect(jsonPath("$.records[4].facilityName").value("가야그라운드골프장"))
                .andExpect(jsonPath("$.records[4].checkInAt").value("14:30"))
                .andExpect(jsonPath("$.records[4].checkOutAt").value("15:00"))
                .andExpect(jsonPath("$.records[4].durationMinutes").value(30));
    }

    // ==========================================================================================

    @Test
    @DisplayName("학생은 본인의 월별 운동 기록 여부를 조회할 수 있다")
    void 학생은_본인의_월별_운동_기록_여부를_조회할_수_있다() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/records/monthly")
                        .param("year", "2026")
                        .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.flags").isMap())
                .andExpect(jsonPath("$.flags").isNotEmpty())
                .andExpect(jsonPath("$.flags['3']").value(true))   // self > 0
                .andExpect(jsonPath("$.flags['5']").value(true))   // checkin > 0
                .andExpect(jsonPath("$.flags['10']").value(false)) // steps = 9999
                .andExpect(jsonPath("$.flags['11']").value(true))  // steps = 10000 (경계값)
                .andExpect(jsonPath("$.flags['12']").value(true))  // steps > 10000
                .andExpect(jsonPath("$.flags['15']").value(false)) // 모든 값 0
                .andExpect(jsonPath("$.flags['20']").value(true)) // 복합 조건
                .andExpect(jsonPath("$.flags['1']").value(false)); // row 자체 없음
    }

    @Test
    @DisplayName("부모는 자식의 월별 운동 기록 여부를 조회할 수 있다")
    void 부모는_자식의_월별_운동_기록_여부를_조회할_수_있다() throws Exception {
        // given: 부모로 SecurityContext 덮어쓰기
        Member parent = mock(Member.class);
        when(parent.getId()).thenReturn(2L);
        when(parent.getEmail()).thenReturn("parent1@test.com");
        when(parent.getNickname()).thenReturn("테스트부모1");
        when(parent.getRole()).thenReturn(RoleType.PARENT);
        when(parent.getRegionCode()).thenReturn("11680");

        CustomUserDetails parentDetails = new CustomUserDetails(parent);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(parentDetails, null, parentDetails.getAuthorities())
        );

        //when
        //then
        mockMvc.perform(get("/records/monthly")
                        .param("year", "2026")
                        .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.flags").isMap())
                .andExpect(jsonPath("$.flags").isNotEmpty())
                .andExpect(jsonPath("$.flags['3']").value(true))   // self > 0
                .andExpect(jsonPath("$.flags['5']").value(true))   // checkin > 0
                .andExpect(jsonPath("$.flags['10']").value(false)) // steps = 9999
                .andExpect(jsonPath("$.flags['11']").value(true))  // steps = 10000 (경계값)
                .andExpect(jsonPath("$.flags['12']").value(true))  // steps > 10000
                .andExpect(jsonPath("$.flags['15']").value(false)) // 모든 값 0
                .andExpect(jsonPath("$.flags['20']").value(true)) // 복합 조건
                .andExpect(jsonPath("$.flags['1']").value(false)); // row 자체 없음
    }

    // ==========================================================================================
    //TODO : 데이터 삽입

    @Test
    @DisplayName("부모는 아이의 위클리 리포트를 조회할 수 있다 - 날짜 파라미터를 포함하는 경우")
    void 부모는_아이의_위클리_리포트를_조회할_수_있다_날짜_파라미터를_포함하는_경우() throws Exception {
        // given: 부모로 SecurityContext 덮어쓰기
        Member parent = mock(Member.class);
        when(parent.getId()).thenReturn(4L);
        when(parent.getEmail()).thenReturn("parent2@test.com");
        when(parent.getNickname()).thenReturn("테스트부모2");
        when(parent.getRole()).thenReturn(RoleType.PARENT);
        when(parent.getRegionCode()).thenReturn("11680");

        CustomUserDetails parentDetails = new CustomUserDetails(parent);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(parentDetails, null, parentDetails.getAuthorities())
        );

        //when
        //then
        mockMvc.perform(get("/records/children/weekly")
                        .param("date", "2026-01-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.childId").value(3)) // SQL에서 자식 id를 5로 넣는 예시임
                .andExpect(jsonPath("$.weekStartDate").value("2026-01-05"))
                .andExpect(jsonPath("$.weekEndDate").value("2026-01-11"))
                .andExpect(jsonPath("$.dailyAchievements").isMap())
                .andExpect(jsonPath("$.dailyAchievements['MONDAY'].date").value("2026-01-05"))
                .andExpect(jsonPath("$.dailyAchievements['TUESDAY'].date").value("2026-01-06"))
                .andExpect(jsonPath("$.dailyAchievements['WEDNESDAY'].date").value("2026-01-07"))
                .andExpect(jsonPath("$.dailyAchievements['THURSDAY'].date").value("2026-01-08"))
                .andExpect(jsonPath("$.dailyAchievements['FRIDAY'].date").value("2026-01-09"))
                .andExpect(jsonPath("$.dailyAchievements['SATURDAY'].date").value("2026-01-10"))
                .andExpect(jsonPath("$.dailyAchievements['SUNDAY'].date").value("2026-01-11"))
                .andExpect(jsonPath("$.dailyAchievements['MONDAY'].score").isNumber())
                .andExpect(jsonPath("$.dailyAchievements['SUNDAY'].score").isNumber());
    }

}