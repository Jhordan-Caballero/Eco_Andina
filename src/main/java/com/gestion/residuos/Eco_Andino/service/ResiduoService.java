package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.entity.Residuo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import java.io.StringWriter;
import java.io.IOException;
import java.util.List;

public class ResiduoService {

    public String exportarCatalogoCsv(List<Residuo> residuosActivos) throws IOException {
        StringWriter writer = new StringWriter();

        // Configuración y escritura con commons-csv
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader
                ("ID", "Código", "Nombre", "Peligrosidad"))) {
            for (Residuo residuo : residuosActivos) {
                csvPrinter.printRecord(
                        residuo.getIdResiduo(),
                        residuo.getCodigo(),
                        residuo.getNombre(),
                        residuo.getPeligrosidad()
                );
            }
        }
        return writer.toString(); // Retorna el contenido del archivo CSV
    }
}