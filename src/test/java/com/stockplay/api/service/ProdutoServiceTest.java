package com.stockplay.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stockplay.api.dto.ProdutoRequest;
import com.stockplay.api.model.Produto;
import com.stockplay.api.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;

    @Test
    void deveCadastrarProdutoValido() {
        // Arrange: prepara os dados e simula a persistência do produto.
        ProdutoRequest dados = new ProdutoRequest("Teclado", "USB", new BigDecimal("89.90"), 5);
        when(repository.save(any(Produto.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        // Act: cadastra o produto pelo Service.
        Produto produto = service.cadastrar(dados);

        // Assert: verifica os dados e a chamada ao Repository.
        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        verify(repository).save(captor.capture());
        assertSame(captor.getValue(), produto);
        assertEquals(dados.nome(), produto.getNome());
        assertEquals(dados.descricao(), produto.getDescricao());
        assertEquals(dados.preco(), produto.getPreco());
        assertEquals(dados.quantidade(), produto.getQuantidade());
    }

    @Test
    void deveAtualizarProdutoExistente() {
        Produto produto = new Produto("Teclado", "USB", new BigDecimal("89.90"), 5);
        ProdutoRequest dados = new ProdutoRequest("Mouse", "Sem fio", new BigDecimal("49.90"), 10);
        when(repository.findById(1L)).thenReturn(Optional.of(produto));

        Produto atualizado = service.atualizar(1L, dados);

        assertSame(produto, atualizado);
        assertEquals(dados.nome(), atualizado.getNome());
        assertEquals(dados.descricao(), atualizado.getDescricao());
        assertEquals(dados.preco(), atualizado.getPreco());
        assertEquals(dados.quantidade(), atualizado.getQuantidade());
    }

    @Test
    void deveFalharAoBuscarProdutoInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProdutoNaoEncontradoException.class, () -> service.buscar(1L));
    }

    @Test
    void deveFalharAoExcluirProdutoInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProdutoNaoEncontradoException.class, () -> service.excluir(1L));
        verify(repository, never()).delete(any(Produto.class));
    }
}
