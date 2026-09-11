package it.epicode.socialnetwork2.exceptions;

// Tesseract non e' riuscito a leggere l'immagine: 422 (file ricevuto ma non elaborabile)
public class OcrException extends RuntimeException {

	public OcrException(String message) {
		super(message);
	}
}
