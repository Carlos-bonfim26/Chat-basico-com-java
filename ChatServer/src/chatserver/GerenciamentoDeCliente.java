/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author carlo
 */
public class GerenciamentoDeCliente implements Runnable {

    private Socket cliente;
    private String nomeCliente;
    private static final HashMap<String, GerenciamentoDeCliente> clientesConectados = new HashMap<>();
    private BufferedReader input;
    private PrintWriter output;

    public GerenciamentoDeCliente(Socket cliente) {
        this.cliente = cliente;
    }

    @Override
    
    public void run() {
        try {
            //inicializando os canais de comunicação
            input = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            output = new PrintWriter(cliente.getOutputStream(), true);
            output.println("por favor coloque o seu nome:");
            //lendo o nome do cliente
            output.flush();
            String message = input.readLine();
            output.println("Bem vindo ao chat " + message);
            this.nomeCliente = message.toLowerCase().replaceAll(",", "");
            // adicionando o cliente à lista de conectados
            clientesConectados.put(nomeCliente, this);
            //lendo mensagens do cliente
            while (true) {
            
                message = input.readLine();
                //permitir que o cliente saia do chat
                if (message.equalsIgnoreCase("::sair")) {
                    output.println("Voce saiu do chat");
                    cliente.close();
                    break;
                }
                //listar clientes conectados
                else if(message.trim().equalsIgnoreCase("::lista")){
                StringBuilder str = new StringBuilder();
                //montando a lista de clientes conectados
                for(String c : clientesConectados.keySet()){
                    str.append(c).append("\n");
                }
                output.println("Clientes conectados:\n" + str);
                
            }
            //mostrar ajuda 
            else if(message.trim().equalsIgnoreCase("::ajuda")){
                output.println("Comandos disponíveis:\n:: <destinatario> <mensagem> - Enviar mensagem privada\n::lista - Listar clientes conectados\n::sair - Sair do chat\n::ajuda - Mostrar esta ajuda");
            }
              
                // Envio de mensagens para outros clientes
                else if (message.toLowerCase().startsWith("::")) {

                    String[] parts = message.split(" ");
                    // Verificando se o formato da mensagem está correto
                    if (parts.length > 2) {
                        // Extraindo o nome do destinatário e o conteúdo da mensagem
                        String nomedestinatario = parts[1];
                        String conteudo = message.substring(3 + nomedestinatario.length()).trim();

                        if (conteudo.isEmpty()) {
                            output.println("Mensagem vazia, escreva algo após o destinatário!");
                        } else {
                            // Enviando a mensagem para o destinatário
                            System.out.println("Enviando para " + nomedestinatario);
                            GerenciamentoDeCliente destinatario = clientesConectados.get(nomedestinatario);

                            if (destinatario == null) {
                                output.println("Cliente " + nomedestinatario + " nao encontrado");
                            } else {
                                // Enviando a mensagem para o destinatário
                                destinatario.getOutput().println(getNomeCliente() + ": " + conteudo);
                                output.println(nomeCliente + ": " + conteudo);
                            }
                        } 
                    } else {
                        output.println("comando inválido. Use: ::ajuda para ver os comandos disponíveis.");
                    }

                }   else {
                    output.println("Comando inválido. Use ::ajuda para ver os comandos disponíveis.");
                }
            
            }

        } catch (IOException ex) {
            System.out.println("O cliente fechou a conexão");
            ex.printStackTrace();
        }
    }

    public PrintWriter getOutput() {
        return output;
    }

    public BufferedReader getInput() {
        return input;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

}
