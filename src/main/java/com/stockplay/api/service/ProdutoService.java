package com.stockplay.api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stockplay.api.dto.ProdutoRequest;
import com.stockplay.api.model.Produto;
import com.stockplay.api.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private static final Logger log = LoggerFactory.getLogger(ProdutoService.class);

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto cadastrar(ProdutoRequest dados) {
        Produto produto = repository.save(new Produto(dados.nome(), dados.descricao(), dados.preco(), dados.quantidade()));
        log.info("Produto {} cadastrado", produto.getId());
        return produto;
    }

    public List<Produto> listar() {
        return repository.findAll();
    }

    public Produto buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProdutoNaoEncontradoException(id));
    }

    @Transactional
    public Produto atualizar(Long id, ProdutoRequest dados) {
        Produto produto = buscar(id);
        produto.atualizar(dados.nome(), dados.descricao(), dados.preco(), dados.quantidade());
        return produto;
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }
}
