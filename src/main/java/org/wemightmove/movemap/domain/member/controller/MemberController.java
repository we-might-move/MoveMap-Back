package org.wemightmove.movemap.domain.member.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Member")
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
}
