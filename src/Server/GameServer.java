package Server;

import Client.CarMP;
import Client.Panel;
import Server.Packets.Packet;
import Server.Packets.Packet00Login;
import Server.Packets.Packet01Disconnect;
import Server.Packets.Packet02Move;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
///////////////////////////////////////////
//
//  Sid: 1955004
//
///////////////////////////////////////////

public class GameServer extends Thread  {

    private DatagramSocket socket;
    private Panel panel; // may be changed look what game is


    private int socket_port = 5000;


    private int nConnections=0;

    private List<CarMP> connectedPlayers = new ArrayList<CarMP>();

    public GameServer( Panel panel){
        this.panel = panel;
        try {
            this.socket = new DatagramSocket(socket_port);
        }catch (SocketException e){
            e.printStackTrace();
        }

    }
    public void run(){
        while(true){
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            parsePacket(packet.getData(), packet.getAddress(), packet.getPort());


            /*String message = new String(packet.getData());
            //if data received is ping, reply pong
            System.out.println("CLIENT > " + message);
            if (message.trim().equalsIgnoreCase("ping")) {
                sendData("pong".getBytes(),packet.getAddress(),packet.getPort());
            }*/
        }
    }
    private void parsePacket(byte[] data, InetAddress address, int socket_port){
        String message = new String(data).trim();
        Packet.PacketTypes type = Packet.lookupPacket(message.substring(0,2));
        Packet packet = null;
        switch (type){
            default:
            case INVALID:
                break;
            case LOGIN:
                if(nConnections >=2){
                    System.out.println("Two players maximum");
                }else if(nConnections >= 0) {
                    nConnections++;
                    packet = new Packet00Login(data);
                    CarMP player = new CarMP(nConnections,address,socket_port);
                    System.out.println("Player: "+player.getpNum()+" has connected...");
                    this.addConnection(player,(Packet00Login)packet);
                }
                break;

            case DISCONNECT:
                nConnections--;
                packet = new Packet01Disconnect(data);
                System.out.println("Player: "+((Packet01Disconnect)packet).getpNum()+" has left...");
                this.removeConnection((Packet01Disconnect)packet);

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
            packet.writeData(this);
        }
    }


    public void addConnection(CarMP player, Packet00Login packet) {
        boolean alreadyConnected= false;
        for(CarMP c : this.connectedPlayers) {
            if(player.getpNum() == c.getpNum()){
                if(c.ipAddress == null){
                    c.ipAddress = player.ipAddress;
                }
                if (c.socketPort == -1){
                    c.socketPort = player.socketPort;
                }
                alreadyConnected =true;
            }else {
                sendData(packet.getData(), c.ipAddress,c.socketPort);

                packet = new Packet00Login(c.getpNum(),c.getPositionX(),c.getPositionY());
                sendData(packet.getData(),player.ipAddress, player.socketPort);
            }
        }
        if (!alreadyConnected) {
            this.connectedPlayers.add(player);
        }
    }


    public void removeConnection(Packet01Disconnect packet) {
        this.connectedPlayers.remove(getCarMPIndex(packet.getpNum()));
        packet.writeData(this);
    }

    //check if the player is connected to server
    public CarMP getCarMP(int pNum){
        for(CarMP player: this.connectedPlayers){
            if (player.getpNum() == pNum){
                return player;
            }
        }
        return null;
    }

    //check the index of the player in the list of users
    public int getCarMPIndex(int pNum){
        int index=0;
        for(CarMP player: this.connectedPlayers){
            if (player.getpNum() == pNum){
                break;
            }
            index++;
        }
        return index;
    }

    public void sendData (byte[] data, InetAddress ipAddress, int port){
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendDataToAllClients(byte[] data) {
        for(CarMP c : connectedPlayers){
            sendData(data,c.ipAddress, c.socketPort);
        }
    }
}
