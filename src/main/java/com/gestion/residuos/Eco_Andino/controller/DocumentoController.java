package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.DocumentoDtos.DocumentoDto;
import com.gestion.residuos.Eco_Andino.dto.DocumentoDtos.DocumentoRequest;
import com.gestion.residuos.Eco_Andino.dto.DocumentoDtos.RechazoRequest;
import com.gestion.residuos.Eco_Andino.dto.Opcion;
import com.gestion.residuos.Eco_Andino.service.DocumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Documentos y evidencias (CU16). */
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {
    private static final String REVISAR =
            "hasAuthority('DOCUMENTO_GESTIONAR') and hasAnyAuthority('GENERACION_VALIDAR', 'RECOLECCION_PROGRAMAR')";

    private final DocumentoService documentoService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DOCUMENTO_GESTIONAR', 'AUDITORIA_CONSULTAR')")
    public List<DocumentoDto> listar() {
        return documentoService.listar();
    }

    @GetMapping("/referencias")
    @PreAuthorize("hasAuthority('DOCUMENTO_GESTIONAR')")
    public List<Opcion> referencias() {
        return documentoService.referencias();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('DOCUMENTO_GESTIONAR')")
    public DocumentoDto adjuntar(@Valid @RequestPart("datos") DocumentoRequest datos,
                                 @RequestPart(value = "archivo", required = false) MultipartFile archivo) {
        return documentoService.adjuntar(datos, archivo);
    }

    @PatchMapping("/{id}/validar")
    @PreAuthorize(REVISAR)
    public DocumentoDto validar(@PathVariable Integer id) {
        return documentoService.validar(id);
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize(REVISAR)
    public DocumentoDto rechazar(@PathVariable Integer id, @Valid @RequestBody RechazoRequest datos) {
        return documentoService.rechazar(id, datos.motivo());
    }

    @GetMapping("/{id}/archivo")
    @PreAuthorize("hasAnyAuthority('DOCUMENTO_GESTIONAR', 'AUDITORIA_CONSULTAR')")
    public ResponseEntity<Resource> descargar(@PathVariable Integer id) {
        DocumentoService.Descarga descarga = documentoService.descargar(id);
        MediaType tipo = descarga.tipoContenido() == null
                ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(descarga.tipoContenido());
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(descarga.nombreArchivo(), StandardCharsets.UTF_8).build().toString())
                // Evita que el navegador interprete el archivo como otro tipo (ej. HTML)
                .header("X-Content-Type-Options", "nosniff")
                .body(descarga.recurso());
    }
}
