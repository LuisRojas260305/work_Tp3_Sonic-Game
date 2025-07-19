package com.miestudio.jsonic.Server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Pantallas.GameScreen;
import com.miestudio.jsonic.Pantallas.LobbyScreen;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.GameState;
import com.miestudio.jsonic.Util.InputState;
import com.miestudio.jsonic.Util.ShutdownPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestiona todas las operaciones de red, incluyendo la lógica de juego para el servidor.
 */
public class NetworkManager {

    private final JuegoSonic game;
    private ServerSocket serverSocket;
    private final List<ClientConnection> clientConnections = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger nextPlayerId = new AtomicInteger(1);
    private volatile GameState currentGameState;
    private ObjectOutputStream clientOutputStream; // Stream para enviar inputs del cliente al servidor
    private ObjectInputStream clientInputStream; // Stream para recibir GameState del servidor
    private Socket clientSocket; // Socket del cliente

    // Mapa para almacenar los inputs de todos los jugadores (solo relevante en el Host)
    private final ConcurrentHashMap<Integer, InputState> playerInputs = new ConcurrentHashMap<>();

    private volatile boolean isHost = false;
    private volatile boolean shouldAnnounce = false;
    private DatagramSocket discoverySocket;
    private TreeSet<Integer> availablePlayerIds = new TreeSet<>(); // Para IDs de clientes (1, 2)

    // Hilos de red
    private Thread hostDiscoveryThread;
    private Thread clientReceiveThread;

    public NetworkManager(JuegoSonic game) {
        this.game = game;
        for (int i = 1; i < Constantes.MAX_PLAYERS; i++) {
            availablePlayerIds.add(i);
        }
    }

    /**
     * Verifica el estado de la red para determinar si esta instancia será host o cliente.
     * Se ejecuta en un hilo separado para no bloquear el hilo principal.
     */
    public void checkNetworkStatus() {
        String serverAddress = discoverServer();
        if (serverAddress != null) {
            // Si se encuentra un servidor, intentar conectarse como cliente
            try {
                String[] parts = serverAddress.split(":");
                String ip = parts[0];
                int port = Integer.parseInt(parts[1]);
                connectAsClient(ip, port);
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear la dirección del servidor: " + serverAddress);
                startHost(); // Fallback a host si hay un error
            }
        } else {
            // Si no se encuentra un servidor, convertirse en host
            startHost();
        }
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
     * Configura la instancia en caso de ser host del juego.
     * - Crea el socket del servidor.
     * - Comienza a aceptar conexiones.
     */
    public void startHost() {
        isHost = true;
        shouldAnnounce = true;

        System.out.println("Soy el HOST.");

        try {
            serverSocket = new ServerSocket(Constantes.GAME_PORT);
            System.out.println("Servidor escuchando en el puerto: " + Constantes.GAME_PORT);

            // Hilo para aceptar conexiones de los jugadores
            Thread acceptClientsThread = new Thread(this::acceptClients);
            acceptClientsThread.setDaemon(true);
            acceptClientsThread.start();

            // Hilo para anunciar la presencia del servidor
            hostDiscoveryThread = new Thread(this::announceServer);
            hostDiscoveryThread.setDaemon(true);
            hostDiscoveryThread.start();

            // El host es el jugador 0 (Sonic)
            Gdx.app.postRunnable(() -> game.setScreen(new LobbyScreen(game, Color.BLUE, true)));

        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    /**
     * Envia anuncios UDP para que los clientes puedan descubrir este servidor.
     */
    private void announceServer() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);

            while (isHost && !Thread.currentThread().isInterrupted()) { // El hilo se mantiene vivo mientras sea el host
                if (shouldAnnounce) {
                    String message = NetworkHelper.ObtenerIpLocal() + ":" + Constantes.GAME_PORT + ":" + (Constantes.MAX_PLAYERS - 1 - clientConnections.size());
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
        } finally {
            System.out.println("Hilo de anuncio de servidor terminado.");
        }
    }

    /**
     * Configura la instancia en caso de ser jugador.
     * - Intenta conectarse al host.
     */
    public void connectAsClient(String ip, int port) {
        isHost = false;

        System.out.println("Soy CLIENTE.");

        try {
            clientSocket = new Socket();
            clientSocket.connect(new java.net.InetSocketAddress(ip, port), 5000); // 5 segundos de timeout
            System.out.println("¡Conectado al servidor! Esperando ID de jugador...");

            this.clientOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.clientInputStream = new ObjectInputStream(clientSocket.getInputStream());

            int playerId = clientInputStream.readInt();

            if (playerId == -1 || playerId == 255) { // -1 para fin de stream, 255 para rechazo explícito
                System.err.println("Conexión rechazada por el servidor (probablemente está lleno).");
                return;
            }

            System.out.println("ID de jugador recibido del servidor: " + playerId);

            Color playerColor = (playerId == 1) ? Color.YELLOW : (playerId == 2) ? Color.RED : Color.GRAY;
            Gdx.app.postRunnable(() -> game.setScreen(new LobbyScreen(game, playerColor, false)));

            // Hilo para recibir GameState y mensajes del servidor
            clientReceiveThread = new Thread(() -> {
                try {
                    while (!clientSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                        Object receivedObject = clientInputStream.readObject();
                        if (receivedObject instanceof String && "START_GAME".equals(receivedObject)) {
                            Gdx.app.postRunnable(() -> game.setScreen(new GameScreen(game, playerId)));
                        } else if (receivedObject instanceof GameState) {
                            this.currentGameState = (GameState) receivedObject;
                        } else if (receivedObject instanceof ShutdownPacket) {
                            System.out.println("Recibido ShutdownPacket. Cerrando aplicación.");
                            Gdx.app.postRunnable(Gdx.app::exit);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Desconectado del servidor: " + e.getMessage());
                    Gdx.app.postRunnable(Gdx.app::exit);
                } finally {
                    System.out.println("Hilo de recepción de cliente terminado.");
                }
            });
            clientReceiveThread.setDaemon(true);
            clientReceiveThread.start();

        } catch (IOException e) {
            System.err.println("No se pudo conectar al servidor. ¿Está iniciado? Error: " + e.getMessage());
        }
    }

    private void acceptClients() {
        while (!serverSocket.isClosed()) { // Mantener el bucle mientras el socket no esté cerrado
            try {
                Socket clientSocket = serverSocket.accept();
                
                if (clientConnections.size() < Constantes.MAX_PLAYERS - 1) {
                    int playerId = nextPlayerId.getAndIncrement();
                    ClientConnection connection = new ClientConnection(clientSocket, playerId, new ObjectOutputStream(clientSocket.getOutputStream()), new ObjectInputStream(clientSocket.getInputStream()));
                    clientConnections.add(connection);
                    connection.out.writeInt(playerId); // Enviar ID al cliente
                    connection.out.flush();
                    Thread clientThread = new Thread(connection);
                    clientThread.setDaemon(true);
                    clientThread.start();
                } else {
                    System.out.println("Servidor lleno. Rechazando conexión de " + clientSocket.getInetAddress());
                    ObjectOutputStream tempOut = new ObjectOutputStream(clientSocket.getOutputStream());
                    tempOut.writeInt(255); // Enviar byte de rechazo
                    tempOut.flush();
                    tempOut.close(); // Cerrar el stream temporal
                    clientSocket.close();
                }
            } catch (java.net.SocketException e) {
                // Esta excepción es esperada cuando el serverSocket.close() interrumpe el accept()
                System.out.println("El socket del servidor se ha cerrado. Dejando de aceptar clientes.");
                break; // Salir del bucle de aceptación de clientes
            } catch (IOException e) {
                System.err.println("Error al aceptar cliente: " + e.getMessage());
            }
        }
        System.out.println("Hilo de aceptación de clientes terminado.");
    }

    /**
     * Obtiene el siguiente ID de jugador disponible.
     * @return El ID de jugador disponible, o -1 si no hay ninguno.
     */
    private synchronized int getNextAvailablePlayerId() {
        if (!availablePlayerIds.isEmpty()) {
            return availablePlayerIds.pollFirst();
        }
        return -1;
    }

    public void startGame() {
        broadcastMessage("START_GAME");
        Gdx.app.postRunnable(() -> game.setScreen(new GameScreen(game, 0))); // Host es el jugador 0
    }

    public void broadcastGameState(GameState gameState) {
        this.currentGameState = gameState;
        synchronized (clientConnections) {
            for (ClientConnection conn : clientConnections) {
                conn.sendGameState(gameState);
            }
        }
    }

    public void broadcastMessage(Object message) {
        synchronized (clientConnections) {
            for (ClientConnection conn : clientConnections) {
                conn.sendMessage(message);
            }
        }
    }

    public void sendInputState(InputState inputState) {
        if (clientOutputStream != null) {
            try {
                clientOutputStream.writeObject(inputState);
                clientOutputStream.flush();
            } catch (IOException e) {
                System.err.println("Error al enviar estado de input: " + e.getMessage());
            }
        }
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    // Getter para que GameScreen (Host) pueda acceder a los inputs de los jugadores
    public ConcurrentHashMap<Integer, InputState> getPlayerInputs() {
        return playerInputs;
    }

    public boolean isHost() {
        return isHost;
    }

    public void dispose() {
        System.out.println("NetworkManager: Iniciando shutdown.");
        try {
            if (isHost) {
                shouldAnnounce = false; // Detener el anuncio del servidor
                if (hostDiscoveryThread != null) {
                    hostDiscoveryThread.interrupt();
                    System.out.println("NetworkManager: Hilo de anuncio interrumpido.");
                }
                if (serverSocket != null) {
                    serverSocket.close();
                    serverSocket = null;
                    System.out.println("NetworkManager: ServerSocket cerrado.");
                }
                synchronized (clientConnections) {
                    for (ClientConnection conn : clientConnections) {
                        conn.close();
                    }
                    clientConnections.clear();
                    System.out.println("NetworkManager: Conexiones de clientes cerradas.");
                }
            } else { // Es cliente
                if (clientReceiveThread != null) {
                    clientReceiveThread.interrupt();
                    System.out.println("NetworkManager: Hilo de recepción de cliente interrumpido.");
                }
                if (clientSocket != null) {
                    clientSocket.close();
                    clientSocket = null;
                    System.out.println("NetworkManager: ClientSocket cerrado.");
                }
                if (clientOutputStream != null) {
                    clientOutputStream.close();
                    clientOutputStream = null;
                }
                if (clientInputStream != null) {
                    clientInputStream.close();
                    clientInputStream = null;
                }
            }
        } catch (IOException e) {
            System.err.println("NetworkManager: Error al cerrar recursos de red: " + e.getMessage());
        } finally {
            System.out.println("NetworkManager: Shutdown completado.");
        }
    }

    private class ClientConnection implements Runnable {
        private final Socket socket;
        private final int playerId;
        private final ObjectOutputStream out;
        private final ObjectInputStream in;

        ClientConnection(Socket socket, int playerId, ObjectOutputStream out, ObjectInputStream in) {
            this.socket = socket;
            this.playerId = playerId;
            this.out = out;
            this.in = in;
        }

        @Override
        public void run() {
            try {
                while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    Object receivedObject = in.readObject();
                    if (receivedObject instanceof InputState) {
                        // Actualizar el mapa de inputs en el NetworkManager principal
                        NetworkManager.this.playerInputs.put(playerId, (InputState) receivedObject);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Cliente " + playerId + " desconectado: " + e.getMessage());
                clientConnections.remove(this);
                // Opcional: Notificar a GameScreen o al juego que un cliente se desconectó
            } finally {
                System.out.println("ClientConnection para " + playerId + " terminado.");
            }
        }

        void sendGameState(GameState gameState) {
            try {
                out.writeObject(gameState);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error al enviar GameState al cliente " + playerId + ": " + e.getMessage());
                // Considerar cerrar la conexión si hay un error de envío persistente
            }
        }

        void sendMessage(Object message) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error al enviar mensaje al cliente " + playerId + ": " + e.getMessage());
            }
        }

        void close() {
            try {
                socket.close();
                out.close();
                in.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar socket del cliente " + playerId + ": " + e.getMessage());
            }
        }
    }
}