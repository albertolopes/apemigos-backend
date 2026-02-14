# Apemigos API — Plataforma sobre Esclerose Múltipla

A Apemigos API é o backend oficial desenvolvido para apoiar a ONG Apemigos. Este README foi atualizado para documentar o mapa da API e as mudanças recentes no projeto.

Principais mudanças e recursos adicionados
- Autenticação: JWT para usuários e tokens de serviço. Passar sempre o Bearer token e, para chamadas internas/serviço, também o header X-Service-Token (lista controlada via env var `ALLOWED_SERVICE_TOKENS`).
- Associações (CRUD mínimo): endpoint multipart para criação de associado com suporte a arquivos (PDF, imagens). Os arquivos são enviados ao provedor de storage (Cloudinary) para registro, mas o envio de email tem prioridade e os arquivos só são submetidos ao Cloudinary após o envio de email.
- Uploads: informação do retorno do Cloudinary é salva na tabela `associado_file` com FK para `associado`.
- Email: integração via Mailgun (substituiu Zoho/MailerSend). Implementado via Feign client (não RestTemplate) — use `MAILGUN_API_KEY` e `MAILGUN_DOMAIN`.
- PIX: endpoint para gerar payload/QR Code PIX estático. Configurações default em env: chave, merchant name e city.
- Swagger/OpenAPI: documentação disponível e configurada para aceitar testes com Bearer + X-Service-Token.
- Actuator: `/actuator/health` liberado sem autenticação para checagens externas.
- Segurança: validação de origem foi simplificada; agora é obrigatório `X-Service-Token` para chamadas de serviço. Requisições sem token válido são bloqueadas.
- Migrations: adicionadas migrations para as novas tabelas `associado` e `associado_file` (ver `src/main/resources/db/migrations`).

Mapa da API (endpoints principais)

- Auth
  - POST /api/auth/login
    - Corpo: { serviceKey, userLogin, serviceLogin }
    - Retorna: token JWT (Bearer) para uso nas chamadas.

- Associados
  - POST /api/associados  (multipart/form-data)
    - Headers: Authorization: Bearer <token>, X-Service-Token: <token-de-servico>
    - Form fields (exemplo):
      - nome, sobrenome, dataNascimento (YYYY-MM-DD), cpf, rg, email, telefoneContato, telefoneEmergencia,
        medicoResponsavel, telefoneMedico, possuiConvenio (true/false), convenioNome, cidade, estado,
        bairro, logradouro, complemento, cep, observacoes, nomeContatoEmergencia
      - Arquivos (partes): laudo, foto3x4, documento
    - Comportamento:
      - Envia o email com os anexos (prioridade). Se email enviado com sucesso, em background faz upload dos arquivos ao Cloudinary e persiste os registros em `associado_file`.
      - Retorno: HTTP 201 (recomendado) com Location do recurso criado ou 204 quando não há corpo.

- Email
  - POST /api/email/send
    - Headers: Authorization: Bearer <token> (e X-Service-Token se for serviço)
    - Body (JSON):
      {
        "from": { "email": "no-reply@yourdomain.com", "name": "Apemigos" },
        "to": [{ "email": "recipient@example.com" }],
        "subject": "Assunto",
        "html": "<p>Conteúdo</p>",
        "attachments": [{ "content": "BASE64_CONTENT", "filename": "arquivo.pdf", "contentType": "application/pdf" }]
      }
    - Integração: Feign client para Mailgun (API HTTP). O serviço monta a requisição multipart/form-data conforme documentação do Mailgun e envia os anexos.
    - Retorno: 204 No Content (Void) em sucesso. Exceções são tratadas globalmente.

- PIX
  - POST /api/pix/qr
    - Body: { "amount": number, "pixKey": string, "merchantName": string, "merchantCity": string, "txid": string }
    - Retorna: { "payload": "<texto-pix-com-crc>", "qrcodeBase64": "data:image/png;base64,..." }
    - Observações: o `amount` aceita número float, `merchantName` e `merchantCity` possuem truncamentos automáticos conforme padrão EMV.

- Health
  - GET /actuator/health  — liberado sem autenticação para verificações externas.

Swagger / OpenAPI
- URL (local): http://localhost:8080/swagger-ui/index.html
- No Swagger UI há opção para adicionar Bearer token e também o header `X-Service-Token` (a configuração foi adicionada para permitir testes). Adicione ambos quando necessário.

Banco de dados e Migrations
- Configurações de conexão via env vars (veja seção abaixo).
- Migrations (Liquibase) executadas na inicialização. Se a aplicação não conseguir conectar ao banco, o startup falhará.

Variáveis de ambiente (exemplos importantes)
- DATASOURCE_URL=jdbc:postgresql://localhost:5432/apemigos
- DATASOURCE_USER=postgres
- DATASOURCE_PASSWORD=postgres
- SERVER_PORT=8080

- JWT_SECRET (mínimo 64 caracteres) — usado para gerar tokens JWT
- JWT_SERVICE_KEY (chave do serviço usada no login)
- ALLOWED_SERVICE_TOKENS=comma,separated,list,of,service,tokens

- CLOUDINARY_URL= (opcional, para uploads de imagens/pdf)

- MAILGUN_API_KEY= (obrigatório para envio de email via Mailgun)
- MAILGUN_DOMAIN= (ex.: mg.sua-conta.com)
- MAILGUN_FROM_EMAIL=
- MAILGUN_FROM_NAME=
- DEFAULT_EMAIL_TO=allbertollopes@gmail.com

- PIX_KEY, PIX_MERCHANT_NAME, PIX_MERCHANT_CITY — defaults podem ser fornecidos via .env

Observações de execução e debugging
- Rodar localmente com vars carregadas: crie um arquivo `.env` na raiz (existe um exemplo no repositório) e exporte ou use uma ferramenta para carregar antes de executar.
- Para executar:

  - Empacotar: ./mvnw -DskipTests package
  - Rodar: ./mvnw spring-boot:run  (ou java -jar target/Apemigos-Backend-0.0.1-SNAPSHOT.jar)

- Testes: ./mvnw test

Logs e troubleshooting
- Se houver erros de timeout ao conectar ao servidor SMTP em produção, migramos para envio HTTP (Mailgun API) que costuma ser mais tolerante em hosts com rede restrita. Se ainda houver problemas, verifique:
  - Se as variáveis `MAILGUN_API_KEY` / `MAILGUN_DOMAIN` estão corretas
  - Se o provedor de hosting permite saída HTTPS para o domínio de API do Mailgun
  - Timeouts de rede (aumentados para 5 minutos quando enviar emails grandes)

Segurança e melhores práticas
- Nunca exponha `JWT_SECRET` ou `ALLOWED_SERVICE_TOKENS` em repositórios públicos.
- O `X-Service-Token` foi projetado para ser usado apenas por serviços. No frontend, não coloque tokens de serviço estáticos — para chamadas do browser, faça passar pelo seu backend que autentica e adiciona X-Service-Token.

Notas finais / Onde olhar no código
- Auth: `src/main/java/org/apemigos/auth`
- Associados: `src/main/java/org/apemigos/associados`
- Email / integração Mailgun: `src/main/java/org/apemigos/integrations/email`
- Cloudinary: `src/main/java/org/apemigos/integrations/cloudinary`
- Migrations: `src/main/resources/db/migrations`
- Swagger: `src/main/java/org/apemigos/configuration/OpenAPISecurityConfig.java`

---
Atualizado em: 2026-01-15
