/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author carlo
 */
public class ChatServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        ServerSocket servidor = null;
        try {
            System.out.println("ligando servidor");
            servidor = new ServerSocket(12345);
            System.out.println("servidor ligado");
            
            while (true) {                
                Socket cliente = servidor.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
              new Thread(new GerenciamentoDeCliente(cliente)).start();
            }
            
        } catch (Exception e) {
            if (servidor != null) {
                servidor.close();
            }
            e.printStackTrace();
        }
    }

}
