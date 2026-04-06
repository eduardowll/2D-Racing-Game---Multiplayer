package Server.Packets;

import Server.GameClient;
import Server.GameServer;

import static java.lang.Boolean.parseBoolean;
///////////////////////////////////////////
//
//  Sid: 1955004
//
///////////////////////////////////////////
public class Packet02Move extends Packet{
    private int pNum;
    private int x, y;

    private int speed;
    private int direction = 4;
    private String alert;
    private String status;
    private boolean isReady;

    private long timestamp;

    public Packet02Move(byte[] data) {
        super(02);
        String[] dataArray = readData(data).split(",");
        this.pNum = Integer.parseInt(dataArray[0]);
        this.x = Integer.parseInt(dataArray[1]);
        this.y = Integer.parseInt(dataArray[2]);
        this.direction = Integer.parseInt(dataArray[3]);
        this.speed = Integer.parseInt(dataArray[4]);
        this.alert = dataArray[5];
        this.status = dataArray[6];
        if(dataArray[7].equalsIgnoreCase("false")){
            this.isReady=false;
        }else{
            this.isReady=true;
        }
        this.timestamp = Long.parseLong(dataArray[8]);

    }

    public Packet02Move(int pNum, int x, int y, int direction, int speed,String alert,String status, boolean isReady) {
        super(02);
        this.pNum = pNum;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.speed = speed;
        this.alert = alert;
        this.status = status;
        this.isReady = isReady;

        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void writeData(GameServer server) {
        server.sendDataToAllClients(getData());
    }

    @Override
    public void writeData(GameClient client) {
        client.sendData(getData());
    }

    @Override
    public byte[] getData() {
        return ("02" + this.pNum + "," + this.x + "," + this.y +
                "," + this.direction + "," + this.speed +"," +
                this.alert + "," + this.status+ "," + isReady + "," + this.timestamp).getBytes();

    }

    public int getpNum(){
        return this.pNum;
    }


    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getDirection() {
        return this.direction;
    }

    public int getSpeed() {
        return this.speed;
    }
    public String getAlert() {
        return this.alert;
    }
    public String getStatus() {
        return this.status;
    }
    public boolean isReady() {
        return this.isReady;
    }

    public long getTimestamp() {
        return this.timestamp;
    }


}
