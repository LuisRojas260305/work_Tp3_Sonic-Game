package com.miestudio.jsonic.Server;

// Importando paquetes necesarios
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Server.NetworkHelper;
import com.badlogic.gdx.Gdx;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Pantallas.MainScreen;

// Importando librerias
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona todas las operaciones de red, incluyendo:
 * - Deteccion de host y cliente
 * - Creacion de servidor
 * - Conexion de clientes
 * - Gestion de sockets
 */
public class NetworkManager {
    /** Atributos */

    /** Referencia al juego principal */
    private final JuegoSonic game;
    /** Socket del servidor */
    private ServerSocket serverSocket;
    /** Lista de sockets de clientes conectados */
    private List<Socket> clientSockets = new ArrayList<>();
    /** Inidica si esta instacia es el host */
    private boolean isHost = false;
    /** Puerto asignado dinamicamente */
    private int assignedPort;
    /** Socket para el descubrimiento de servidores */
    private volatile boolean shouldAnnounce = false;
    private DatagramSocket discoverySocket;

    /**
     * Constructor del gestor de red
     *
     * @param game Referencia al juego principal
     */
    public NetworkManager(JuegoSonic game){
        this.game = game;
    }

    /**
     * Verificar el estado de la red para determinar si esta instacia sera host o cliente
     *
     * Se ejecuta en un hilo separado para no bloquear el hilo principal
     */
    public void checkNetworkStatus(){
        new Thread(() -> {
            String serverAddress = discoverServer();
            if (serverAddress != null) {
                // Si se encuentra un servidor, intentar conectarse como cliente
                try {
                    String[] parts = serverAddress.split(":");
                    String ip = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    assignedPort = port;
                    becomeClient(ip, port);
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear la dirección del servidor: " + serverAddress);
                    becomeHost(); // Fallback a host si hay un error
                }
            } else {
                // Si no se encuentra un servidor, convertirse en host
                becomeHost();
            }
        }).start();
    }

    /**
     * Intenta descubrir un servidor existente en la red local usando UDP.
     *
     * @return La dirección IP y el puerto del servidor encontrado (ej. "192.168.1.100:7777"), o null si no se encuentra ninguno.
     */
    private String discoverServer() {
        try {
            discoverySocket = new DatagramSocket(Constantes.DISCOVERY_PORT);
            discoverySocket.setSoTimeout(2000); // Esperar 2 segundos por un anuncio

            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("Buscando servidor en el puerto " + Constantes.DISCOVERY_PORT + "...");
            discoverySocket.receive(packet); // Bloquea hasta que se recibe un paquete o el tiempo de espera expira

            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Servidor encontrado: " + message);

            // Parsear el mensaje para obtener la disponibilidad
            String[] parts = message.split(":");
            if (parts.length == 3) {
                int availableSlots = Integer.parseInt(parts[2]);
                if (availableSlots > 0) {
                    return parts[0] + ":" + parts[1]; // Retornar IP:Puerto si hay espacio
                } else {
                    System.out.println("Servidor encontrado pero sin espacio disponible.");
                    return null; // Servidor lleno
                }
            } else {
                System.err.println("Formato de anuncio de servidor inválido: " + message);
                return null; // Formato inválido
            }

        } catch (SocketTimeoutException e) {
            System.out.println("No se encontró ningún servidor en el tiempo de espera.");
            return null;
        } catch (IOException e) {
            System.err.println("Error al intentar descubrir el servidor: " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear los espacios disponibles del servidor: " + e.getMessage());
            return null;
        } finally {
            if (discoverySocket != null && !discoverySocket.isClosed()) {
                discoverySocket.close();
            }
        }
    }

    /**
     * Configurar la instancia en caso de ser host del juego
     * - Crea el socket del servidor
     * - Comienza a aceptar conexiones
     */
    private void becomeHost(){
        isHost = true;
        shouldAnnounce = true;

        System.out.println("Soy el HOST PERRA");

        try{
            serverSocket = new ServerSocket(0); // Asigna un puerto libre
            assignedPort = serverSocket.getLocalPort();
            System.out.println("Puerto asignado: " + assignedPort);

            // Hilo para aceptar conexiones de los jugadores
            new Thread(this::aceptarConexion).start();

            // Hilo para anunciar la presencia del servidor
            new Thread(this::announceServer).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Cambiar a pantalla de menu principal en modo host
        Gdx.app.postRunnable(() -> game.setScreen(new MainScreen(game, true)));
    }

    /**
     * Envia anuncios UDP para que los clientes puedan descubrir este servidor.
     */
    private void announceServer() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);

            while (isHost) { // El hilo se mantiene vivo mientras sea el host
                if (shouldAnnounce) {
                    String message = NetworkHelper.ObtenerIpLocal() + ":" + assignedPort + ":" + (Constantes.MAX_PLAYERS - 1 - clientSockets.size());
                    byte[] buffer = message.getBytes();

                    // Enviar a la dirección de broadcast de la red local
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), Constantes.DISCOVERY_PORT);
                    socket.send(packet);
                    System.out.println("Anunciando servidor: " + message);
                } else {
                    System.out.println("Servidor lleno o no debe anunciar. Esperando...");
                }
                Thread.sleep(1000); // Pausa para evitar spam de anuncios o para esperar
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al anunciar el servidor: " + e.getMessage());
        }
    }

    /**
     * Configurar la instacia en caso de ser jugador
     * - Intenta conectarse al host.
     */
    private void becomeClient(String ip, int port){
        isHost = false;

        System.out.println("Soy CLIENTE");
        ConectarServer(ip, port);

        // Cambiar a pantalla de menu principal en modo cliente
        Gdx.app.postRunnable(() -> game.setScreen(new MainScreen(game, false)));
    }

    /**
     * Acepta conexiones de clientes hasta alcanzar el max de jugadores
     */
    private void aceptarConexion(){
        try{
            while (clientSockets.size() < Constantes.MAX_PLAYERS - 1){
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);

                System.out.println("Jugador conectado: " + clientSocket.getInetAddress());

                // Iniciar un hilo para manejar la comunicación con este cliente
                new Thread(new ClientHandler(clientSocket)).start();

                if (clientSockets.size() == Constantes.MAX_PLAYERS - 1) {
                    shouldAnnounce = false; // Detener anuncios si el servidor está lleno
                    System.out.println("Servidor lleno. Deteniendo anuncios.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clase interna para manejar la comunicación con un cliente individual.
     */
    private class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            System.out.println("ClientHandler iniciado para: " + clientSocket.getInetAddress());
            try {
                // Intentar leer del InputStream para detectar desconexión
                // read() devolverá -1 si el stream ha llegado al final (cliente desconectado)
                // o lanzará una IOException si la conexión se rompe.
                while (!clientSocket.isClosed()) {
                    try {
                        int byteRead = clientSocket.getInputStream().read();
                        if (byteRead == -1) {
                            // Cliente ha cerrado la conexión limpiamente
                            System.out.println("Cliente desconectado limpiamente: " + clientSocket.getInetAddress());
                            break;
                        }
                        // Si se lee algo, se puede procesar aquí. Por ahora, solo se detecta la conexión.
                    } catch (IOException e) {
                        // Error de E/S, probablemente el cliente se desconectó abruptamente
                        System.out.println("Cliente desconectado (IOException en lectura): " + clientSocket.getInetAddress() + ", Mensaje: " + e.getMessage());
                        break;
                    }
                    // Pequeña pausa para no consumir CPU excesivamente si no hay datos
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("ClientHandler interrumpido para: " + clientSocket.getInetAddress());
            } finally {
                // Limpiar recursos cuando el cliente se desconecta
                System.out.println("Finalizando ClientHandler para: " + clientSocket.getInetAddress());
                removeClient(clientSocket);
            }
        }
    }

    /**
     * Elimina un socket de cliente de la lista y reactiva los anuncios si hay espacio.
     * @param socket El socket del cliente a eliminar.
     */
    private synchronized void removeClient(Socket socket) {
        System.out.println("Intentando eliminar cliente: " + socket.getInetAddress() + ", Clientes actuales: " + clientSockets.size());
        if (clientSockets.remove(socket)) {
            System.out.println("Cliente eliminado: " + socket.getInetAddress() + ", Clientes restantes: " + clientSockets.size());
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (clientSockets.size() < Constantes.MAX_PLAYERS - 1) {
                shouldAnnounce = true; // Reactivar anuncios si hay espacio
                System.out.println("Espacio disponible. Reactivando anuncios. shouldAnnounce = " + shouldAnnounce);
            }
        } else {
            System.out.println("El socket del cliente no se encontró en la lista: " + socket.getInetAddress());
        }
    }

    /**
     * Conecta el cliente al host usando la IP y el puerto especificados
     */
    public void ConectarServer(String ip, int port){
        try{
            Socket socket = new Socket(ip, port);

            clientSockets.add(socket);
            System.out.println("Conectado al host");
        } catch (IOException e) {
            System.out.println("Error conectando al host: " + e.getMessage());
        }
    }

    /**
     * Libera recursos de red al cerrar el juego
     * Cierra todos los socket abiertos
     */
    public void dispose(){
        isHost = false;
        shouldAnnounce = false;
        try{
            if (serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
            }

            if (discoverySocket != null && !discoverySocket.isClosed()){
                discoverySocket.close();
            }

            for (Socket socket : clientSockets){
                if (!socket.isClosed()){
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
