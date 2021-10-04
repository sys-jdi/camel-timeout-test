package org.project.test;

import org.apache.camel.model.Resilience4jConfigurationDefinition;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class WireMockRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("servlet:/test/endpoint").routeId("test-servlet-endpoint")
            .circuitBreaker()
                .resilience4jConfiguration(buildResilience4jConfigurationDefinition())
                .to("http://localhost:9050/stub/endpoint?bridgeEndpoint=true")
            .endCircuitBreaker();
    }    


    private Resilience4jConfigurationDefinition buildResilience4jConfigurationDefinition() {       
        final Resilience4jConfigurationDefinition definition = new Resilience4jConfigurationDefinition();

        definition.bulkheadEnabled(true)
            .bulkheadMaxConcurrentCalls(25)
            .bulkheadMaxWaitDuration(0);

        definition.timeoutEnabled(true)
            .timeoutCancelRunningFuture(true)
            .timeoutDuration(500);

        definition
            .minimumNumberOfCalls(10)
            .slidingWindowType("COUNT_BASED")
            .slidingWindowSize(100)
            .failureRateThreshold(100)
            .waitDurationInOpenState(60);
       
        return definition;
    } 
}
