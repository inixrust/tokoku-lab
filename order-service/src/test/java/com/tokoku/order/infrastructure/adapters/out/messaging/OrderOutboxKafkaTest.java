package com.tokoku.order.infrastructure.adapters.out.messaging;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.tokoku.order.application.port.in.PlaceOrderUseCase;
import com.tokoku.order.application.port.in.PlaceOrderUseCase.PlaceOrderCommand;
import com.tokoku.order.domain.model.Availability;
import com.tokoku.order.domain.model.Order;
import com.tokoku.order.domain.model.OrderLine;
import com.tokoku.order.domain.port.out.StockCheck;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

/**
 * L7.1 langkah 2: order + outbox dalam satu transaction, lalu relay mengirim OrderPlaced ke Kafka.
 * Database dan broker berasal dari Testcontainers, bukan dari compose.
 */
@Testcontainers
@SpringBootTest(properties = "spring.kafka.consumer.group-id=order-service-test")
class OrderOutboxKafkaTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	@Container
	@ServiceConnection
	static KafkaContainer kafka = new KafkaContainer("apache/kafka:4.2.2");

	@MockitoBean
	StockCheck stockCheck;

	@Autowired
	PlaceOrderUseCase placeOrder;

	@Test
	void placedOrderIsPublishedToOrdersTopic() {
		given(stockCheck.check(anyLong(), anyInt())).willReturn(Availability.AVAILABLE);

		Order order = placeOrder.place(new PlaceOrderCommand("alice", List.of(new OrderLine(1, 2))));

		try (KafkaConsumer<String, String> consumer = testConsumer()) {
			consumer.subscribe(List.of("orders"));
			ConsumerRecord<String, String> record = pollUntilKey(consumer, String.valueOf(order.id()));

			assertThat(record.headers().lastHeader("eventType").value())
				.asString(StandardCharsets.UTF_8)
				.isEqualTo("OrderPlaced");
			assertThat(record.value()).contains("\"orderId\":" + order.id());
		}
	}

	private static KafkaConsumer<String, String> testConsumer() {
		return new KafkaConsumer<>(Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
				ConsumerConfig.GROUP_ID_CONFIG, "test-reader", ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
				new StringDeserializer(), new StringDeserializer());
	}

	private static ConsumerRecord<String, String> pollUntilKey(KafkaConsumer<String, String> consumer, String key) {
		long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
		while (System.nanoTime() < deadline) {
			for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
				if (key.equals(record.key())) {
					return record;
				}
			}
		}
		throw new AssertionError("No record with key " + key + " on topic orders within 20 s");
	}

}
