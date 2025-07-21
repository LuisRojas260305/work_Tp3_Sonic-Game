
package com.miestudio.jsonic.Server;

import com.badlogic.gdx.Gdx;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Pantallas.CharacterSelectionScreen;
import com.miestudio.jsonic.Pantallas.GameScreen;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.GameState;
import com.miestudio.jsonic.Util.InputState;
import com.miestudio.jsonic.Util.ShutdownPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkManager {

    private final JuegoSonic game;
    private ServerSocket serverTcpSocket;
    private DatagramSocket udpSocket;
    private final List<ClientConnection> clientConnections = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger nextPlayerId = new AtomicInteger(1);
    private volatile GameState currentGameState;
    private Socket clientTcpSocket;
    private InetAddress serverAddress;
    private int serverUdpPort;

    private final ConcurrentHashMap<Integer, InputState> playerInputs = new ConcurrentHashMap<>();
    private volatile boolean isHost = false;

    private Thread hostDiscoveryThread;
    private Thread clientTcpReceiveThread;
    private Thread udpReceiveThread;

    private final List<String> selectedCharacters = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentHashMap<Integer, String> playerCharacters = new ConcurrentHashMap<>();
    private CharacterSelectionListener characterSelectionListener;

    public interface ConnectionCallback {
        void onConnected();
        void onConnectionFailed(String error);
    }

    public NetworkManager(JuegoSonic game) {
        this.game = game;
    }

    public void discoverAndConnect(ConnectionCallback callback) {
        new Thread(() -> {
            try (DatagramSocket discoverySocket = new DatagramSocket(Constantes.DISCOVERY_PORT)) {
                discoverySocket.setSoTimeout(3000); // 3 segundos de búsqueda
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                Gdx.app.log("NetworkManager", "Buscando servidor...");
                discoverySocket.receive(packet);
                String serverIp = packet.getAddress().getHostAddress();
                Gdx.app.log("NetworkManager", "Servidor encontrado en: " + serverIp);
                connectAsClient(serverIp, Constantes.GAME_PORT, callback);
            } catch (SocketTimeoutException e) {
                Gdx.app.postRunnable(() -> callback.onConnectionFailed("No se encontró servidor."));
            } catch (IOException e) {
                Gdx.app.postRunnable(() -> callback.onConnectionFailed("Error de red."));
                e.printStackTrace();
            }
        }).start();
    }

    public void startHost() {
        isHost = true;
        try {
            serverTcpSocket = new ServerSocket(Constantes.GAME_PORT);
            serverTcpSocket.setSoTimeout(1000);
            udpSocket = new DatagramSocket(Constantes.GAME_PORT);
            Gdx.app.log("NetworkManager", "Servidor iniciado en TCP y UDP en el puerto " + Constantes.GAME_PORT);

            hostDiscoveryThread = new Thread(this::announceServer);
            hostDiscoveryThread.setDaemon(true);
            hostDiscoveryThread.start();

            Thread acceptClientsThread = new Thread(this::acceptClients);
            acceptClientsThread.setDaemon(true);
            acceptClientsThread.start();
            startUdpListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void announceServer() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            while (isHost && !Thread.currentThread().isInterrupted()) {
                String message = "SONIC_GAME_HOST";
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), Constantes.DISCOVERY_PORT);
                socket.send(packet);
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        }
    }

    private void acceptClients() {
        while (isHost && !serverTcpSocket.isClosed()) {
            try {
                Socket clientSocket = serverTcpSocket.accept();
                if (clientConnections.size() < Constantes.MAX_PLAYERS - 1) {
                    int playerId = nextPlayerId.getAndIncrement();
                    ClientConnection connection = new ClientConnection(clientSocket, playerId);
                    clientConnections.add(connection);
                    Thread clientThread = new Thread(connection);
                    clientThread.setDaemon(true);
                    clientThread.start();
                } else {
                    clientSocket.close();
                }
            } catch (SocketTimeoutException e) {
                // Continue loop
            } catch (IOException e) {
                if (!isHost || serverTcpSocket.isClosed()) break;
                e.printStackTrace();
            }
        }
    }

    private void connectAsClient(String ip, int port, ConnectionCallback callback) {
        isHost = false;
        try {
            serverAddress = InetAddress.getByName(ip);
            clientTcpSocket = new Socket(serverAddress, port);
            udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(1000);

            ObjectOutputStream out = new ObjectOutputStream(clientTcpSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientTcpSocket.getInputStream());

            out.writeInt(udpSocket.getLocalPort());
            out.flush();

            int playerId = in.readInt();
            serverUdpPort = in.readInt();

            if (playerId != -1) {
                Gdx.app.log("NetworkManager", "Conectado como jugador " + playerId);
                startClientTcpListener(in, playerId);
                startUdpListener();
                Gdx.app.postRunnable(callback::onConnected);
            } else {
                Gdx.app.postRunnable(() -> callback.onConnectionFailed("Servidor lleno."));
                clientTcpSocket.close();
                udpSocket.close();
            }
        } catch (IOException e) {
            Gdx.app.postRunnable(() -> callback.onConnectionFailed("Error de conexión."));
            e.printStackTrace();
        }
    }

    private void startClientTcpListener(ObjectInputStream in, int playerId) {
        clientTcpReceiveThread = new Thread(() -> {
            try {
                while (!clientTcpSocket.isClosed()) {
                    Object msg = in.readObject();
                    if ("START_GAME".equals(msg)) {
                        Gdx.app.postRunnable(() -> game.setScreen(new GameScreen(game, playerId)));
                    } else if (msg instanceof ShutdownPacket) {
                        Gdx.app.postRunnable(game::dispose);
                    } else if (msg instanceof UpdateSelectedCharactersPacket) {
                        List<String> chars = ((UpdateSelectedCharactersPacket) msg).selectedCharacters;
                        if (characterSelectionListener != null) {
                            characterSelectionListener.onCharacterSelectionChanged(chars);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                // Disconnected
            }
        });
        clientTcpReceiveThread.setDaemon(true);
        clientTcpReceiveThread.start();
    }

    private void startUdpListener() {
        try {
            udpSocket.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        udpReceiveThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (!udpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Object obj = ois.readObject();

                    if (isHost) {
                        if (obj instanceof InputState) {
                            InputState input = (InputState) obj;
                            playerInputs.put(input.getPlayerId(), input);
                        }
                    } else {
                        if (obj instanceof GameState) {
                            currentGameState = (GameState) obj;
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // Continue loop
                } catch (SocketException e) {
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        udpReceiveThread.setDaemon(true);
        udpReceiveThread.start();
    }

    public void selectCharacter(String characterName) {
        if (isHost) {
            synchronized (selectedCharacters) {
                if (!selectedCharacters.contains(characterName)) {
                    selectedCharacters.add(characterName);
                    playerCharacters.put(0, characterName); // Host is player 0
                    broadcastTcpMessage(new UpdateSelectedCharactersPacket(new ArrayList<>(selectedCharacters)));
                }
            }
        } else {
            sendTcpMessage(new CharacterSelectionPacket(characterName));
        }
    }

    public void startGame() {
        if (isHost) {
            broadcastTcpMessage("START_GAME");
            Gdx.app.postRunnable(() -> game.setScreen(new GameScreen(game, 0)));
        }
    }

    public void broadcastTcpMessage(Object message) {
        synchronized (clientConnections) {
            for (ClientConnection conn : clientConnections) {
                conn.sendTcpMessage(message);
            }
        }
    }
    
    public void sendTcpMessage(Object message) {
        if (!isHost) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(clientTcpSocket.getOutputStream());
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastUdpGameState(GameState gameState) {
        if (!isHost) return;
        this.currentGameState = gameState;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(gameState);
            oos.flush();
            byte[] data = baos.toByteArray();

            synchronized (clientConnections) {
                for (ClientConnection conn : clientConnections) {
                    DatagramPacket packet = new DatagramPacket(data, data.length, conn.getClientAddress(), conn.getClientUdpPort());
                    udpSocket.send(packet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInputState(InputState inputState) {
        if (isHost) return;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(inputState);
            oos.flush();
            byte[] data = baos.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverUdpPort);
            udpSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public ConcurrentHashMap<Integer, InputState> getPlayerInputs() {
        return playerInputs;
    }
    
    public ConcurrentHashMap<Integer, String> getPlayerCharacters() {
        return playerCharacters;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setCharacterSelectionListener(CharacterSelectionListener listener) {
        this.characterSelectionListener = listener;
    }

    public void dispose() {
        if (isHost) {
            if (hostDiscoveryThread != null) hostDiscoveryThread.interrupt();
            broadcastTcpMessage(new ShutdownPacket());
            try {
                if (serverTcpSocket != null) serverTcpSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (clientTcpReceiveThread != null) clientTcpReceiveThread.interrupt();
            try {
                if (clientTcpSocket != null) clientTcpSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (udpSocket != null) udpSocket.close();
        if (udpReceiveThread != null) udpReceiveThread.interrupt();
    }

    private class ClientConnection implements Runnable {
        private final Socket tcpSocket;
        private final int playerId;
        private ObjectOutputStream tcpOut;
        private InetAddress clientAddress;
        private int clientUdpPort;

        ClientConnection(Socket socket, int playerId) {
            this.tcpSocket = socket;
            this.playerId = playerId;
            this.clientAddress = socket.getInetAddress();
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(tcpSocket.getInputStream())) {
                this.tcpOut = new ObjectOutputStream(tcpSocket.getOutputStream());
                
                this.clientUdpPort = in.readInt();
                
                tcpOut.writeInt(playerId);
                tcpOut.writeInt(udpSocket.getLocalPort());
                tcpOut.flush();

                // Send current list of selected characters
                sendTcpMessage(new UpdateSelectedCharactersPacket(new ArrayList<>(selectedCharacters)));

                Gdx.app.log("NetworkManager", "Cliente " + playerId + " conectado desde " + clientAddress.getHostAddress() + ":" + clientUdpPort);

                while (!tcpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    Object msg = in.readObject();
                    if (msg instanceof CharacterSelectionPacket) {
                        String charName = ((CharacterSelectionPacket) msg).characterName;
                        synchronized (selectedCharacters) {
                            if (!selectedCharacters.contains(charName)) {
                                selectedCharacters.add(charName);
                                playerCharacters.put(playerId, charName);
                                broadcastTcpMessage(new UpdateSelectedCharactersPacket(new ArrayList<>(selectedCharacters)));
                            }
                        }
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                Gdx.app.log("NetworkManager", "Cliente " + playerId + " desconectado.");
            } finally {
                clientConnections.remove(this);
                String removedChar = playerCharacters.remove(playerId);
                if (removedChar != null) {
                    selectedCharacters.remove(removedChar);
                    broadcastTcpMessage(new UpdateSelectedCharactersPacket(new ArrayList<>(selectedCharacters)));
                }
                try {
                    if (!tcpSocket.isClosed()) tcpSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void sendTcpMessage(Object message) {
            try {
                if (tcpOut != null) {
                    tcpOut.writeObject(message);
                    tcpOut.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public InetAddress getClientAddress() {
            return clientAddress;
        }

        public int getClientUdpPort() {
            return clientUdpPort;
        }
    }
}
