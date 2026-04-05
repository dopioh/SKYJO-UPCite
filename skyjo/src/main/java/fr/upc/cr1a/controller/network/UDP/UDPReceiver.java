package fr.upc.cr1a.controller.network.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/**
 * Classe qui attend de recevoir qqch ( sur un Thread separé ) sur toutes ses cartes reseaux
 */
public class UDPReceiver implements Runnable{

    private int listenPort;

    // volatile protection de variable en multithreads comme synchronised pour les fonctions
    private volatile boolean isListening = true;
    private Thread thread;
    private ServerListener serverListener;

    public UDPReceiver(int listenPort, ServerListener serverListener){
        this.listenPort = listenPort;
        this.serverListener = serverListener;
        this.thread = new Thread(this);
    }

    public void start(){
        isListening = true;
        thread.start();
    }

    public void stop(){
        isListening = false;
        thread.interrupt();
    }



    @Override
    public void run(){
        try (DatagramSocket socket = new DatagramSocket(listenPort)) {
            
            // si au bout de 1 sec il recoit rien, ça declenche une erreur et ça debloque le thread
            socket.setSoTimeout(1000); // en ms
            
            byte[] reception = new byte[1024]; // 2048 c'est peut etre overkill
            
            System.out.println("[UDP CLIENT] Recherche de serveurs en cours sur le port " + listenPort + "...");

            while (isListening) {
                DatagramPacket packet = new DatagramPacket(reception, reception.length);
                
                try {

                    socket.receive(packet); // on bloque la tant qu'on a pas recu de msg comme in.readLine()
                    
                    String message = new String(packet.getData(), 0, packet.getLength()); // on repasse de byte à String
 
                    String serverIp = packet.getAddress().getHostAddress(); // l'adresse IP du serveur 
                    
                    System.out.println("[UDP CLIENT] Serveur trouvé à l'IP : " + serverIp + " | Message : " + message);
                    
                    // CALL UNE METHODE QUI PREVIENT QU'ON A TROUVER UN SERVEUR ICI ICI ICI ET QU'ON SE CONNECTTE ICI
                    if (serverListener != null) {
                        serverListener.foundServer(message,serverIp);
                    }
                    
                } catch (SocketTimeoutException e) {
                    // rien à catch, les exceptions sont normals
                }
            }
            
            System.out.println("[UDP CLIENT] Arrêt de la recherche de serveurs.");

        } catch (Exception e) {
            System.out.println("[UDP CLIENT] Erreur lors de l'écoute.");
            e.printStackTrace();
        }
    }
}
