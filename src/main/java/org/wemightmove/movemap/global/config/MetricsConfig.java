package org.wemightmove.movemap.global.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer {@link MeterRegistry} 폴백 설정.
 * <p>
 * 이 프로젝트는 actuator 를 도입하지 않으므로(경량 {@code micrometer-core} 만 추가) 자동 구성되는
 * 레지스트리가 없다. {@link SimpleMeterRegistry} 를 기본 빈으로 제공해 검색 메트릭({@code SearchMetrics})이
 * 항상 등록될 곳을 갖게 한다.
 * <p>
 * {@link ConditionalOnMissingBean} 이므로 향후(T7/프로덕션) actuator + prometheus 레지스트리 등
 * 실제 레지스트리가 주입되면 그쪽이 우선하고 이 폴백은 비활성화된다.
 */
@Configuration
public class MetricsConfig {

    @Bean
    @ConditionalOnMissingBean(MeterRegistry.class)
    public MeterRegistry simpleMeterRegistry() {
        return new SimpleMeterRegistry();
    }
}
