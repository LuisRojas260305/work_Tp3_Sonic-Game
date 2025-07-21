
package com.miestudio.jsonic.Servidor;

import com.badlogic.gdx.Gdx;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Pantallas.PantallaJuego;
import com.miestudio.jsonic.Pantallas.PantallaSeleccionPersonaje;
import com.miestudio.jsonic.Utilidades.Constantes;
import com.miestudio.jsonic.Utilidades.EstadoJuego;
import com.miestudio.jsonic.Utilidades.EstadoEntrada;
import com.miestudio.jsonic.Utilidades.PaqueteApagado;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GestorRed {

    private final JuegoSonic juego;
    private ServerSocket socketTcpServidor;
    private DatagramSocket socketUdp;
    private final List<ClientConnection> conexionesCliente = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger siguienteIdJugador = new AtomicInteger(1);
    private volatile EstadoJuego estadoJuegoActual;
    private Socket socketTcpCliente;
    private InetAddress direccionServidor;
    private int puertoUdpServidor;
    private ObjectOutputStream outClienteTcp;
    private ObjectInputStream inClienteTcp;

    private final ConcurrentHashMap<Integer, EstadoEntrada> entradasJugador = new ConcurrentHashMap<>();
    private volatile boolean esHost = false;

    private Thread hiloDescubrimientoHost;
    private Thread hiloRecepcionTcpCliente;
    private Thread hiloRecepcionUdp;

    

    public interface CallbackConexion {
        void onConnected();
        void onConnectionFailed(String error);
    }

    public GestorRed(JuegoSonic juego) {
        this.juego = juego;
    }

    public void descubrirYConectar(CallbackConexion callback) {
        new Thread(() -> {
            try (DatagramSocket socketDescubrimiento = new DatagramSocket(Constantes.DISCOVERY_PORT)) {
                socketDescubrimiento.setSoTimeout(3000);
                byte[] buffer = new byte[256];
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                Gdx.app.log("GestorRed", "Buscando servidor...");
                socketDescubrimiento.receive(paquete);
                String ipServidor = paquete.getAddress().getHostAddress();
                Gdx.app.log("GestorRed", "Servidor encontrado en: " + ipServidor);
                conectarComoCliente(ipServidor, Constantes.GAME_PORT, callback);
            } catch (SocketTimeoutException e) {
                Gdx.app.postRunnable(() -> callback.onConnectionFailed("No se encontró servidor."));
            } catch (IOException e) {
                Gdx.app.postRunnable(() -> callback.onConnectionFailed("Error de red."));
                e.printStackTrace();
            }
        }).start();
    }

    public void iniciarHost() {
        esHost = true;
        try {
            socketTcpServidor = new ServerSocket(Constantes.GAME_PORT);
            socketTcpServidor.setSoTimeout(1000);
            socketUdp = new DatagramSocket(Constantes.GAME_PORT);
            Gdx.app.log("GestorRed", "Servidor iniciado en TCP y UDP en el puerto " + Constantes.GAME_PORT);

            hiloDescubrimientoHost = new Thread(this::anunciarServidor);
            hiloDescubrimientoHost.setDaemon(true);
            hiloDescubrimientoHost.start();

            Thread hiloAceptarClientes = new Thread(this::aceptarClientes);
            hiloAceptarClientes.setDaemon(true);
            hiloAceptarClientes.start();
            iniciarListenerUdp();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void anunciarServidor() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            while (esHost && !Thread.currentThread().isInterrupted()) {
                String mensaje = "SONIC_GAME_HOST";
                byte[] buffer = mensaje.getBytes();
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), Constantes.DISCOVERY_PORT);
                socket.send(paquete);
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        }
    }

    private void aceptarClientes() {
        while (esHost && !socketTcpServidor.isClosed()) {
            try {
                Socket socketCliente = socketTcpServidor.accept();
                if (conexionesCliente.size() < Constantes.MAX_PLAYERS - 1) {
                    int idJugador = siguienteIdJugador.getAndIncrement();
                    ClientConnection conexion = new ClientConnection(socketCliente, idJugador);
                    conexionesCliente.add(conexion);
                    Thread hiloCliente = new Thread(conexion);
                    hiloCliente.setDaemon(true);
                    hiloCliente.start();
                } else {
                    socketCliente.close();
                }
            } catch (SocketTimeoutException e) {
                // Continue loop
            } catch (IOException e) {
                if (!esHost || socketTcpServidor.isClosed()) break;
                e.printStackTrace();
            }
        }
    }

    private void conectarComoCliente(String ip, int puerto, CallbackConexion callback) {
        esHost = false;
        try {
            direccionServidor = InetAddress.getByName(ip);
            socketTcpCliente = new Socket(direccionServidor, puerto);
            socketUdp = new DatagramSocket();
            socketUdp.setSoTimeout(1000);

            outClienteTcp = new ObjectOutputStream(socketTcpCliente.getOutputStream());
            inClienteTcp = new ObjectInputStream(socketTcpCliente.getInputStream());

            outClienteTcp.writeInt(socketUdp.getLocalPort());
            outClienteTcp.flush();

            int idJugador = inClienteTcp.readInt();
            puertoUdpServidor = inClienteTcp.readInt();

            if (idJugador != -1) {
                Gdx.app.log("GestorRed", "Conectado como jugador " + idJugador);
                iniciarListenerTcpCliente(idJugador);
                iniciarListenerUdp();
                Gdx.app.postRunnable(callback::onConnected);
            } else {
                Gdx.app.postRunnable(() -> callback.onConnectionFailed("Servidor lleno."));
                socketTcpCliente.close();
                socketUdp.close();
            }
        } catch (IOException e) {
            Gdx.app.postRunnable(() -> callback.onConnectionFailed("Error de conexión."));
            e.printStackTrace();
        }
    }

    private void iniciarListenerTcpCliente(int idJugador) {
        hiloRecepcionTcpCliente = new Thread(() -> {
            try {
                while (!socketTcpCliente.isClosed()) {
                    Object msg = inClienteTcp.readObject();
                    if ("START_GAME".equals(msg)) {
                        Gdx.app.log("GestorRed", "Cliente recibió START_GAME. Cambiando a PantallaJuego.");
                        Gdx.app.postRunnable(() -> juego.setScreen(new PantallaJuego(juego, idJugador)));
                    } else if (msg instanceof PaqueteApagado) {
                        Gdx.app.postRunnable(juego::dispose);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                Gdx.app.log("GestorRed", "Error en hilo de recepción TCP del cliente: " + e.getMessage());
                // Disconnected
            }
        });
        hiloRecepcionTcpCliente.setDaemon(true);
        hiloRecepcionTcpCliente.start();
    }

    private void iniciarListenerUdp() {
        try {
            socketUdp.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        hiloRecepcionUdp = new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (!socketUdp.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                    socketUdp.receive(paquete);
                    ByteArrayInputStream bais = new ByteArrayInputStream(paquete.getData(), 0, paquete.getLength());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Object obj = ois.readObject();

                    if (esHost) {
                        if (obj instanceof EstadoEntrada) {
                            EstadoEntrada entrada = (EstadoEntrada) obj;
                            entradasJugador.put(entrada.getIdJugador(), entrada);
                        }
                    } else {
                        if (obj instanceof EstadoJuego) {
                            estadoJuegoActual = (EstadoJuego) obj;
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
        hiloRecepcionUdp.setDaemon(true);
        hiloRecepcionUdp.start();
    }

    

    
        public void iniciarJuego() {
        if (esHost) {
            enviarMensajeTcpBroadcast("START_GAME");
            Gdx.app.postRunnable(() -> juego.setScreen(new PantallaJuego(juego, 0)));
        }
    }

    public void enviarMensajeTcpBroadcast(Object mensaje) {
        synchronized (conexionesCliente) {
            for (ClientConnection conexion : conexionesCliente) {
                conexion.enviarMensajeTcp(mensaje);
            }
        }
    }
    
    public void enviarMensajeTcp(Object mensaje) {
        if (!esHost) {
            try {
                if (outClienteTcp != null) {
                    outClienteTcp.writeObject(mensaje);
                    outClienteTcp.flush();
                } else {
                    Gdx.app.error("GestorRed", "ObjectOutputStream del cliente es nulo.");
                }
            } catch (IOException e) {
                Gdx.app.error("GestorRed", "Error al enviar mensaje TCP como cliente: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void enviarEstadoJuegoUdpBroadcast(EstadoJuego estadoJuego) {
        if (!esHost) return;
        this.estadoJuegoActual = estadoJuego;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(estadoJuego);
            oos.flush();
            byte[] data = baos.toByteArray();

            synchronized (conexionesCliente) {
                for (ClientConnection conexion : conexionesCliente) {
                    DatagramPacket paquete = new DatagramPacket(data, data.length, conexion.getDireccionCliente(), conexion.getPuertoUdpCliente());
                    socketUdp.send(paquete);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarEstadoEntrada(EstadoEntrada estadoEntrada) {
        if (esHost) return;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(estadoEntrada);
            oos.flush();
            byte[] data = baos.toByteArray();
            DatagramPacket paquete = new DatagramPacket(data, data.length, direccionServidor, puertoUdpServidor);
            socketUdp.send(paquete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EstadoJuego getEstadoJuegoActual() {
        return estadoJuegoActual;
    }

    public ConcurrentHashMap<Integer, EstadoEntrada> getEntradasJugador() {
        return entradasJugador;
    }
    public boolean esHost() {
        return esHost;
    }
    

    

    public void dispose() {
        if (esHost) {
            if (hiloDescubrimientoHost != null) hiloDescubrimientoHost.interrupt();
            enviarMensajeTcpBroadcast(new PaqueteApagado());
            try {
                if (socketTcpServidor != null) socketTcpServidor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (hiloRecepcionTcpCliente != null) hiloRecepcionTcpCliente.interrupt();
            try {
                if (outClienteTcp != null) outClienteTcp.close();
                if (inClienteTcp != null) inClienteTcp.close();
                if (socketTcpCliente != null) socketTcpCliente.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socketUdp != null) socketUdp.close();
        if (hiloRecepcionUdp != null) hiloRecepcionUdp.interrupt();
    }

    private class ClientConnection implements Runnable {
        private final Socket socketTcp;
        private final int idJugador;
        private ObjectOutputStream salidaTcp;
        private InetAddress direccionCliente;
        private int puertoUdpCliente;

        ClientConnection(Socket socket, int idJugador) {
            this.socketTcp = socket;
            this.idJugador = idJugador;
            this.direccionCliente = socket.getInetAddress();
            try {
                this.salidaTcp = new ObjectOutputStream(socketTcp.getOutputStream());
            } catch (IOException e) {
                Gdx.app.error("GestorRed", "Error al crear ObjectOutputStream para cliente " + idJugador + ": " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socketTcp.getInputStream())) {
                
                this.puertoUdpCliente = in.readInt();
                
                salidaTcp.writeInt(idJugador);
                salidaTcp.writeInt(socketUdp.getLocalPort());
                salidaTcp.flush();

                Gdx.app.log("GestorRed", "Cliente " + idJugador + " conectado desde " + direccionCliente.getHostAddress() + ":" + puertoUdpCliente);

                // Mantener el hilo vivo para escuchar mensajes TCP si es necesario en el futuro
                while (!socketTcp.isClosed() && !Thread.currentThread().isInterrupted()) {
                    Thread.sleep(1000);
                }

            } catch (IOException | InterruptedException e) {
                Gdx.app.log("GestorRed", "Cliente " + idJugador + " desconectado. Causa: " + e.getMessage());
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            } finally {
                conexionesCliente.remove(this);
                try {
                    if (!socketTcp.isClosed()) socketTcp.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void enviarMensajeTcp(Object mensaje) {
            try {
                if (salidaTcp != null) {
                    Gdx.app.log("GestorRed", "Servidor enviando mensaje TCP a cliente " + idJugador + ": " + mensaje.getClass().getSimpleName());
                    salidaTcp.writeObject(mensaje);
                    salidaTcp.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public InetAddress getDireccionCliente() {
            return direccionCliente;
        }

        public int getPuertoUdpCliente() {
            return puertoUdpCliente;
        }
    }
}
