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
 * 레지스트리가 없다. {@link SimpleMeterRegistry} 를 인메모리 폴백 빈으로 제공해, 다른 레지스트리가
 * 없는 지금 상황에서도 검색 메트릭({@code SearchMetrics})이 항상 등록될 곳을 갖게 한다.
 * <p>
 * 주의: 이 빈은 {@link ConditionalOnMissingBean} 으로 선언되어 있지만, 이 클래스처럼 컴포넌트 스캔으로
 * 등록되는 {@code @Configuration} 빈은 {@code @EnableAutoConfiguration} 으로 지연 등록되는 actuator의
 * 메트릭 자동 구성보다 먼저 평가되는 경우가 있어, 향후 actuator + 실제 레지스트리(예: Prometheus)를
 * 도입하더라도 이 폴백이 자동으로 비활성화된다는 보장이 없다. 따라서 actuator 도입 시점에는 이 빈을
 * 명시적으로 제거하거나, {@code @ConditionalOnMissingBean} 조건이 의도대로 동작하는지 반드시 별도로
 * 검증해야 한다.
 */
@Configuration
public class MetricsConfig {

    @Bean
    @ConditionalOnMissingBean(MeterRegistry.class)
    public MeterRegistry simpleMeterRegistry() {
        return new SimpleMeterRegistry();
    }
}
