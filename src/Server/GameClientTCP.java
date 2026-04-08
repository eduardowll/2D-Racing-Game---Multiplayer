package Server;

import Client.CarMP;
import Client.Panel;
import Server.Packets.Packet;
import Server.Packets.Packet00Login;
import Server.Packets.Packet01Disconnect;
import Server.Packets.Packet02Move;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class GameClientTCP extends Thread {

    private Socket socket;
    private Panel panel;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private String ipAddress;
    private int serverPort = 5000;
    private boolean running = true;

    public GameClientTCP(String ipAddress, Panel panel) {
        this.panel = panel;
        this.ipAddress = ipAddress;
        connect();
    }

    private void connect() {
        try {
            this.socket = new Socket(ipAddress, serverPort);
            this.dataOut = new DataOutputStream(socket.getOutputStream());
            this.dataIn = new DataInputStream(socket.getInputStream());
            System.out.println("Conectado ao servidor TCP em " + ipAddress);
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor TCP: " + e.getMessage());
        }
    }

    public void run() {
        while (running) {
            try {
                if (socket == null || socket.isClosed()) {
                    Thread.sleep(2000);
                    connect();
                    continue;
                }

                int length = dataIn.readInt();
                if (length > 0 && length < 10000) {
                    byte[] packetData = new byte[length];
                    dataIn.readFully(packetData);
                    this.parsePacket(packetData, socket.getInetAddress(), socket.getPort());
                }
            } catch (EOFException e) {
                System.out.println("Servidor fechou a conexão TCP.");
                closeSocket();
            } catch (IOException e) {
                System.err.println("Erro de rede TCP: " + e.getMessage());
                closeSocket();
            } catch (Exception e) {
                System.err.println("Erro inesperado no GameClientTCP: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void closeSocket() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parsePacket(byte[] data, InetAddress address, int socket_port) {
        try {
            String message = new String(data).trim();
            if (message.length() < 2) return;

            Packet.PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
            switch (type) {
                case LOGIN:
                    handleLogin(new Packet00Login(data), address, socket_port);
                    break;
                case DISCONNECT:
                    System.out.println("Player desconectou.");
                    break;
                case MOVE:
                    handleMove(new Packet02Move(data));
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar pacote TCP: " + e.getMessage());
        }
    }

    public synchronized void sendData(byte[] data) {
        try {
            if (socket != null && !socket.isClosed() && dataOut != null) {
                dataOut.writeInt(data.length);
                dataOut.write(data);
                dataOut.flush();
            }
        } catch (IOException e) {
            System.err.println("Erro ao enviar dados TCP: " + e.getMessage());
        }
    }

    private void handleLogin(Packet00Login packet, InetAddress address, int port) {
        CarMP player = new CarMP(packet.getpNum(), address, serverPort);
        panel.addCar(player.getpNum(), player);
    }

    private void handleMove(Packet02Move packet) {
        // --- ADICIONADO O CÁLCULO DE PING AQUI ---
        // Se o pacote não for o seu próprio carro local, ele mede o Ping!
        if (packet.getpNum() != this.panel.getpNum()) {
            long tempoChegada = System.currentTimeMillis();
            long atraso = tempoChegada - packet.getTimestamp();
            System.out.println("PING [TCP] - Atraso do Jogador " + packet.getpNum() + ": " + atraso + " ms");
        }

        CarMP enemyCar = (CarMP) this.panel.getCar(packet.getpNum());
        if (enemyCar != null) {
            enemyCar.updateTarget(packet.getX(), packet.getY());
            enemyCar.setDirection(packet.getDirection());
            enemyCar.setSpeed(packet.getSpeed());
            enemyCar.setAlert(packet.getAlert());
            enemyCar.setStatus(packet.getStatus());
            enemyCar.setReady(packet.isReady());
        }
    }

    public void stopClient() {
        this.running = false;
        closeSocket();
    }
}
