package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.entity.Transportista;
import org.apache.commons.collections4.CollectionUtils;

public class TransportistaService {

    public void validarDisponibilidad(Transportista transportista) {
        // Valida listas de forma segura frente a nulos con commons-collections4
        if (CollectionUtils.isEmpty(transportista.getVehiculos())) {
            throw new IllegalStateException("El transportista debe tener al menos un vehículo registrado y activo.");
        }
        // Más código...
    }
}
