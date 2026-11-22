package com.stockplay.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stockplay.api.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
