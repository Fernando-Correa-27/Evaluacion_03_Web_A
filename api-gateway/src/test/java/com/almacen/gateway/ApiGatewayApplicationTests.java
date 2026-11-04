package com.almacen.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.cloud.config.enabled=false", "spring.cloud.discovery.enabled=false", "eureka.client.enabled=false", "eureka.client.register-with-eureka=false", "eureka.client.fetch-registry=false"})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
