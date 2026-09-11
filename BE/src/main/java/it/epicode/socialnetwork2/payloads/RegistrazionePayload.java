package it.epicode.socialnetwork2.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Dati in ingresso per la registrazione: il ruolo non c'e', e' sempre MEMBER
public record RegistrazionePayload(
		@NotBlank(message = "Lo username è obbligatorio")
		@Size(min = 3, max = 50, message = "Lo username deve essere tra 3 e 50 caratteri")
		String username,

		@NotBlank(message = "Il nome completo è obbligatorio")
		@Size(max = 75, message = "Il nome completo non può superare i 75 caratteri")
		String nomeCompleto,

		@NotBlank(message = "L'email è obbligatoria")
		@Email(message = "L'email inserita non è in un formato valido")
		@Size(max = 75, message = "L'email non può superare i 75 caratteri")
		String email,

		@NotBlank(message = "La password è obbligatoria")
		@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
				message = "La password deve contenere almeno 8 caratteri, con almeno una lettera e un numero")
		String password
) {
}
