package it.epicode.socialnetwork2.payloads;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * L'autore non c'e': e' sempre l'utente del token.
 * La posizione si puo' dare in due modi, entrambi facoltativi:
 * - nomeLuogo: un indirizzo, che il back-end geocodifica
 * - latitudine + longitudine: un punto scelto sulla mappa (ha la precedenza)
 */
public record PostPayload(
		@NotBlank(message = "Il testo del post è obbligatorio")
		@Size(max = 200, message = "Il testo non può superare i 200 caratteri")
		String testo,

		@Size(max = 100, message = "Il nome del luogo non può superare i 100 caratteri")
		String nomeLuogo,

		@DecimalMin(value = "-90.0", message = "La latitudine deve essere tra -90 e 90")
		@DecimalMax(value = "90.0", message = "La latitudine deve essere tra -90 e 90")
		Double latitudine,

		@DecimalMin(value = "-180.0", message = "La longitudine deve essere tra -180 e 180")
		@DecimalMax(value = "180.0", message = "La longitudine deve essere tra -180 e 180")
		Double longitudine
) {
}
