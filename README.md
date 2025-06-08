# java-microservice

This branch is to record a failed attempt to make `order-service`'s endpoints read JWT tokens. 

Open `OrderController.java` to see `GET /api/orders/{id}` and `POST /api/orders` parse a JWT given in an API request. The endpoints can parse JWTs, but I couldn't find how to adjust the test files for that. To parse JWTs, `@AuthenticationPrincipal` is necessary, and the annotation requires the `spring.security.oauth2.resourceserver.jwt.issuer-uri` property to be set in application.yml. If this property is missing, none of the test cases (including OrderServiceApplicationTests) will run. Running test files results in the "IllegalState Failed to load ApplicationContext" error. However, relying on an external OAuth2 server isn't suitable for unit/integration tests. I haven't found a way to make the tests run without an actual OAuth2 server configured via that property. 
