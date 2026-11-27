package com.shoploc.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.shoploc.order.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
