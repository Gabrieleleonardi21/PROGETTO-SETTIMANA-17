package it.epicode.socialnetwork2.config;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class OcrConfig {

	// PROTOTYPE: Tesseract NON e' thread-safe, ogni richiesta deve avere la sua istanza
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public ITesseract tesseract(@Value("${app.ocr.datapath}") String datapath,
	                            @Value("${app.ocr.native-lib-path}") String nativeLibPath,
	                            @Value("${app.ocr.default-language}") String linguaDefault) {
		// Dove sta libtesseract.dylib (Homebrew su Apple Silicon): Tess4J la carica via JNA
		System.setProperty("jna.library.path", nativeLibPath);

		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath(datapath); // cartella dei file .traineddata
		tesseract.setLanguage(linguaDefault);
		return tesseract;
	}
}
