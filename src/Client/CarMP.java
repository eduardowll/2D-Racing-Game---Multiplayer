package Client;

///////////////////////////////////////////
//
//  Sid: 1955004
//
///////////////////////////////////////////

import java.net.InetAddress;

public class CarMP extends Car {
    public InetAddress ipAddress;
    public int socketPort;

    private int targetX;
    private int targetY;
    private float suavizacao = 0.2f;

    // NOVA VARIÁVEL: Diz se o carro é seu ou do adversário
    public boolean isLocal = false;

    public CarMP(int PNum, InetAddress ipAddress, int socketPort) {
        super(PNum);
        this.ipAddress = ipAddress;
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
        super.animate();

        // SÓ FAZ A INTERPOLAÇÃO SE FOR O CARRO DO INIMIGO
        if (!isLocal) {
            int difX = targetX - getPositionX();
            int difY = targetY - getPositionY();
            this.setPositionX(getPositionX() + (int)(difX * suavizacao));
            this.setPositionY(getPositionY() + (int)(difY * suavizacao));
        }
    }
}
