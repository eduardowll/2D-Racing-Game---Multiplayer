package Server;

import Client.CarMP;
import Client.Panel;
import Server.Packets.Packet;
import Server.Packets.Packet00Login;
import Server.Packets.Packet01Disconnect;
import Server.Packets.Packet02Move;

import java.io.IOException;
import java.net.*;
///////////////////////////////////////////
//
//  Sid: 1955004
//
///////////////////////////////////////////

public class GameClient extends Thread{

    private InetAddress ipAddress;
    private DatagramSocket socket;
    private Panel panel;

    private int socket_port = 5000;

    public GameClient(String ipAddress, Panel panel){
        this.panel = panel;
        try {
            this.ipAddress = InetAddress.getByName(ipAddress);
            this.socket = new DatagramSocket();
        }catch (SocketException | UnknownHostException e){
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

            this.parsePacket(packet.getData(), packet.getAddress(),packet.getPort());
            /*String message = new String(packet.getData());
            System.out.println("SERVER > "+message);*/

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
                packet = new Packet00Login(data);
                handleLogin(((Packet00Login) packet),address,socket_port);
                break;
            case DISCONNECT:
                packet = new Packet01Disconnect(data);
                System.out.println("Player: "+((Packet01Disconnect)packet).getpNum()+" has left, couldn't handle the challenge...");
                break;
            case MOVE:
                packet = new Packet02Move(data);
                handleMove((Packet02Move) packet);
                //System.out.println("Player: "+ ((Packet02Move)packet).getpNum()+" sent "+(Packet02Move) packet);
        }
    }
    public void sendData (byte[] data){
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, socket_port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void handleLogin(Packet00Login packet, InetAddress address, int port) {
        int pNum = ((Packet00Login)packet).getpNum();
        CarMP player = new CarMP(pNum,address,socket_port);
        System.out.println("Player: "+((Packet00Login)packet).getpNum()+" has joined the game...");
        panel.addCar(player.getpNum(),player);
    }

    private void handleMove(Packet02Move packet) {
        long tempoChegada = System.currentTimeMillis();
        long atraso = tempoChegada - packet.getTimestamp();
        System.out.println("Atraso (Ping) do jogador " + packet.getpNum() + ": " + atraso + " ms");

        // 2. Pega o carro adversário no Panel
        CarMP enemyCar = (CarMP) this.panel.getCar(packet.getpNum());

        // 3. Se o carro existir, atualiza o alvo dele e os status
        if (enemyCar != null) {
            // Define o ALVO para a interpolação atuar suavemente
            enemyCar.updateTarget(packet.getX(), packet.getY());

            // Atualiza a direção e a velocidade para a extrapolação continuar prevendo o movimento
            enemyCar.setDirection(packet.getDirection());
            enemyCar.setSpeed(packet.getSpeed());

            // Atualiza os outros status normais
            enemyCar.setAlert(packet.getAlert());
            enemyCar.setStatus(packet.getStatus());
            enemyCar.setReady(packet.isReady());
        }
    }


}
