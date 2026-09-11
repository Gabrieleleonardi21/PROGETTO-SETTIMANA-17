package it.epicode.socialnetwork2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Espone la cartella degli upload come file statici: una Foto salvata in
 * uploads/abc.jpg diventa raggiungibile a http://localhost:3001/uploads/abc.jpg.
 * La rotta e' in permitAll perche' un <img src> del browser non puo' mandare
 * l'header Authorization.
 */
@Configuration
public class UploadConfig implements WebMvcConfigurer {

	private final Path cartellaUpload;

	public UploadConfig(@Value("${app.upload.dir}") String cartellaUpload) {
		this.cartellaUpload = Path.of(cartellaUpload).toAbsolutePath();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/**")
				// lo slash finale e' obbligatorio, altrimenti Spring non lo tratta come cartella
				.addResourceLocations("file:" + cartellaUpload + "/");
	}
}
