package it.epicode.socialnetwork2.payloads;

import it.epicode.socialnetwork2.entities.Foto;
import it.epicode.socialnetwork2.entities.Post;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Vista di un post per il client. Non si serializza l'entita' Post perche'
 * l'autore e' LAZY e con open-in-view=false esploderebbe fuori dal service.
 */
public record PostResponse(
		UUID id,
		String testo,
		Instant dataPubblicazione,
		UtenteResponse autore,
		PosizioneResponse posizione,
		List<FotoResponse> foto
) {

	// Le foto arrivano da fuori: il service le carica con una sola query per tutta la pagina
	public static PostResponse from(Post post, List<Foto> foto) {
		return new PostResponse(
				post.getId(),
				post.getTesto(),
				post.getDataPubblicazione(),
				UtenteResponse.from(post.getUtente()),
				PosizioneResponse.from(post.getPosizione()),
				foto.stream().map(FotoResponse::from).toList()
		);
	}
}
