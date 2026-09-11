package it.epicode.socialnetwork2.services;

import it.epicode.socialnetwork2.exceptions.BadRequestException;
import it.epicode.socialnetwork2.exceptions.OcrException;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Estrae il testo da un'immagine o da un PDF. Lavora sui byte: non sa da
 * dove arrivano e non tocca il database.
 */
@Service
public class OcrService {

	// ObjectProvider e non ITesseract diretto: il bean e' prototype, quindi va
	// chiesto al momento dell'uso. Iniettandolo in questo service (singleton)
	// ne riceveremmo UNA sola istanza riusata per sempre.
	private final ObjectProvider<ITesseract> tesseractProvider;

	public OcrService(ObjectProvider<ITesseract> tesseractProvider) {
		this.tesseractProvider = tesseractProvider;
	}

	// Solo immagini (foto dei post)
	public String estraiTesto(byte[] contenuto) {
		BufferedImage immagine = leggiComeImmagine(contenuto);
		if (immagine == null) {
			throw new BadRequestException("Il file non è un'immagine leggibile (jpg, png, gif, bmp)");
		}
		return ocrDaImmagine(immagine);
	}

	// Immagini o PDF (documenti del profilo): per il PDF Tess4J vuole un File
	public String estraiTesto(byte[] contenuto, String estensione) {
		BufferedImage immagine = leggiComeImmagine(contenuto);
		if (immagine != null) {
			return ocrDaImmagine(immagine);
		}
		if (".pdf".equals(estensione)) {
			return ocrDaFile(contenuto, estensione);
		}
		throw new BadRequestException("Il file non è un'immagine o un PDF leggibile");
	}

	private String ocrDaImmagine(BufferedImage immagine) {
		try {
			return tesseractProvider.getObject().doOCR(appiattisciTrasparenza(immagine)).trim();
		} catch (TesseractException e) {
			throw new OcrException("OCR fallito: " + e.getMessage());
		}
	}

	// Tess4J legge i PDF da File (li rende in pagine con PDFBox): scriviamo un
	// temporaneo e lo cancelliamo subito dopo, qualunque cosa succeda
	private String ocrDaFile(byte[] contenuto, String estensione) {
		Path temporaneo = null;
		try {
			temporaneo = Files.createTempFile("ocr-", estensione);
			Files.write(temporaneo, contenuto);
			return tesseractProvider.getObject().doOCR(temporaneo.toFile()).trim();
		} catch (TesseractException e) {
			throw new OcrException("OCR fallito: " + e.getMessage());
		} catch (IOException e) {
			throw new OcrException("Impossibile scrivere il file temporaneo per l'OCR");
		} finally {
			eliminaTemporaneo(temporaneo);
		}
	}

	private BufferedImage leggiComeImmagine(byte[] contenuto) {
		try {
			return ImageIO.read(new ByteArrayInputStream(contenuto));
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Ridisegna l'immagine su fondo bianco togliendo il canale alpha.
	 * Un PNG trasparente arriva a Tesseract con l'alpha letto come nero:
	 * testo nero su fondo nero e caratteri a caso in uscita.
	 */
	private BufferedImage appiattisciTrasparenza(BufferedImage originale) {
		if (originale.getTransparency() == Transparency.OPAQUE) {
			return originale;
		}

		BufferedImage senzaAlpha = new BufferedImage(
				originale.getWidth(), originale.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D grafica = senzaAlpha.createGraphics();
		grafica.setColor(Color.WHITE);
		grafica.fillRect(0, 0, originale.getWidth(), originale.getHeight());
		grafica.drawImage(originale, 0, 0, null);
		grafica.dispose(); // libera le risorse native della grafica
		return senzaAlpha;
	}

	private void eliminaTemporaneo(Path temporaneo) {
		if (temporaneo == null) {
			return;
		}
		try {
			Files.deleteIfExists(temporaneo);
		} catch (IOException e) {
			// un temporaneo rimasto non giustifica il fallimento della richiesta
			temporaneo.toFile().deleteOnExit();
		}
	}
}
