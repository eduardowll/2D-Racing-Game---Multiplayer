package Server.Packets;

import Server.GameClient;
import Server.GameServer;
import Server.GameServerTCP;
///////////////////////////////////////////
//
//  Sid: 1955004
//
///////////////////////////////////////////

public class Packet01Disconnect extends Packet{
    private int pNum;

    //Receive player number from the server
    public Packet01Disconnect(byte[] data){
        super(01);
        this.pNum = Integer.parseInt(readData(data));
    }
    //send player number from the server
    public Packet01Disconnect(int pNum){
        super(01);
        this.pNum = pNum;
    }

    //TODO consider to use username or if the pnum can be done on the server or if the user shall choose
    @Override
    public void writeData(GameClient client) {
        client.sendData(getData());
    }

    @Override
    public void writeData(GameServer server) {
        server.sendDataToAllClients(getData());
    }

    @Override
    public byte[] getData() {
        return ("01"+this.pNum).getBytes();
    }

    public int getpNum(){
        return pNum;
    }

    public void writeData(GameServerTCP gameServerTCP) {

    }
}