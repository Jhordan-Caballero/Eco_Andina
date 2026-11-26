package com.gestion.residuos.Eco_Andino.monitoreo;

import com.gestion.residuos.Eco_Andino.service.ArchivoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Salud de la carpeta de archivos adjuntos (CU16), visible en /actuator/health como "archivos".
 * Sin esta carpeta no se pueden subir manifiestos ni certificados, y sin ellos no se cierra ninguna orden
 * (RNF003), así que su estado es tan importante como el de la base de datos.
 * Queda DOWN si la carpeta no existe, no se puede escribir o le queda menos espacio libre que el mínimo.
 */
@Component("archivos")
public class ArchivosHealthIndicator implements HealthIndicator {
    private final ArchivoService archivoService;
    private final long minimoLibreMb;

    public ArchivosHealthIndicator(ArchivoService archivoService,
                                   @Value("${app.monitoreo.espacio-minimo-mb:500}") long minimoLibreMb) {
        this.archivoService = archivoService;
        this.minimoLibreMb = minimoLibreMb;
    }

    @Override
    public Health health() {
        Path carpeta = archivoService.directorio();
        if (!Files.isDirectory(carpeta)) {
            return Health.down().withDetail("carpeta", carpeta.toString())
                    .withDetail("error", "La carpeta de archivos no existe").build();
        }
        if (!Files.isWritable(carpeta)) {
            return Health.down().withDetail("carpeta", carpeta.toString())
                    .withDetail("error", "Sin permiso de escritura en la carpeta de archivos").build();
        }
        File disco = carpeta.toFile();
        long libreMb = disco.getUsableSpace() / 1024 / 1024;
        long totalMb = disco.getTotalSpace() / 1024 / 1024;
        var estado = libreMb < minimoLibreMb ? Health.down() : Health.up();
        return estado.withDetail("carpeta", carpeta.toString())
                .withDetail("archivos", archivoService.archivosEnDisco().size())
                .withDetail("espacioLibreMB", libreMb)
                .withDetail("espacioTotalMB", totalMb)
                .withDetail("minimoLibreMB", minimoLibreMb)
                .build();
    }
}
