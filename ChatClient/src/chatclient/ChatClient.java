/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author carlo
 */
public class ChatClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            Socket cliente = new Socket("localhost", 12345);

            // Inicializando o socket e verificando a conexão
            System.out.println("Conectado ao servidor: " + cliente.getInetAddress());

            new Thread() {
                //lendo mensagem do servidor
                @Override
                public void run() {
                    try {
                        BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

                        while (true) {
                            String msg = entrada.readLine();
                            System.out.println("Servidor: " + msg);
                            

                        }
                    } catch (NullPointerException ex) {
                        System.out.println("Impossível ler a mensagem do servidor");
                    } catch (IOException ex) {
                        System.out.println("Erro de I/O: " + ex.getMessage());
                    }
                }

            }.start();

            //escrevendo para o servidor
            PrintWriter saida = new PrintWriter(cliente.getOutputStream());
            BufferedReader leitorTerminal = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (true) {
                    String msg = leitorTerminal.readLine();
                    saida.println(msg);
                    saida.flush();
                    if(msg.equalsIgnoreCase("::sair")){

                        break;
                    }
                }
            } finally {
                cliente.close(); // Fechando o socket ao sair
                saida.close();
                leitorTerminal.close();
            }
        } catch (UnknownHostException e) {
            System.out.println("Servidor não encontrado: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erro de I/O: " + e.getMessage());
        }
    }
}

