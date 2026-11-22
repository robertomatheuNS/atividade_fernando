# StockPlay API — Trabalho Individual de Pesquisa e Aplicação

**Disciplina** — Back-end Frameworks, Aulas 10 e 11  
**Professor** — Fernando Pereira  
**Aluno(a)** — Roberto Matheus Nunes dos Santos  
**Data** — 21/11/2026  
**Repositório individual no GitHub** — https://github.com/robertomatheuNS/atividade_fernando.git

## Introdução

A StockPlay é uma API REST de cadastro de produtos com Java 21, Spring Boot e PostgreSQL. Cada produto tem identificador, nome, descrição, preço e quantidade. O Controller recebe e valida as requisições HTTP, o Service coordena as operações e o Repository acessa o banco com Spring Data JPA.

Para funcionar de forma confiável em outros ambientes, a API precisa de testes repetíveis, um pacote executável, configuração externa e um ambiente com dependências definidas. Também precisa oferecer meios de investigar falhas e verificar se está pronta para receber requisições. No StockPlay, esses pontos são atendidos pela suíte de testes, pelo JAR, pelo Docker Compose, pelos logs e pelo endpoint de saúde.

## Parte I — Testes automatizados

1. **Teste automatizado.** É código que executa um cenário e compara o resultado observado com o esperado. Ele pode ser repetido a cada alteração para detectar regressões.
2. **Teste manual e automatizado.** No teste manual, alguém executa as etapas e interpreta a resposta. No automatizado, o código executa o cenário e determina se passou. A verificação HTTP da StockPlay complementou os testes unitários ao exercitar a API com PostgreSQL.
3. **Arrange, Act e Assert.** Arrange prepara os dados e as dependências. Act executa a operação. Assert confirma o resultado ou a exceção esperada.
4. **JUnit.** Estrutura e executa casos com `@Test` e fornece asserções como `assertEquals` e `assertThrows`.
5. **Mockito.** Cria dependências simuladas, configura respostas e permite verificar chamadas feitas pelo código testado.
6. **Mock.** É um objeto que simula uma dependência. Um método sem comportamento configurado pode retornar o valor padrão correspondente ao tipo, como zero, `false`, um `Optional.empty()` ou `null`. A resposta relevante deve ser configurada explicitamente para que a intenção do teste fique clara.
7. **Service e PostgreSQL.** O teste unitário da StockPlay instancia o `ProdutoService` com um `ProdutoRepository` simulado. Assim, executa rapidamente e controla cenários como um ID ausente. A integração com PostgreSQL é verificada em outra camada. O teste unitário pode rodar sem iniciar o contexto do Spring.
8. **`@Mock` e `@InjectMocks`.** `@Mock` cria o Repository simulado. `@InjectMocks` cria o Service real e injeta o mock pelo construtor.
9. **Configuração, interação e exceção.** `when(...).thenReturn(...)` determina o retorno de uma chamada simulada. `verify(...)` confirma uma interação, como a chamada a `save`. `assertThrows(...)` confirma o tipo da exceção produzida. `assertEquals` compara valores, como o preço enviado e o retornado.
10. **Service e Controller.** O teste de Service cobre decisões da camada de aplicação, como atualizar um produto encontrado ou lançar uma exceção se ele estiver ausente. O teste de Controller cobre o contrato HTTP, incluindo status, validação de entrada e formato da resposta.
11. **MockMvc.** Exercita o processamento de requisições pelo Spring MVC com objetos simulados de requisição e resposta. Pode testar, por exemplo, se um cadastro válido produz `201` com `Location` e se uma exceção do Service vira `404`.
12. **Cobertura.** Cobertura mede quais trechos foram executados. Mesmo com 100%, uma asserção fraca pode aceitar uma resposta incorreta e deixar passar erros em valores de limite ou combinações de entrada.

### Exemplo comentado de teste de Service

Trecho de [ProdutoServiceTest.java](src/test/java/com/stockplay/api/service/ProdutoServiceTest.java). A classe usa `@ExtendWith(MockitoExtension.class)` para inicializar as anotações do Mockito.

```java
@Test
void deveCadastrarProdutoValido() {
    // Arrange: prepara a entrada e faz o mock devolver o produto recebido.
    ProdutoRequest dados = new ProdutoRequest("Teclado", "USB", new BigDecimal("89.90"), 5);
    when(repository.save(any(Produto.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

    // Act: executa o cadastro no Service real.
    Produto produto = service.cadastrar(dados);

    // Assert: confirma a chamada ao Repository e confere os dados do resultado.
    ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
    verify(repository).save(captor.capture());
    assertSame(captor.getValue(), produto);
    assertEquals(dados.nome(), produto.getNome());
    assertEquals(dados.descricao(), produto.getDescricao());
    assertEquals(dados.preco(), produto.getPreco());
    assertEquals(dados.quantidade(), produto.getQuantidade());
}
```

O mock devolve o objeto recebido para reproduzir o fluxo de retorno do cadastro. O captor permite conferir que o Service enviou o produto ao Repository. A geração do identificador e a persistência real foram verificadas nas requisições HTTP com PostgreSQL.

## Parte II — Aplicação executável e Docker

1. **Build.** Transforma o código-fonte e os recursos em um artefato executável ou distribuível.
2. **`mvn clean package`.** Limpa a saída do build anterior e executa as etapas até o empacotamento, incluindo compilação e testes na configuração da StockPlay. O Maven coloca classes, relatórios de teste e o JAR em `target/`.
3. **JAR.** É um arquivo que reúne classes e recursos Java. O plugin do Spring Boot reempacota o JAR da StockPlay para incluir o inicializador e as dependências de execução.
4. **Execução sem IDE.** Com Java 21 e as variáveis de banco definidas, o comando é `java -jar target/stockplay-api-0.0.1-SNAPSHOT.jar`. O inicializador do JAR carrega a aplicação e inicia o servidor HTTP embutido.
5. **Segredos.** Credenciais no código podem aparecer no histórico Git. Credenciais no Dockerfile podem acabar na imagem, no histórico de suas camadas ou nos registros do build. O projeto recebe as credenciais em tempo de execução.
6. **Variáveis de ambiente.** Permitem trocar parâmetros de um ambiente para outro sem recompilar o JAR. O StockPlay lê `DB_URL`, `DB_USER` e `DB_PASSWORD`.
7. **Profiles do Spring.** Ativam configurações para ambientes ou finalidades específicas. Por exemplo, `application-dev.properties` pode ser carregado com `SPRING_PROFILES_ACTIVE=dev`. A StockPlay usa a configuração base com valores externos.
8. **Imagem e container.** A imagem reúne o Java e o JAR como modelo para execução. O container é uma instância desse modelo. Parar um container encerra seu processo e permite reiniciar a mesma instância. Removê-lo descarta a instância e sua camada gravável.
9. **Dockerfile.** `FROM` escolhe a imagem base, `WORKDIR` define o diretório de trabalho, `COPY` incorpora o JAR e `ENTRYPOINT` define o comando de execução.
10. **Docker Compose.** Declara os serviços, sua rede e o volume. O Compose da StockPlay constrói a imagem da API, inicia o PostgreSQL e espera o banco ficar saudável antes de iniciar a aplicação.
11. **`localhost` em containers.** Dentro do container da API, `localhost` aponta para esse próprio container. Para alcançar o PostgreSQL, o JDBC usa o nome do serviço `db` na rede do Compose.
12. **Volumes.** Guardam dados fora da camada gravável do container. O volume `postgres_data` preserva os arquivos do PostgreSQL ao substituir seu container. Sem esse volume ou outra montagem persistente, a remoção do container pode descartar os dados que estavam na camada gravável.
13. **Logs.** Registram eventos de operação e falhas para diagnóstico. O Service registra o identificador do produto cadastrado.
14. **Dados sensíveis nos logs.** Senhas, tokens, chaves e dados pessoais desnecessários devem ficar fora dos registros. Para confirmar um cadastro, o ID é suficiente.
15. **Spring Boot Actuator.** Envia informações operacionais por endpoints gerenciados. O StockPlay expõe somente `health` por HTTP. Endpoints adicionais podem revelar detalhes internos e exigem controle de exposição e acesso.
16. **`/actuator/health`.** O estado `UP` indica que os indicadores consultados reportaram uma condição saudável naquele momento. A correção das regras de negócio é avaliada pelos testes e pelas operações do CRUD.

### Dockerfile comentado

O [Dockerfile](Dockerfile) carrega um JAR previamente empacotado. O mesmo artefato é usado para execução direta e na imagem.

```dockerfile
# Disponibiliza o runtime Java 21.
FROM eclipse-temurin:21-jre
# Define o diretório onde será executado o comando.
WORKDIR /app
# Copia o JAR empacotado para dentro da imagem.
COPY target/stockplay-api-0.0.1-SNAPSHOT.jar app.jar
# Documenta a porta da API.
EXPOSE 8080
# Inicia a aplicação com o Java quando o container sobe.
ENTRYPOINT ["java", "-jar", "app.jar"]
```

No [compose.yaml](compose.yaml), `db` usa PostgreSQL 16 com o volume `postgres_data` montado em `/var/lib/postgresql/data`. O serviço `api` recebe as credenciais do ambiente, usa `jdbc:postgresql://db:5432/stockplay` e espera o teste `pg_isready` do banco. O mapeamento do Compose publica a API na porta 8080 da máquina local.

## Estudo de caso — API de pedidos

A API de pedidos apresenta quatro problemas relacionados. O ambiente local esconde dependências da IDE e do banco, as credenciais estão no código, faltam verificações automáticas e a equipe não consegue aferir a saúde do serviço.

| Ação | Problema e aplicação na StockPlay | Cuidado |
| --- | --- | --- |
| Testes de Service e Controller | No caso de pedidos, testar regras do Service com o Repository simulado e testar o contrato HTTP com MockMvc. Na StockPlay, há quatro testes unitários de Service e verificação HTTP com banco real. | Cobrir erros além dos sucessos. Para MockMvc, simular o Service e conferir status, cabeçalhos e corpo. |
| JAR executável | Elimina a dependência de abrir a IDE. O Maven empacota a StockPlay e o Java inicia o JAR no container da API. | Usar Java compatível, executar os testes durante o build e fornecer as variáveis exigidas. |
| Variáveis de ambiente | Permitem configurar o banco fora do código. A StockPlay recebe URL, usuário e senha em execução. | Manter o arquivo local de credenciais fora do Git e evitar expor seu conteúdo nos logs. |
| Dockerfile | Define Java 21 e incorpora o mesmo artefato gerado pelo Maven. | Conferir o nome do JAR e reconstruir a imagem após alterações. |
| Compose com PostgreSQL | Reproduz o ambiente e permite usar o nome `db` para a conexão. | Aguardar o banco aceitar conexões e validar a aplicação após a inicialização. |
| Volume | Mantém os dados do banco ao substituir o container. | Preservar o volume ao encerrar o ambiente e fazer backup quando os dados forem importantes. |
| Logs | Ajudam a investigar falhas e confirmar operações. A StockPlay registra o cadastro pelo ID. | Escolher níveis adequados e omitir credenciais e dados sensíveis. |
| Actuator | Permite consultar a saúde por HTTP. A StockPlay responde em `/actuator/health`. | Limitar a exposição e usar testes para avaliar regras de negócio. |

## Resultados observados

A execução de 21/11/2026 usou Java 21 e PostgreSQL 16. Os comandos para reproduzir a validação estão no [README.md](README.md).

| Verificação | Evidência textual |
| --- | --- |
| Testes automatizados | `mvn clean package` concluiu com `BUILD SUCCESS`. Foram 5 testes, 0 falhas, 0 erros e 0 ignorados. A suíte inclui quatro testes de Service e um de inicialização do Spring com PostgreSQL. |
| Execução do JAR | O build gerou `target/stockplay-api-0.0.1-SNAPSHOT.jar`. O container executou `java -jar app.jar`, e o log registrou a inicialização do Tomcat na porta 8080. |
| Containers | Os serviços `api` e `db` iniciaram pelo Compose. O PostgreSQL foi reportado como saudável. |
| Actuator | `GET /actuator/health` retornou HTTP `200` com `"status":"UP"`. |
| Cadastro e consultas | O cadastro retornou `201`. A listagem e a busca retornaram `200` com o produto. O log registrou `Produto 1 cadastrado`. |
| Atualização | O PUT retornou `200`, e uma nova consulta confirmou nome e quantidade atualizados. |
| Validação e exclusão | Uma entrada inválida retornou `400`. A exclusão retornou `204` e a consulta posterior ao mesmo ID retornou `404`. |

## Conclusão

A StockPlay usa testes unitários para verificar as decisões do Service e requisições HTTP para confirmar o comportamento do CRUD com PostgreSQL. O JAR e o Compose tornam a execução reproduzível, enquanto a configuração externa permite trocar as credenciais por ambiente. Os logs e o Actuator permitem acompanhar o funcionamento da aplicação após a inicialização.

## Referências

- Fernando Pereira. *Back-end Frameworks, Aula 10 — Testes em aplicações Spring Boot e Aula 11 — Aplicação executável, Docker e Actuator*. Material da disciplina, PDF fornecido. Consulta em 21/11/2026.
- Fernando Pereira. *Trabalho Individual de Pesquisa e Aplicação — Aulas 10 e 11*. Consulta em 21/11/2026. [Enunciado no Notion](https://app.notion.com/p/Trabalho-Individual-de-Pesquisa-e-Aplica-o-Aulas-10-e-11-41901892d4638088aad3eb05bc7048b8)
- Equipe JUnit. *JUnit User Guide*. Consulta em 21/11/2026. [Documentação do JUnit](https://docs.junit.org/6.1.3/overview.html)
- Equipe Mockito. *Mockito Framework*. Consulta em 21/11/2026. [Documentação do Mockito](https://site.mockito.org/)
- Spring. *MockMvc*. Consulta em 21/11/2026. [Spring Framework Reference](https://docs.spring.io/spring-framework/reference/testing/mockmvc.html)
- Spring. *Actuator Endpoints*. Consulta em 21/11/2026. [Spring Boot Reference](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)
- Apache Maven Project. *Introduction to the Build Lifecycle*. Consulta em 21/11/2026. [Guia do Maven](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
- Docker. *Docker Compose*. Consulta em 21/11/2026. [Docker Docs](https://docs.docker.com/compose/)
