package com.stockplay.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProdutoRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Size(max = 255) String descricao,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal preco,
        @NotNull @PositiveOrZero Integer quantidade) {
}
