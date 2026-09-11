package it.epicode.socialnetwork2.exceptions;

import it.epicode.socialnetwork2.payloads.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

// Intercetta le eccezioni di tutti i controller e le trasforma in ErrorResponse JSON
@RestControllerAdvice
public class ExceptionsHandler {

	private static final Logger log = LoggerFactory.getLogger(ExceptionsHandler.class);

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleNotFound(NotFoundException e) {
		return ErrorResponse.of(e.getMessage(), HttpStatus.NOT_FOUND.value());
	}

	// 400: regole di business violate (es. email gia' in uso)
	@ExceptionHandler(BadRequestException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleBadRequest(BadRequestException e) {
		return ErrorResponse.of(e.getMessage(), HttpStatus.BAD_REQUEST.value());
	}

	// 400: payload che non passa le annotazioni di validazione (@NotBlank, @Email...)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
		String errori = e.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return ErrorResponse.of(errori, HttpStatus.BAD_REQUEST.value());
	}

	// 400: es. un UUID non valido nel path
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		return ErrorResponse.of("Parametro '" + e.getName() + "' non valido", HttpStatus.BAD_REQUEST.value());
	}

	// 400: multipart senza il campo atteso (es. manca "file")
	@ExceptionHandler(MissingServletRequestPartException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleParteMancante(MissingServletRequestPartException e) {
		return ErrorResponse.of("Manca il campo '" + e.getRequestPartName() + "' nel form", HttpStatus.BAD_REQUEST.value());
	}

	// 404: nessuna rotta/risorsa statica per l'url richiesto (senza questo finirebbe nel 500 generico)
	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleRottaInesistente(NoResourceFoundException e) {
		return ErrorResponse.of("Rotta non trovata: /" + e.getResourcePath(), HttpStatus.NOT_FOUND.value());
	}

	// 401: token mancante, non valido o scaduto (lanciata dal JwtFilter) o credenziali errate
	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleUnauthorized(UnauthorizedException e) {
		return ErrorResponse.of(e.getMessage(), HttpStatus.UNAUTHORIZED.value());
	}

	// 403: autenticato ma senza il ruolo richiesto da @PreAuthorize
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorResponse handleAccessDenied(AccessDeniedException e) {
		return ErrorResponse.of("Non hai i permessi necessari per eseguire questa operazione",
				HttpStatus.FORBIDDEN.value());
	}

	// 413: file oltre il limite di spring.servlet.multipart.max-file-size
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
	public ErrorResponse handleFileTroppoGrande(MaxUploadSizeExceededException e) {
		return ErrorResponse.of("Il file supera la dimensione massima consentita (10MB)",
				HttpStatus.PAYLOAD_TOO_LARGE.value());
	}

	// 422: immagine ricevuta ma Tesseract non l'ha potuta elaborare
	@ExceptionHandler(OcrException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ErrorResponse handleOcr(OcrException e) {
		return ErrorResponse.of(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value());
	}

	// 502: il problema e' a monte (Google non risponde o chiave mancante), non nella richiesta del client
	@ExceptionHandler(GeocodingException.class)
	@ResponseStatus(HttpStatus.BAD_GATEWAY)
	public ErrorResponse handleGeocoding(GeocodingException e) {
		return ErrorResponse.of(e.getMessage(), HttpStatus.BAD_GATEWAY.value());
	}

	// 409: vincolo unique violato a livello DB (rete di sicurezza se i controlli nel service non bastano)
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleDataIntegrity(DataIntegrityViolationException e) {
		return ErrorResponse.of("Operazione in conflitto con dati già esistenti", HttpStatus.CONFLICT.value());
	}

	// 500: qualsiasi errore non previsto
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleGeneric(Exception e) {
		// stack trace nel log del server, al client arriva solo un messaggio generico
		log.error("Errore non gestito", e);
		return ErrorResponse.of("Si è verificato un errore imprevisto, riprova più tardi",
				HttpStatus.INTERNAL_SERVER_ERROR.value());
	}
}
