package com.evenlo.service;

import com.evenlo.dto.kafka.BookingConfirmedEvent;
import com.evenlo.model.User;
import com.evenlo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BookingConfirmedConsumer {
	private final ObjectMapper objectMapper;
	private final UserRepository userRepository;
	private final EmailService emailService;

	public BookingConfirmedConsumer(ObjectMapper objectMapper, UserRepository userRepository, EmailService emailService) {
		this.objectMapper = objectMapper;
		this.userRepository = userRepository;
		this.emailService = emailService;
	}

	@KafkaListener(topics = KafkaEventPublisher.TOPIC_BOOKING_CONFIRMED)
	public void onMessage(String payload) throws Exception {
		BookingConfirmedEvent event = objectMapper.readValue(payload, BookingConfirmedEvent.class);
		User user = userRepository.findById(event.userId()).orElse(null);
		if (user == null) {
			return;
		}
		String subject = "Booking confirmed";
		String text = "Your booking " + event.bookingId() + " is confirmed.";
		emailService.sendBookingConfirmed(user.getEmail(), subject, text);
	}
}
