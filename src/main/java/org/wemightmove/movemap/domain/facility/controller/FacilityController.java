package org.wemightmove.movemap.domain.facility.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.wemightmove.movemap.domain.facility.service.FacilityCommandService;
import org.wemightmove.movemap.global.security.CustomUserDetails;

@RestController
@Tag(name = "Facility")
@RequiredArgsConstructor
@RequestMapping("/facilities")
public class FacilityController {
    private final FacilityCommandService facilityCommandService;

    @PostMapping("/{id}/bookmarks")
    public ResponseEntity<Void> bookmarkFacility(@AuthenticationPrincipal CustomUserDetails member, @PathVariable("id") Long facilityId) {
        facilityCommandService.addBookmarkFacility(member.getId(), facilityId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/bookmarks")
    public ResponseEntity<Void> deleteBookmarkFacility(@AuthenticationPrincipal CustomUserDetails member, @PathVariable("id") Long facilityId) {
        facilityCommandService.deleteBookmarkFacility(member.getId(), facilityId);
        return ResponseEntity.noContent().build();
    }
}
