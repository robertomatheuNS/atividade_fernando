# StockPlay API

API REST de cadastro de produtos feita com Java 21, Spring Boot e PostgreSQL.

## Executar com Docker Compose

Na raiz do projeto, gere o JAR com `./mvnw -Dtest=ProdutoServiceTest clean package` no Linux ou `.\mvnw.cmd -Dtest=ProdutoServiceTest clean package` no PowerShell. Esse comando roda os testes unitários do Service antes de empacotar a aplicação.

Crie um arquivo local `.env` com os valores abaixo e substitua a senha por uma senha de desenvolvimento. O Git ignora esse arquivo.

```dotenv
DB_USER=stockplay
DB_PASSWORD=troque-esta-senha
```

Execute `docker compose up -d --build --wait`. A API fica disponível em `http://localhost:8080`. Verifique o estado com `docker compose ps` e consulte `GET /actuator/health`. Use `docker compose logs api` para ver os logs e `docker compose down` para encerrar os containers preservando o volume do PostgreSQL.

## Executar o JAR diretamente

Com um PostgreSQL acessível e um banco criado, configure `DB_URL` (por exemplo, `jdbc:postgresql://localhost:5432/stockplay`), `DB_USER` e `DB_PASSWORD` no ambiente do terminal. Execute:

```text
java -jar target/stockplay-api-0.0.1-SNAPSHOT.jar
```

Com as mesmas variáveis, execute `./mvnw test` (ou `.\mvnw.cmd test` no PowerShell) para rodar a suíte completa, incluindo o teste de inicialização com PostgreSQL.

## Rotas

| Método | Caminho | Resultado |
| --- | --- | --- |
| POST | /produtos | Cadastra e retorna 201 com o endereço do produto |
| GET | /produtos | Lista os produtos |
| GET | /produtos/{id} | Busca um produto |
| PUT | /produtos/{id} | Atualiza os quatro campos |
| DELETE | /produtos/{id} | Exclui e retorna 204 |

Envie JSON com `Content-Type: application/json` no POST e no PUT.

```json
{
  "nome": "Teclado",
  "descricao": "Teclado USB",
  "preco": 89.90,
  "quantidade": 5
}
```

Um corpo inválido retorna 400. Buscar, atualizar ou excluir um ID inexistente retorna 404.

## Evidências de validação

Em 21/11/2026, com Java 21 e PostgreSQL 16, o comando `mvn clean package` concluiu com `BUILD SUCCESS`. A suíte executou 5 testes, com 0 falhas, 0 erros e 0 ignorados.

O Docker Compose iniciou a API e o PostgreSQL. O banco ficou saudável e o container da API executou `java -jar app.jar`. O log registrou o Tomcat na porta 8080 e a mensagem `Produto 1 cadastrado`.

| Verificação | Resultado observado |
| --- | --- |
| GET /actuator/health | 200 e status UP |
| POST /produtos | 201 |
| GET /produtos e GET /produtos/1 | 200 e produto cadastrado |
| PUT /produtos/1 | 200 e alteração confirmada em nova consulta |
| POST /produtos com dados inválidos | 400 |
| DELETE /produtos/1 | 204 |
| GET /produtos/1 após exclusão | 404 |
