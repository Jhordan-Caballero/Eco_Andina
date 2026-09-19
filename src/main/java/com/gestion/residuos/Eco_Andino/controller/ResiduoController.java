package com.gestion.residuos.Eco_Andino.controller;
import com.gestion.residuos.Eco_Andino.entity.Residuo;
import com.gestion.residuos.Eco_Andino.repository.ResiduoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/residuos")
public class ResiduoController {
    @Autowired
    private ResiduoRepository residuoRepository;

    // Endpoint para listar todos los residuos
    @GetMapping
    public List<Residuo> obtenerTodos() {
        return residuoRepository.findAll();
    }

    // Endpoint para crear un residuo y probar la escritura en la BD
    @PostMapping
    public Residuo guardarResiduo(@RequestBody Residuo residuo) {
        return residuoRepository.save(residuo);
    }
}
