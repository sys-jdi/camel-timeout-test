package org.project.test;

import java.util.concurrent.ExecutionException;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringBootTest
@SpringBootTest(classes = { TestApplication.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class WireMockRouteTest {

    @RegisterExtension
    static WireMockExtension wireMock = new WireMockExtension();
    
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void failOnSendingRequestDueToResilience4jTimeout() throws InterruptedException, ExecutionException {
        final String dummyRequest = "{\"dummyField1\":\"Value of dummy\",\"dummyField2\":\"dummy-value-extended\"}";

        wireMock.stubFor(post("/stub/endpoint")
            .willReturn(aResponse().withStatus(200).withBody("Successful forward").withFixedDelay(1000)));

        final HttpEntity<String> request = new HttpEntity<>(dummyRequest);

        // WHEN
        ResponseEntity<String> response = testRestTemplate.exchange("/test/endpoint", HttpMethod.POST, request, String.class);
        
        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(response.getBody()).isEqualTo("Timeout error");
    }
}