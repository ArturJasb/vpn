# 🔒 VPN em Java

Implementação de um túnel VPN criptografado em Java puro com AES-256-CBC.

## Como rodar

Pré-requisito: Java 11+

```bash
# Terminal 1 - Servidor
javac VpnServer.java
java VpnServer

# Terminal 2 - Cliente
javac VpnClient.java
java VpnClient
```

## Tecnologias

- Java puro (javax.crypto, java.net)
- Criptografia AES-256-CBC com IV aleatório
- Comunicação via Sockets TCP
- Codificação Base64

## Como funciona

```
Cliente                          Servidor
  |                                 |
  |-- AES-256 encrypt(pacote) ----->|
  |                                 |-- decrypt e processa
  |<-- AES-256 encrypt(resposta) ---|
  |-- decrypt e exibe resposta      |
```

## Roadmap

- [x] Tunel TCP com AES-256-CBC
- [ ] Troca de chaves ECDH (Bouncy Castle)
- [ ] Autenticacao com certificados X.509
- [ ] Interface TUN/TAP via JNA
- [ ] Painel de controle com Spring Boot
- [ ] Suporte a UDP

## Plano de estudo

| Fase | Conteudo | Bibliotecas |
|------|----------|-------------|
| 1 | Fundamentos Java | java.net, java.nio |
| 2 | Criptografia | javax.crypto, Bouncy Castle |
| 3 | Tunel VPN | JNA, SLF4J |
| 4 | API e Painel | Spring Boot, Lombok |
| 5 | Testes | JUnit 5, Mockito |

## Licenca

MIT
