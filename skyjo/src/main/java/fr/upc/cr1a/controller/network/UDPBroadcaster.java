package fr.upc.cr1a.controller.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import java.util.List; // LE REITIREZ PAS SINON VSCODE PAS CONTENT

/**
 * Classe pour envoyer des msg sur toutes les cartes reseaux
 */
public class UDPBroadcaster {

    public static void broadcast( int broadcastPort, String message ){
        try (DatagramSocket datagramSocket = new DatagramSocket()){
            
            datagramSocket.setBroadcast(true);
            byte[] messagePackage = message.getBytes();

            // recup toutes les entrées cartes reseaux (wifi/ethernet/VLAN)
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); // pq on peut pas mettre dans des list purée

            while (interfaces.hasMoreElements()){
                NetworkInterface singleInterface = interfaces.nextElement();
                
                if ( singleInterface.isLoopback() || ! singleInterface.isUp()){ // verifie que la carte reseau est pas localhost ou eteinte 
                    continue; // passe à la pro iteration en n'executant pas ce qui se passe apres ça
                }

                // une carte reseau peut avoir plusieurs adresses ( ipv4 ou ipv6 )
                for ( InterfaceAddress interfaceAddress : singleInterface.getInterfaceAddresses()){
                    InetAddress broadcast = interfaceAddress.getBroadcast();

                    if (broadcast == null){ continue;}

                    try {
                        DatagramPacket packet = new DatagramPacket(messagePackage, messagePackage.length, broadcast, broadcastPort);
                        datagramSocket.send(packet);

                        System.out.println("[UDP] Ping envoyé sur l'interface : " + singleInterface.getDisplayName() + " (" + broadcast.getHostAddress() + ")");

                    } catch (Exception e) {
                        System.out.println("[UDP] Erreur d'envoi sur " + broadcast.getHostAddress());
                    }
                }


            }

        } catch (Exception e) {
            System.out.println("[UDP] Le message n'a pas pu etre envoyé sur le broadcast ");
            e.printStackTrace();
        } // plus besoin d'ecrire finally datagramSocket.close() si je declare datagram entre parentheses du try 
    }
    


}
