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
            input = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            output = new PrintWriter(cliente.getOutputStream(), true);
            output.println("por favor coloque o seu nome:");
            output.flush();
            String message = input.readLine();
            output.println("Bem vindo ao chat " + message);
            this.nomeCliente = message;
            clientesConectados.put(nomeCliente, this);
            while (true) {
                message = input.readLine();
                
                if (message.equalsIgnoreCase("::sair")) {
                    output.println("Voce saiu do chat");
                    cliente.close();
                    break;
                } else if (message.toLowerCase().startsWith("::msg")) {

                    String[] parts = message.split(" ");

                    if (parts.length > 2) {
                        String nomedestinatario = parts[1];
                        String conteudo = message.substring(6 + nomedestinatario.length()).trim();

                        if (conteudo.isEmpty()) {
                            output.println("Mensagem vazia, escreva algo após o destinatário!");
                        } else {
                            System.out.println("Enviando para " + nomedestinatario);
                            GerenciamentoDeCliente destinatario = clientesConectados.get(nomedestinatario);

                            if (destinatario == null) {
                                output.println("Cliente " + nomedestinatario + " nao encontrado");
                            } else {
                                destinatario.getOutput().println(getNomeCliente() + ": " + conteudo);
                                output.println(nomeCliente + ": " + conteudo);
                            }
                        }
                    } else {
                        output.println("Formato inválido. Use: ::msg <destinatario> <mensagem>");
                    }

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
