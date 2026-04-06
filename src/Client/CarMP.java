package Client;

///////////////////////////////////////////
//
//  Sid: 1955004
//
///////////////////////////////////////////

import java.net.InetAddress;

public class CarMP extends Car{
    public InetAddress ipAddress;
    public int socketPort;
    private int targetX, targetY; // A posição que veio do servidor
    private float interpolationFactor = 0.1f; // O quão suave será o deslize (0.1 a 0.5)

    //multiplayer car class
    public CarMP(int PNum,InetAddress ipAddress, int socketPort) {
        super(PNum);
        this.ipAddress=ipAddress;
        this.socketPort = socketPort;
        this.targetX = getPositionX();
        this.targetY = getPositionY();
    }

    public void updateTarget(int x, int y) {
        this.targetX = x;
        this.targetY = y;
    }

    @Override
    public void animate() {
        // 1. EXTRAPOLAÇÃO: Rodamos o animate original do Car.java.
        super.animate();

        // 2. INTERPOLAÇÃO: Corrigimos a posição suavemente em direção ao alvo real do servidor.
        int difX = targetX - getPositionX();
        int difY = targetY - getPositionY();

        this.setPositionX(getPositionX() + (int)(difX * interpolationFactor));
        this.setPositionY(getPositionY() + (int)(difY * interpolationFactor));
    }
}
