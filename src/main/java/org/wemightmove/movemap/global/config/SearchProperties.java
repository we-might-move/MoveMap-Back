package org.wemightmove.movemap.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 자동완성 검색(/facilities/search, /programs/search) 엔진 스위칭 및 리컨실리에이션 설정.
 * <p>
 * engine=db일 때(또는 ES 호출 실패 시) 기존 native 쿼리로 폴백한다(GLOBAL CONSTRAINT §3).
 *
 * @param facility  시설 검색 엔진 설정
 * @param program   프로그램 검색 엔진 설정
 * @param reconcile PG↔ES 정합성 리컨실리에이션 스케줄 설정
 */
@ConfigurationProperties(prefix = "movemap.search")
public record SearchProperties(
        @DefaultValue Facility facility,
        @DefaultValue Program program,
        @DefaultValue Reconcile reconcile
) {

    /**
     * @param engine 검색 엔진: "db" 또는 "es". 기본 "db"
     */
    public record Facility(@DefaultValue("db") String engine) {
    }

    /**
     * @param engine 검색 엔진: "db" 또는 "es". 기본 "db"
     */
    public record Program(@DefaultValue("db") String engine) {
    }

    /**
     * @param cron    리컨실리에이션 스케줄러 cron 표현식. 기본 15분마다({@code 0 &#42;/15 * * * *})
     * @param enabled 리컨실리에이션 활성화 여부. 기본 true
     */
    public record Reconcile(
            @DefaultValue("0 */15 * * * *") String cron,
            @DefaultValue("true") boolean enabled
    ) {
    }
}
