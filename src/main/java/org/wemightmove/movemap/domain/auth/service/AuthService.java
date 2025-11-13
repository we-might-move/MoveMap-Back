package org.wemightmove.movemap.domain.auth.service;

import org.wemightmove.movemap.domain.auth.dto.request.LoginRequest;
import org.wemightmove.movemap.global.jwt.TokenDto;

public interface AuthService {
    TokenDto login(LoginRequest request);
}
