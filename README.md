# Chat Desktop

Aplicativo de chat desktop com **backend Java/Spring Boot** e **frontend Java Swing**, usando **STOMP sobre WebSocket**, fallback SockJS, FlatLaf e autenticação JWT.

## Arquitetura

O projeto é Maven multimódulo e mantém a separação entre backend e frontend. O backend usa Spring Data JPA com H2 em arquivo para desenvolvimento local; a camada de persistência está isolada em `UsuarioRepository`, facilitando a troca posterior para PostgreSQL. O frontend utiliza `HttpClient` para autenticação REST, mantém o token somente em memória e abre o JFrame principal após login bem-sucedido.

| Módulo | Responsabilidade | Entrada |
|---|---|---|
| `backend` | REST de autenticação, JPA/H2, BCrypt, JWT, WebSocket/STOMP e presença em memória | `http://localhost:8080` |
| `frontend` | Login, cadastro, sessão JWT, JFrame de chat, FlatLaf e cliente STOMP | `ChatClientApplication` |

## Autenticação REST

Os endpoints de autenticação são públicos e retornam mensagens JSON compreensíveis em caso de erro:

| Método | Endpoint | Resultado |
|---|---|---|
| `POST` | `/auth/registrar` | Cria o usuário com senha BCrypt; retorna `201 Created` |
| `POST` | `/auth/login` | Valida credenciais e retorna token JWT; retorna `401 Unauthorized` em caso de falha |

Exemplo de registro:

```bash
curl -X POST http://localhost:8080/auth/registrar \
  -H "Content-Type: application/json" \
  -d '{"nomeUsuario":"ana","senha":"senha123"}'
```

Exemplo de login:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"nomeUsuario":"ana","senha":"senha123"}'
```

A resposta de login tem este formato:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "nomeUsuario": "ana",
  "expiraEmSegundos": 86400
}
```

O frontend não grava o token em arquivo, banco ou preferências do sistema. Ele permanece na instância `AuthSession` em memória durante a execução do cliente.

## Como o JWT trafega

Para futuras requisições REST protegidas, o formato é o header padrão:

```http
Authorization: Bearer <token>
```

A conexão SockJS possui uma limitação prática: o handshake HTTP acontece antes do frame STOMP `CONNECT`, e o cliente Swing não injeta o header `Authorization` no transporte SockJS. Por isso, o cliente usa:

```text
http://localhost:8080/ws?access_token=<JWT>
```

O `JwtHandshakeInterceptor` valida esse token antes de aceitar a sessão. Depois, o `JwtChannelInterceptor` associa o usuário autenticado ao comando STOMP `CONNECT`, e o backend reutiliza essa identidade ao publicar mensagens e registrar presença. O campo `remetente` enviado pelo cliente não é usado para autenticação, evitando que um usuário se passe por outro.

## WebSocket/STOMP

| Destino | Direção | Finalidade |
|---|---|---|
| `/app/mensagens` | cliente → backend | Publica mensagem do usuário autenticado |
| `/topic/mensagens` | backend → clientes | Broadcast para o chat geral |
| `/app/presenca/entrar` | cliente → backend | Registra a sessão do usuário autenticado |
| `/topic/usuarios` | backend → clientes | Atualiza usuários conectados |

O endpoint `/ws` permanece configurado com SockJS e CORS liberado para desenvolvimento local. A presença segue em memória e é removida no encerramento da sessão.

## Banco H2 e troca para PostgreSQL

Por padrão, o H2 usa o arquivo `./data/chatdb`, criado na pasta a partir da qual o backend é iniciado. Os usuários continuam disponíveis após reiniciar o backend localmente. O console H2 fica em `http://localhost:8080/h2-console`, com JDBC URL `jdbc:h2:file:./data/chatdb`, usuário `sa` e senha vazia.

Para migrar para PostgreSQL, substitua a dependência runtime do H2 pela dependência do driver PostgreSQL e forneça as propriedades `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` e `spring.datasource.driver-class-name` por ambiente. A entidade e o repositório JPA não precisam mudar.

A chave JWT existente é apenas de desenvolvimento. Em produção, altere `app.jwt.secret` para uma variável de ambiente ou um secret manager, usando uma chave Base64 de pelo menos 256 bits.

## Identidade visual

Toda a interface Swing usa a classe `AppTheme` como fonte única para cores, fontes, espaçamentos e raios. O padrão visual é um modo escuro quente, com fundo chumbo `#262624`, sidebar `#1E1D1B`, cards `#33322F`, texto quase branco `#ECECE6`, texto muted `#9C9A93`, destaque terracota `#D97757` e bordas sutis `#3A3935`. A tipografia agora prioriza `Aptos`, depois `Segoe UI Variable`, `Inter` e, por fim, `Segoe UI`/sans-serif como fallback. O `FlatDarkLaf` é configurado em `ChatClientApplication`, e `RoundedPanel` fornece painéis reutilizáveis com cantos arredondados.

Para qualquer tela nova, inicialize o tema uma única vez no ponto de entrada, use as constantes de `AppTheme`, aplique `AppTheme.stylePrimaryButton` ou `AppTheme.styleSecondaryButton` aos botões e prefira `RoundedPanel` para cards, formulários e blocos de conteúdo. Componentes novos devem manter padding de 12–16 px, evitar bordas pesadas e usar `AppTheme.TEXT_MUTED` para legendas e estados secundários.

## Estrutura relevante

```text
backend/src/main/java/com/example/chat/backend/
├── auth/
│   ├── AuthController.java
│   ├── AuthExceptionHandler.java
│   ├── AuthService.java
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── InvalidCredentialsException.java
│   └── UsernameAlreadyExistsException.java
├── config/
│   ├── WebCorsConfig.java
│   └── WebSocketConfig.java
├── controller/ChatController.java
├── entity/Usuario.java
├── repository/UsuarioRepository.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtChannelInterceptor.java
│   ├── JwtHandshakeInterceptor.java
│   ├── JwtService.java
│   ├── SecurityConfig.java
│   └── WebSocketUserRegistry.java
└── service/
    ├── PresenceEventListener.java
    └── PresenceService.java

frontend/src/main/java/com/example/chat/frontend/
├── auth/
│   ├── AuthApiClient.java
│   └── AuthSession.java
├── client/ChatWebSocketClient.java
├── ui/
│   ├── AppTheme.java
│   ├── LoginDialog.java
│   ├── MainFrame.java
│   ├── RegisterDialog.java
│   ├── RoundedPanel.java
│   └── UsernameDialog.java
└── ChatClientApplication.java
```

## Como executar

É necessário ter **JDK 17 ou superior** e **Maven 3.9 ou superior**. A partir da raiz do projeto:

```bash
mvn clean package
```

Em um primeiro terminal, inicie o backend:

```bash
mvn -pl backend spring-boot:run
```

Em um segundo terminal, execute o frontend já empacotado com dependências:

```bash
java -jar frontend/target/chat-frontend-1.0.0-SNAPSHOT.jar
```

No Windows PowerShell, use os mesmos comandos a partir de `C:\ProgramProject\BigProjects\Chatting`. O cliente abrirá a tela de login. O botão **Criar conta** abre o cadastro, com validação de usuário, senha mínima de seis caracteres e confirmação. Após o login, a janela de chat é aberta e a conexão STOMP é autorizada pelo JWT.

## Validação realizada

O projeto foi compilado com `mvn clean package`. Também foi validado um fluxo integrado que registra um usuário, confirma a rejeição de duplicidade, rejeita senha incorreta, realiza login, abre o handshake SockJS com JWT, publica mensagem e verifica que o servidor usa o usuário autenticado em vez do remetente falsificado.

## Referências técnicas

[1]: https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html "Spring Security — Password Storage"
[2]: https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html "Spring Framework — STOMP over WebSocket"
[3]: https://github.com/jwtk/jjwt "JJWT — JSON Web Token for Java"
[4]: https://www.formdev.com/flatlaf/ "FlatLaf — Look and Feel para Swing"
[5]: https://docs.spring.io/spring-data/jpa/reference/ "Spring Data JPA Reference"
