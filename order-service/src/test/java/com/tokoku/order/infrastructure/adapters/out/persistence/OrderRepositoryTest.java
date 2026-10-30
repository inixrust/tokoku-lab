package com.tokoku.order.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/** Slice JPA dengan PostgreSQL asli (slide 100), bukan H2. Flyway menjalankan migrasi yang sama dengan produksi. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class OrderRepositoryTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	OrderJpaRepository orders;

	@Test
	@Sql("/seed/orders.sql")
	void findsOnlyOrdersOfThatCustomer() {
		var page = orders.findByCustomerId("cust-7", PageRequest.of(0, 20));

		assertThat(page.getTotalElements()).isEqualTo(2);
		assertThat(page.getContent()).allMatch(order -> order.getCustomerId().equals("cust-7"));
	}

	@Test
	@Sql("/seed/orders.sql")
	void ownershipQueryHidesOrdersOfOtherCustomers() {
		assertThat(orders.findByIdAndCustomerId(9003L, "cust-7")).isEmpty();
		assertThat(orders.findByIdAndCustomerId(9003L, "cust-8")).isPresent();
	}

}
