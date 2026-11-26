package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Validación de archivos adjuntos (CU16): tipo real, tamaño y nombres seguros en disco. */
class ArchivoServiceTest {
    private static final byte[] PDF = "%PDF-1.7\n contenido de prueba".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @TempDir
    Path carpeta;

    private ArchivoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new ArchivoService(carpeta.toString(), 1024);
    }

    @Test
    @DisplayName("Guarda un PDF válido con nombre aleatorio y lo puede volver a cargar")
    void guardaPdfValido() {
        ArchivoService.Guardado guardado = servicio.guardar(
                new MockMultipartFile("archivo", "manifiesto.pdf", "application/pdf", PDF));

        assertEquals("application/pdf", guardado.tipoContenido());
        assertTrue(guardado.nombreAlmacenado().endsWith(".pdf"));
        assertFalse(guardado.nombreAlmacenado().contains("manifiesto"), "el nombre original no se usa en disco");
        assertTrue(Files.exists(carpeta.resolve(guardado.nombreAlmacenado())));
        assertTrue(servicio.cargar(guardado.nombreAlmacenado()).exists());
        assertEquals(1, servicio.archivosEnDisco().size());
    }

    @Test
    @DisplayName("Acepta imágenes PNG")
    void aceptaPng() {
        ArchivoService.Guardado guardado = servicio.guardar(
                new MockMultipartFile("archivo", "foto.PNG", "image/png", PNG));
        assertEquals("image/png", guardado.tipoContenido());
    }

    @Test
    @DisplayName("Rechaza un archivo cuyo contenido no corresponde a la extensión")
    void rechazaExtensionFalsa() {
        MockMultipartFile ejecutableRenombrado = new MockMultipartFile("archivo", "certificado.pdf",
                "application/pdf", new byte[]{'M', 'Z', 0, 0, 1, 2, 3});
        SolicitudInvalidaException error = assertThrows(SolicitudInvalidaException.class,
                () -> servicio.guardar(ejecutableRenombrado));
        assertTrue(error.getMessage().contains("no corresponde"));
    }

    @Test
    @DisplayName("Rechaza extensiones no permitidas")
    void rechazaExtensionNoPermitida() {
        assertThrows(SolicitudInvalidaException.class, () -> servicio.guardar(
                new MockMultipartFile("archivo", "script.exe", "application/octet-stream", PDF)));
    }

    @Test
    @DisplayName("Rechaza archivos más grandes que el máximo")
    void rechazaArchivoGrande() {
        byte[] grande = new byte[2048];
        System.arraycopy(PDF, 0, grande, 0, PDF.length);
        assertThrows(SolicitudInvalidaException.class, () -> servicio.guardar(
                new MockMultipartFile("archivo", "grande.pdf", "application/pdf", grande)));
    }

    @Test
    @DisplayName("Rechaza archivos vacíos")
    void rechazaVacio() {
        assertThrows(SolicitudInvalidaException.class, () -> servicio.guardar(
                new MockMultipartFile("archivo", "vacio.pdf", "application/pdf", new byte[0])));
    }

    @Test
    @DisplayName("No permite salir de la carpeta de adjuntos (path traversal)")
    void evitaPathTraversal() throws Exception {
        Path secreto = carpeta.getParent().resolve("secreto-" + System.nanoTime() + ".txt");
        Files.writeString(secreto, "no debe leerse");
        try {
            assertThrows(RecursoNoEncontradoException.class,
                    () -> servicio.cargar("../" + secreto.getFileName()));
        } finally {
            Files.deleteIfExists(secreto);
        }
    }
}
