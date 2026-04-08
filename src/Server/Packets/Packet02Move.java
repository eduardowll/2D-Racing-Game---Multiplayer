package Server.Packets;

import Server.GameClient;
import Server.GameServer;

public class Packet02Move extends Packet {
    private int pNum;
    private int x, y;
    private int speed;
    private int direction = 4;
    private String alert = "";
    private String status = "";
    private boolean isReady;
    private long timestamp;

    public Packet02Move(byte[] data) {
        super(02);
        String message = readData(data);
        String[] dataArray = message.split(",");

        // O SEGREDO ESTAVA AQUI! Tira o "02" que estava grudado no número do player
        if (dataArray.length >= 1) {
            String pNumStr = dataArray[0];
            if (pNumStr.startsWith("02")) {
                this.pNum = Integer.parseInt(pNumStr.substring(2));
            } else {
                this.pNum = Integer.parseInt(pNumStr);
            }
        }

        if (dataArray.length >= 2) this.x = Integer.parseInt(dataArray[1]);
        if (dataArray.length >= 3) this.y = Integer.parseInt(dataArray[2]);
        if (dataArray.length >= 4) this.direction = Integer.parseInt(dataArray[3]);
        if (dataArray.length >= 5) this.speed = Integer.parseInt(dataArray[4]);
        if (dataArray.length >= 6) this.alert = dataArray[5];
        if (dataArray.length >= 7) this.status = dataArray[6];
        if (dataArray.length >= 8) {
            this.isReady = dataArray[7].equalsIgnoreCase("true");
        }
        if (dataArray.length >= 9) {
            this.timestamp = Long.parseLong(dataArray[8]);
        } else {
            this.timestamp = System.currentTimeMillis();
        }
    }

    public Packet02Move(int pNum, int x, int y, int direction, int speed, String alert, String status, boolean isReady) {
        super(02);
        this.pNum = pNum;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.speed = speed;
        this.alert = (alert == null) ? "" : alert;
        this.status = (status == null) ? "" : status;
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
        String safeAlert = (alert == null || alert.isEmpty()) ? " " : alert;
        String safeStatus = (status == null || status.isEmpty()) ? " " : status;

        return ("02" + this.pNum + "," + this.x + "," + this.y +
                "," + this.direction + "," + this.speed + "," +
                safeAlert + "," + safeStatus + "," + isReady + "," + this.timestamp).getBytes();
    }

    public int getpNum() { return this.pNum; }
    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public int getDirection() { return this.direction; }
    public int getSpeed() { return this.speed; }
    public String getAlert() { return this.alert; }
    public String getStatus() { return this.status; }
    public boolean isReady() { return this.isReady; }
    public long getTimestamp() { return this.timestamp; }
}