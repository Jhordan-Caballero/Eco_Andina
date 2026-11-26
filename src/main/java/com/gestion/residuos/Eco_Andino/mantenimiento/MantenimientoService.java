package com.gestion.residuos.Eco_Andino.mantenimiento;

import com.gestion.residuos.Eco_Andino.repository.DocumentoRepository;
import com.gestion.residuos.Eco_Andino.repository.GestorRepository;
import com.gestion.residuos.Eco_Andino.repository.TransportistaRepository;
import com.gestion.residuos.Eco_Andino.service.ArchivoService;
import com.gestion.residuos.Eco_Andino.service.VistaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Revisiones de mantenimiento que no cambian datos del negocio: solo detectan problemas y los dejan en el log
 * (logs/ecoandina-alertas.log) para que el administrador actúe. Ver docs/plan-mantenimiento.md.
 */
@Service
@RequiredArgsConstructor
public class MantenimientoService {
    private static final Logger log = LoggerFactory.getLogger(MantenimientoService.class);

    /** Archivos registrados en la BD que no están en disco, y archivos en disco que ningún documento usa. */
    public record VerificacionArchivos(int registrados, int enDisco, List<String> faltantes, List<String> huerfanos) {
        public boolean correcta() {
            return faltantes.isEmpty() && huerfanos.isEmpty();
        }
    }

    /** Empresa con autorización vencida o por vencer (30 días o menos). */
    public record Vigencia(String tipo, String empresa, String autorizacion, LocalDate vigencia, String estado) {
    }

    private final DocumentoRepository documentoRepository;
    private final TransportistaRepository transportistaRepository;
    private final GestorRepository gestorRepository;
    private final ArchivoService archivoService;
    private final VistaService vistas;

    /**
     * Compara los documentos de la BD con la carpeta de adjuntos. Un archivo faltante impide descargar un
     * manifiesto o certificado (se debe restaurar del backup); uno huérfano solo ocupa espacio.
     * No borra nada: el borrado de huérfanos se hace a mano después de revisar.
     */
    @Transactional(readOnly = true)
    public VerificacionArchivos verificarArchivos() {
        Set<String> registrados = new HashSet<>(documentoRepository.findRutasArchivo());
        Set<String> enDisco = archivoService.archivosEnDisco();

        Set<String> faltantes = new TreeSet<>(registrados);
        faltantes.removeAll(enDisco);
        Set<String> huerfanos = new TreeSet<>(enDisco);
        huerfanos.removeAll(registrados);

        VerificacionArchivos resultado = new VerificacionArchivos(registrados.size(), enDisco.size(),
                List.copyOf(faltantes), List.copyOf(huerfanos));
        if (!faltantes.isEmpty()) {
            log.error("Verificación de archivos: {} adjuntos registrados no están en disco (restaurar del backup): {}",
                    faltantes.size(), faltantes);
        }
        if (!huerfanos.isEmpty()) {
            log.warn("Verificación de archivos: {} archivos en disco sin documento asociado: {}",
                    huerfanos.size(), huerfanos);
        }
        if (resultado.correcta()) {
            log.info("Verificación de archivos: {} adjuntos, todo en orden", registrados.size());
        }
        return resultado;
    }

    /** Transportistas y gestores cuya autorización ya venció o vence en 30 días o menos (RN de CU15). */
    @Transactional(readOnly = true)
    public List<Vigencia> revisarVigencias() {
        List<Vigencia> alertas = new ArrayList<>();
        transportistaRepository.findAll().forEach(t -> agregarSiCorresponde(alertas, "Transportista",
                t.getEmpresa(), t.getAutorizacion(), t.getVigencia()));
        gestorRepository.findAll().forEach(g -> agregarSiCorresponde(alertas, "Gestor",
                g.getEmpresa(), g.getAutorizacion(), g.getVigencia()));

        alertas.forEach(a -> log.warn("Vigencia: {} {} ({}) {} el {}", a.tipo(), a.empresa(), a.autorizacion(),
                "VENCIDA".equals(a.estado()) ? "venció" : "vence", a.vigencia()));
        if (alertas.isEmpty()) {
            log.info("Vigencias: todas las autorizaciones de transportistas y gestores están vigentes");
        }
        return alertas;
    }

    private void agregarSiCorresponde(List<Vigencia> alertas, String tipo, String empresa, String autorizacion,
                                      LocalDate vigencia) {
        if (vigencia == null) {
            return;
        }
        String estado = vistas.estadoVigencia(vigencia);
        if (!"VIGENTE".equals(estado)) {
            alertas.add(new Vigencia(tipo, empresa, autorizacion, vigencia, estado));
        }
    }
}
