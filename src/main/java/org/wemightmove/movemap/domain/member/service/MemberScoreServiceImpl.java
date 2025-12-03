package org.wemightmove.movemap.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.member.repository.MemberScoreRepository;

@Service
@RequiredArgsConstructor
public class MemberScoreServiceImpl implements MemberScoreService{

    private final MemberScoreRepository memberScoreRepository;
}
