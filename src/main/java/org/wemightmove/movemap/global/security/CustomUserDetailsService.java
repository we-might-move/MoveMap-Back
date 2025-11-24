package org.wemightmove.movemap.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.wemightmove.movemap.domain.member.entity.Member;
import org.wemightmove.movemap.domain.member.repository.MemberRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(member.isDeleted()) throw new CustomException(ErrorCode.MEMBER_DELETED);
        return new CustomUserDetails(member);
    }

    public UserDetails loadUserById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(member.isDeleted()) throw new CustomException(ErrorCode.MEMBER_DELETED);
        return new CustomUserDetails(member);
    }
}
