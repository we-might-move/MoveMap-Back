package org.wemightmove.movemap.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.wemightmove.movemap.global.util.RedisService;

@Component
@RequiredArgsConstructor
@Log4j2
public class CustomLogoutHandler implements LogoutHandler {

    private final RedisService redisService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("  dd");
        if(authentication != null && authentication.getName() != null) {
            redisService.deleteValues("refreshToken:" + ((CustomUserDetails) authentication.getPrincipal()).getId());
            log.info("refreshToken:" + ((CustomUserDetails) authentication.getPrincipal()).getId());
            SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
            securityContextLogoutHandler.logout(request, response, authentication);
        } else {
        }
    }
}
