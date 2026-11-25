package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Guarda en disco los archivos adjuntos (CU16) con medidas básicas de seguridad:
 * solo PDF e imágenes, comprobando la firma real del archivo (no solo la extensión), tamaño máximo,
 * nombre aleatorio en disco y sin rutas armadas con datos del usuario (evita path traversal).
 */
@Service
public class ArchivoService {
    private static final Logger log = LoggerFactory.getLogger(ArchivoService.class);

    public record Guardado(String nombreAlmacenado, String tipoContenido) {
    }

    private static final Map<String, String> TIPOS = Map.of(
            "pdf", "application/pdf",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp");

    private final Path directorio;
    private final long maxBytes;

    public ArchivoService(@Value("${app.uploads.dir:uploads}") String directorio,
                          @Value("${app.uploads.max-bytes:10485760}") long maxBytes) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
        try {
            Files.createDirectories(this.directorio);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de archivos " + this.directorio, e);
        }
    }

    public Guardado guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("El archivo está vacío.");
        }
        if (archivo.getSize() > maxBytes) {
            throw new SolicitudInvalidaException("El archivo supera el tamaño máximo de " + (maxBytes / 1024 / 1024) + " MB.");
        }
        String extension = FilenameUtils.getExtension(archivo.getOriginalFilename()).toLowerCase(Locale.ROOT);
        String tipoContenido = TIPOS.get(extension);
        if (tipoContenido == null) {
            throw new SolicitudInvalidaException("Solo se aceptan archivos PDF, JPG, PNG o WEBP.");
        }
        if (!firmaCoincide(archivo, extension)) {
            throw new SolicitudInvalidaException("El contenido del archivo no corresponde a su extensión.");
        }

        String nombreAlmacenado = UUID.randomUUID() + "." + extension;
        Path destino = directorio.resolve(nombreAlmacenado).normalize();
        try (InputStream entrada = archivo.getInputStream()) {
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("No se pudo guardar el archivo {}", nombreAlmacenado, e);
            throw new UncheckedIOException("No se pudo guardar el archivo", e);
        }
        return new Guardado(nombreAlmacenado, tipoContenido);
    }

    public Resource cargar(String nombreAlmacenado) {
        Path ruta = directorio.resolve(FilenameUtils.getName(nombreAlmacenado)).normalize();
        if (!ruta.startsWith(directorio) || !Files.isRegularFile(ruta)) {
            throw new RecursoNoEncontradoException("El archivo ya no está disponible en el servidor.");
        }
        return new FileSystemResource(ruta);
    }

    /** Compara los primeros bytes ("magic numbers") con los del formato esperado. */
    private static boolean firmaCoincide(MultipartFile archivo, String extension) {
        byte[] cabecera = new byte[12];
        int leidos;
        try (InputStream entrada = archivo.getInputStream()) {
            leidos = entrada.readNBytes(cabecera, 0, cabecera.length);
        } catch (IOException e) {
            return false;
        }
        byte[] inicio = Arrays.copyOf(cabecera, leidos);
        return switch (extension) {
            case "pdf" -> empiezaCon(inicio, '%', 'P', 'D', 'F');
            case "png" -> empiezaCon(inicio, 0x89, 'P', 'N', 'G');
            case "jpg", "jpeg" -> empiezaCon(inicio, 0xFF, 0xD8, 0xFF);
            case "webp" -> empiezaCon(inicio, 'R', 'I', 'F', 'F') && leidos >= 12
                    && inicio[8] == 'W' && inicio[9] == 'E' && inicio[10] == 'B' && inicio[11] == 'P';
            default -> false;
        };
    }

    private static boolean empiezaCon(byte[] datos, int... firma) {
        if (datos.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((datos[i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
