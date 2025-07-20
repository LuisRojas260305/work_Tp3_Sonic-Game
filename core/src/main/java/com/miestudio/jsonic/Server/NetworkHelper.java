package com.miestudio.jsonic.Server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Clase de utilidad para operaciones relacionadas con la red, como la obtención de la dirección IP local.
 */
public class NetworkHelper {
    /**
     * Obtiene la dirección IP local de la máquina.
     *
     * @return La dirección IP local como una cadena, o "127.0.0.1" si no se puede determinar.
     */
    public static String getIpLocal(){
        try{
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()){
                NetworkInterface iface = interfaces.nextElement();

                // Saltar las interfaces loopback y no activas
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()){
                    InetAddress addr = addresses.nextElement();

                    // Direcciones site-local

                    if (addr.isSiteLocalAddress()){
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "127.0.0.1"; // Fallback a localhost
    }
}