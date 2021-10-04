# camel-timeout-test

This project implements a very simple test route configured with both a Resilience4j Bulkhead and TimeLimiter.

Additionally two tests have been implemented, one attempting to use the application route to call a WireMock instance and a second test calling the WireMock instance directly through a custom configured TimeLimiter.
Both tests are configured to wait a maximum of 500ms and the WireMock instance is configured to respond after 1000ms.

Disabling or commenting out the Bulkhead in ```WireMockRouteBuilder.java``` makes the timeout take effect.

Run ```mvn verify``` to see the results.