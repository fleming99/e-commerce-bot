# E-commerce Telegram Chat Bot

## Descrição

Este projeto é um sistema de e-commerce utilizando um chat bot feito no Telegram, utilizando a Telegram API para fazer as requisições, com autenticação e login.

## Tecnologias Utilizadas

- **Java**
- **Spring Boot**
- **MySQL**: Utilizado como banco de dados.
- **Docker**: A aplicação foi containerizada para ser mais ágil e facil de testar e implementar.
- **Docker-compose**: Foi adicionado um docker-compose para a containerização das diferentes ferramentas que compõem o sistema.
- **Lombok**: Biblioteca utilizada para reduzir o código boilerplate da aplicação.
- **Conceitos**: Clean Code e Clean Architecture.
- **Flyway**: Versionamento do banco de dados com Flyway.
  
## Funcionalidades

O sistema permite as seguintes interações:

### E-commerce

- **Login**: Login e autenticação utilizando o número de telefone e cpf para unicidade de usuário.
- **Criação de usuário**: Caso o número de telefone não esteja cadastrado, será possível cadastrar um novo usuário, desde que possua um CPF válido.
- **Cadastrar Produtos**: Cadastro de produtos.
- **Listar Produtos**: Obtem uma listagem de produtos.
- **Procurar Produtos por ID**: Obtem um único produto pesquisando por ID.
- **Inativar Produto**: Inativa um produto pelo seu ID.

## Estrutura do Projeto

A estrutura do projeto segue os princípios de Clean Architecture, garantindo que o código seja modular, fácil de manter e escalável.

## Como Usar

Para utilizar este projeto, vá até o Telegram, pesquise por Bot Father, obtenha um novo bot com o comando /newbot, e troque as propriedades bot.name e bot.token no application.properties com os valores obtidos.
