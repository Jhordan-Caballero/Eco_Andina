package com.gestion.residuos.Eco_Andino.dto;

import java.time.LocalDateTime;

public record DepuracionResponse(int eliminados, LocalDateTime anterioresA) {
}
