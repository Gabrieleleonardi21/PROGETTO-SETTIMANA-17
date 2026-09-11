package it.epicode.socialnetwork2.payloads;

import it.epicode.socialnetwork2.entities.Documento;

import java.time.Instant;
import java.util.UUID;

public record DocumentoResponse(UUID id, String titolo, String url, Long peso, String testoOcr, Instant dataCaricamento) {

	public static DocumentoResponse from(Documento documento) {
		return new DocumentoResponse(documento.getId(), documento.getTitolo(), documento.getUrl(),
				documento.getPeso(), documento.getTestoOcr(), documento.getDataCaricamento());
	}
}
