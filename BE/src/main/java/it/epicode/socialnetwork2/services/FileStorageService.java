package it.epicode.socialnetwork2.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Scrive e cancella file nella cartella uploads/. Sta in un service a parte
 * perche' serve sia a FotoService (upload) sia a PostService (cancellazione
 * di un post con le sue foto) senza creare dipendenze circolari.
 */
@Service
public class FileStorageService {

	// Prefisso con cui i file vengono esposti dal server (vedi UploadConfig)
	public static final String PREFISSO_URL = "/uploads/";

	private final Path cartellaUpload;

	public FileStorageService(@Value("${app.upload.dir}") String cartellaUpload) {
		this.cartellaUpload = Path.of(cartellaUpload).toAbsolutePath();
	}

	// Ritorna l'url pubblico del file (es. "/uploads/3f2a....jpg")
	public String salva(byte[] contenuto, String estensione) {
		// Nome = UUID: due utenti che caricano "foto.jpg" non si sovrascrivono,
		// e un nome inventato dal client non puo' contenere "../"
		String nomeFile = UUID.randomUUID() + estensione;
		try {
			// createDirectories non fallisce se la cartella esiste gia'
			Files.createDirectories(cartellaUpload);
			Files.write(cartellaUpload.resolve(nomeFile), contenuto);
		} catch (IOException e) {
			throw new UncheckedIOException("Impossibile salvare il file", e);
		}
		return PREFISSO_URL + nomeFile;
	}

	public void elimina(String url) {
		if (url == null || !url.startsWith(PREFISSO_URL)) {
			return;
		}
		Path file = cartellaUpload.resolve(url.substring(PREFISSO_URL.length()));
		try {
			Files.deleteIfExists(file);
		} catch (IOException e) {
			// Un file rimasto su disco non giustifica il fallimento della richiesta:
			// il record e' gia' stato cancellato
			file.toFile().deleteOnExit();
		}
	}
}
