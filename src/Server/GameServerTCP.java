package Server;

import Client.CarMP;
import Client.Panel;
import Server.Packets.Packet;
import Server.Packets.Packet00Login;
import Server.Packets.Packet01Disconnect;
import Server.Packets.Packet02Move;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServerTCP extends Thread {
    private Panel panel;
    private ServerSocket serverSocket;
    private int port = 5000;

    // Listas obrigatórias para o servidor funcionar
    private int nConnections = 0;
    private List<CarMP> connectedPlayers = new ArrayList<>();
    private List<ClientHandler> clients = new ArrayList<>();

    public GameServerTCP(Panel panel) {
        this.panel = panel;
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("Servidor TCP rodando na porta " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo jogador conectou via TCP: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                handler.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Tem que ser PUBLIC para o ClientHandler conseguir acessar
    public void parsePacket(byte[] data, InetAddress address, int socket_port) {
        String message = new String(data).trim();
        Packet.PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
        Packet packet = null;
        switch (type) {
            default:
            case INVALID:
                break;
            case LOGIN:
                if (nConnections >= 2) {
                    System.out.println("Two players maximum");
                } else if (nConnections >= 0) {
                    nConnections++;
                    packet = new Packet00Login(data);
                    CarMP player = new CarMP(nConnections, address, socket_port);
                    System.out.println("Player: " + player.getpNum() + " has connected...");
                    this.addConnection(player, (Packet00Login) packet);
                }
                break;
            case DISCONNECT:
                nConnections--;
                packet = new Packet01Disconnect(data);
                System.out.println("Player: " + ((Packet01Disconnect) packet).getpNum() + " has left...");
                this.removeConnection((Packet01Disconnect) packet);
                break;
            case MOVE:
                packet = new Packet02Move(data);
                this.handleMove(((Packet02Move) packet));
        }
    }

    private void handleMove(Packet02Move packet) {
        if (getCarMP(packet.getpNum()) != null) {
            int index = getCarMPIndex(packet.getpNum());
            CarMP player = this.connectedPlayers.get(index);
            player.setPositionX(packet.getX());
            player.setPositionY(packet.getY());
            player.setDirection(packet.getDirection());
            player.setSpeed(packet.getSpeed());
            player.setAlert(packet.getAlert());
            player.setStatus(packet.getStatus());
            player.setReady(packet.isReady());
            this.sendDataToAllClients(packet.getData());
        }
    }

    public void addConnection(CarMP player, Packet00Login packet) {
        boolean alreadyConnected = false;
        for (CarMP c : this.connectedPlayers) {
            if (player.getpNum() == c.getpNum()) {
                if (c.ipAddress == null) {
                    c.ipAddress = player.ipAddress;
                }
                if (c.socketPort == -1) {
                    c.socketPort = player.socketPort;
                }
                alreadyConnected = true;
            } else {
                // Adaptação para o TCP
                sendDataToAllClients(packet.getData());
                Packet00Login p = new Packet00Login(c.getpNum(), c.getPositionX(), c.getPositionY());
                sendDataToAllClients(p.getData());
            }
        }
        if (!alreadyConnected) {
            this.connectedPlayers.add(player);
        }
    }

    public void removeConnection(Packet01Disconnect packet) {
        this.connectedPlayers.remove(getCarMPIndex(packet.getpNum()));
        this.sendDataToAllClients(packet.getData());
    }

    public CarMP getCarMP(int pNum) {
        for (CarMP player : this.connectedPlayers) {
            if (player.getpNum() == pNum) {
                return player;
            }
        }
        return null;
    }

    public int getCarMPIndex(int pNum) {
        int index = 0;
        for (CarMP player : this.connectedPlayers) {
            if (player.getpNum() == pNum) {
                break;
            }
            index++;
        }
        return index;
    }

    // No TCP, mandamos para todo mundo usando a lista de ClientHandlers
    public void sendDataToAllClients(byte[] data) {
        for (ClientHandler handler : clients) {
            handler.sendData(data);
        }
    }

    // Mantido apenas para compatibilidade com as classes Packet
    public void sendData(byte[] data, InetAddress ipAddress, int port) {
        sendDataToAllClients(data);
    }
}
