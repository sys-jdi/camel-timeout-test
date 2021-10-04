package org.project.test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.fail;

@CamelSpringBootTest
@SpringBootTest(classes = { TestApplication.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TimeoutVerificationTest {

    @RegisterExtension
    static WireMockExtension wireMock = new WireMockExtension();

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void failOnSendingRequestDueToResilience4jTimeout() throws Exception {
        final String dummyRequest = "{\"dummyField1\":\"Value of dummy\",\"dummyField2\":\"dummy-value-extended\"}";

        wireMock.stubFor(get("/stub/endpoint")
            .willReturn(aResponse().withStatus(200).withBody("Successful forward").withFixedDelay(1000)));

        final HttpEntity<String> request = new HttpEntity<>(dummyRequest);

        // WHEN
        TimeLimiterConfig config = TimeLimiterConfig.custom()
            .cancelRunningFuture(true)
            .timeoutDuration(Duration.ofMillis(500))
            .build();

        TimeLimiter timeLimiter = TimeLimiter.of("tester", config);

        CompletableFuture<ResponseEntity<String>> supplier = CompletableFuture.supplyAsync(() -> testRestTemplate.exchange("http://localhost:9050/stub/endpoint", HttpMethod.GET, request, String.class));

        try {
            ResponseEntity<String> response = timeLimiter.executeFutureSupplier(() -> supplier);
            fail("Expected exception!");
        } catch (TimeoutException te)  {
            te.printStackTrace();
        }
    }
}
