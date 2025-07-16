package com.miestudio.jsonic.Server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Esta clase se utiliza para operaciones relacionadas con el servidor.
 * Proporciona metodos para recopilar informacion de la red local.
 */
public class NetworkHelper {
    /**
     * Obtiene la direccion IP local de la PC.
     *
     * @return Dirección IP local como cadena, o "127.0.0.1" si no se encuentra
     */
    public static String ObtenerIpLocal(){
        try{
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()){
                NetworkInterface iface = interfaces.nextElement();

                //Saltar las interfaces loopback y no activas
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()){
                    InetAddress addr = addresses.nextElement();

                    // direcciones site-local

                    if (addr.isSiteLocalAddress()){
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "127.0.0.1"; //fallback a localhost
    }
}