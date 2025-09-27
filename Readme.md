# Chat básico com java
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

## Desafio do professor

Desenvolver uma aplicação que simule
um chat entre o cliente e o servidor. O
servidor deve processar mais de 1 cliente ao
mesmo tempo.

## Introdução
Documentação técnica do chat em Java focada em trechos relevantes do código: **tratamento de erros**, **pontos que se destacam** e **como o cliente se comunica com outro** via servidor. Cada trecho é parcial e comentado para uso direto no repositório.

---

## Arquitetura técnica

### Sockets TCP
- Comunicação baseada em **ServerSocket** no servidor e **Socket** no cliente.
- Conexão é orientada a stream usando `InputStream` e `OutputStream` com wrappers como `BufferedReader` e `PrintWriter`.
- O servidor escuta uma porta conhecida e aceita conexões de clientes via `serverSocket.accept()`.

### Threads
- O servidor usa **uma thread por cliente** para tratar leitura e escrita de cada conexão de forma independente.
- A thread principal do servidor apenas aceita novas conexões e delega o atendimento.
- Cada cliente deve ter uma thread dedicada localmente para ler mensagens do servidor sem bloquear a interface principal ou o loop de envio.

### Fluxo de mensagens
- Cliente conecta ao servidor e envia mensagens pelo socket.
- O servidor recebe mensagens de um cliente e **transmite (broadcast)** para os demais clientes conectados.
- O servidor mantém uma lista de conexões ativas para enviar mensagens a todos.
- A sincronização de acesso à lista de clientes deve ser tratada para evitar condições de corrida.

---

## Como compilar e executar
1. Compilar todos os arquivos Java do projeto.
```bash
javac -d out ChatServer/*.java ChatClient/*.java
```
2. Iniciar o servidor indicando a porta desejada.
```bash
java -cp out ServerMain 12345
```
3. Iniciar cada cliente informando host e porta.
```bash
java -cp out ClientMain localhost 12345
```
Substituir `ServerMain` e `ClientMain` pelo nome das classes que contêm o método `main` no seu projeto se forem diferentes.

---

## Trechos de código destacados

### Aceitação de conexões com tratamento de erro
```java
ServerSocket serverSocket = null;
try {
    serverSocket = new ServerSocket(port);
    while (!serverSocket.isClosed()) {
        try {
            Socket clientSocket = serverSocket.accept();
            // delega para handler
        } catch (IOException acceptEx) {
            System.err.println("Erro ao aceitar conexão: " + acceptEx.getMessage());
            // decidir continuar ou break conforme política
        }
    }
} catch (IOException e) {
    System.err.println("Não foi possível abrir a porta: " + e.getMessage());
} finally {
    if (serverSocket != null && !serverSocket.isClosed()) {
        try { serverSocket.close(); } catch (IOException ignored) {}
    }
}
```
- **Destaque**: capturar exceções do accept separadamente para manter o servidor rodando quando possível.

### Loop de leitura do ClientHandler com try/catch/finally
```java
public void run() {
    try {
        String line;
        while ((line = in.readLine()) != null) {
            // processa mensagem e faz broadcast
            server.broadcast(line, this);
        }
    } catch (IOException e) {
        System.err.println("Erro de I/O com cliente " + clientId + ": " + e.getMessage());
    } finally {
        // limpeza garantida
        server.removeClient(this);
        try { socket.close(); } catch (IOException ignored) {}
    }
}
```
- **Destaque**: `readLine()` bloqueia; `finally` garante remoção e fechamento mesmo em erro.

### Método de envio ao cliente com flush automático e tratamento
```java
public void send(String msg) {
    try {
        out.println(msg); // out foi criado com autoFlush = true
    } catch (Exception e) {
        System.err.println("Falha ao enviar para cliente: " + e.getMessage());
        server.removeClient(this); // remove cliente problemático
    }
}
```
- **Destaque**: proteger envio para evitar que um cliente com socket inválido quebre o broadcast.

### Broadcast robusto com remoção de clientes mortos
```java
public void broadcast(String msg, ClientHandler sender) {
    for (ClientHandler c : clients) {
        if (c == sender) continue;
        try {
            c.send(msg);
        } catch (Exception e) {
            System.err.println("Removendo cliente inativo durante broadcast");
            removeClient(c);
        }
    }
}
```
- **Destaque**: iterar sobre coleção thread-safe ou sincronizada para evitar ConcurrentModificationException.

### Cliente: thread de leitura do servidor com detecção de desconexão
```java
new Thread(() -> {
    try {
        String msg;
        while ((msg = serverIn.readLine()) != null) {
            System.out.println(msg);
        }
        System.out.println("Servidor desconectou");
    } catch (IOException e) {
        System.err.println("Erro lendo do servidor: " + e.getMessage());
    } finally {
        try { socket.close(); } catch (IOException ignored) {}
    }
}).start();
```
- **Destaque**: quando `readLine()` retorna `null` o servidor encerrou a conexão; tratar reconexão se desejado.

### Exemplo de trecho para protocolo simples (username + mensagem)
```java
// envio do cliente
String outMsg = username + ":" + message;
serverOut.println(outMsg);

// no servidor, parse rápido
String[] parts = received.split(":", 2);
String user = parts[0];
String body = parts.length > 1 ? parts[1] : "";
String broadcast = user + " says: " + body;
broadcast(broadcast, this);
```
- **Destaque**: usar `split(..., 2)` para manter o corpo intacto mesmo com ":" no texto.

---

### Boas práticas e melhorias rápidas
- **Coleção thread-safe**: usar `CopyOnWriteArrayList` ou `Collections.synchronizedList(...)` para a lista de clientes.  
- **ExecutorService**: evitar criar threads ilimitadas, usar `Executors.newCachedThreadPool()` ou `newFixedThreadPool(...)`.  
- **Heartbeat / ping**: enviar pings periódicos para detectar clientes mortos sem depender apenas de I/O.  
- **Try-with-resources**: onde aplicável, usar para fechar streams/sockets automaticamente.  
- **Logs estruturados**: trocar `System.err` por logger para análise operacional.  
- **Shutdown hook**: fechar ServerSocket e notificar clientes em encerramento do processo.  
- **Validação de mensagens**: evitar injeção de comandos; verificar tamanho máximo de mensagens.  
- **SSL**: usar `SSLServerSocket`/`SSLSocket` para produção.

---

### Pistas rápidas de implementação segura
- Ao remover clientes durante iteração, evite modificar a lista diretamente; marque para remoção ou use uma coleção segura.  
- Trate cada operação de escrita em socket com timeout se usar streams binários; para `PrintWriter` proteja com try/catch.  
- Separe lógica de protocolo da lógica de I/O para facilitar testes.

