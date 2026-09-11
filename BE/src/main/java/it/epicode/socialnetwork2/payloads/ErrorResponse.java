package it.epicode.socialnetwork2.payloads;

import java.time.LocalDateTime;

// Formato unico di tutti gli errori restituiti dall'API
public record ErrorResponse(String message, int status, LocalDateTime timestamp) {

	public static ErrorResponse of(String message, int status) {
		return new ErrorResponse(message, status, LocalDateTime.now());
	}
}
