package org.wemightmove.movemap.domain.program.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.program.dto.request.ProgramMarkerRequest;
import org.wemightmove.movemap.domain.program.dto.response.ProgramMarkerResponse;
import org.wemightmove.movemap.domain.program.service.ProgramQueryService;
import org.wemightmove.movemap.global.enums.FacilityType;
import org.wemightmove.movemap.global.security.CustomUserDetails;

import java.util.List;

@RestController
@Tag(name = "Program")
@RequiredArgsConstructor
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramQueryService programQueryService;

    @Operation(summary = "프로그램 마커 조회(초기)")
    @GetMapping("/markers/initial")
    public ResponseEntity<ProgramMarkerResponse> getInitialMarkers(
            @AuthenticationPrincipal CustomUserDetails member
    ) {
        ProgramMarkerResponse response = programQueryService.getMarkers(member.getId());
        return ResponseEntity.ok(response);
    }
}
