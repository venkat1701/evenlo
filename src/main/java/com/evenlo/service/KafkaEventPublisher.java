package com.evenlo.service;

import com.evenlo.dto.kafka.BookingConfirmedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventPublisher {
	public static final String TOPIC_BOOKING_CONFIRMED = "evenlo.booking.confirmed";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	public void publishBookingConfirmed(BookingConfirmedEvent event) {
		try {
			kafkaTemplate.send(TOPIC_BOOKING_CONFIRMED, event.bookingId().toString(), objectMapper.writeValueAsString(event));
		} catch (Exception ex) {
			throw new IllegalStateException("kafka_publish_failed", ex);
		}
	}
}
