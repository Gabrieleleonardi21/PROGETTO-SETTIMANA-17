package it.epicode.socialnetwork2.payloads;

import it.epicode.socialnetwork2.entities.Foto;

import java.util.UUID;

public record FotoResponse(UUID id, String url, String testoOcr, UUID postId) {

	public static FotoResponse from(Foto foto) {
		return new FotoResponse(foto.getId(), foto.getUrl(), foto.getTestoOcr(), foto.getPost().getId());
	}
}
