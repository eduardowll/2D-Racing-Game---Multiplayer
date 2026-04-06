package Client;
///////////////////////////////////////////
//
//  Sid: 1955004
//
///////////////////////////////////////////
import Server.Packets.Packet00Login;
import Server.GameClient;
import Server.GameServer;
import Server.Packets.Packet01Disconnect;
import Server.Packets.Packet02Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.TimeUnit;

public class Panel extends JPanel implements KeyListener {

    //modify number of laps
    private final int nLaps;
    private final static int RIGHT = 4;
    private final int fps=60;
    private final String initLab = "<html>Press 'z' and 'm' to set Ready. Press 'r' to request a restart. </html>";
    private Rectangle grassField,outerEdge,gameField,cP1,cP2,cP3,cP4,cP5;
    Car redCar=new CarMP(1,null,-1);
    Car blueCar =new CarMP(2,null,-1);
    JLabel redLabel = new JLabel("<html>Red Car Lap: <br/> Red Car Speed: <br/>Alert: ");
    JLabel blueLabel = new JLabel("<html>Blue Car Lap: <br/> Blue Car Speed: <br/>Alert: ");
    JLabel fpsLabel = new JLabel("FPS: ");
    JLabel controlsLabel = new JLabel(initLab);
    private boolean redPlayed, bluePlayed =false;
    private Timer animationTimer;
    Match match = new Match();
    public GameClient socketClient;
    public GameServer socketServer;

    private int pNum;

    public int getpNum(){
        return this.pNum;
    }

    public Panel(String ipAddress,int nLaps){
        this.nLaps = nLaps;
        setDoubleBuffered(true);
        addKeyListener(this);
        init(ipAddress);

    }
    private synchronized void init(String ipAddress){
        match.setLaps(nLaps);
        if(JOptionPane.showConfirmDialog(this, "Do you want to run the server?")== 0){
            socketServer = new GameServer(this);
            socketServer.start();
        }

        socketClient = new GameClient(ipAddress,this);
        socketClient.start();


        if(socketServer != null){
            pNum=1;
            Packet00Login loginPacket = new Packet00Login(redCar.getpNum(),redCar.getPositionX(),redCar.getPositionY());
            socketServer.addConnection((CarMP)redCar,loginPacket);
            loginPacket.writeData(socketClient);
        }else {
            pNum=2;
            Packet00Login loginPacket = new Packet00Login(blueCar.getpNum(),blueCar.getPositionX(),blueCar.getPositionY());
            loginPacket.writeData(socketClient);
        }


    }

    private void update() {
    }

    // Método para o GameClient conseguir pegar o carro pelo número do jogador
    public Car getCar(int pNum) {
        // Se o número for 1, retorna o carro 1, se for 2, retorna o carro 2.
        if (pNum == 1) {
            return redCar;
        } else if (pNum == 2) {
            return blueCar;
        }
        return null;
    }

    public synchronized void sendUpdate(){
        if (pNum == 1) {
            Packet02Move packet = new Packet02Move(redCar.getpNum(), redCar.getPositionX(), redCar.getPositionY(), redCar.getDirection(), redCar.getSpeed(), redCar.getAlert(), redCar.getStatus(), redCar.isReady());
            packet.writeData(socketClient);
        } else if (pNum == 2) {
            Packet02Move packet = new Packet02Move(blueCar.getpNum(), blueCar.getPositionX(), blueCar.getPositionY(), blueCar.getDirection(), blueCar.getSpeed(), blueCar.getAlert(), blueCar.getStatus(), blueCar.isReady());
            packet.writeData(socketClient);
        }
    }

    public void paintComponent(Graphics g){

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        requestFocusInWindow();

        //paint gui components
        paintTrack(g2);
        paintMenus(g2);

        if (match.isStarted()){
            redCar.setStatus("Matching");
            blueCar.setStatus("Matching");
        }

        redCar.draw(g2,this);
        blueCar.draw(g2,this);

        redCar.countLaps(match,cP1,cP2,cP3,cP4,cP5,gameField);
        blueCar.countLaps(match,cP1,cP2,cP3,cP4,cP5,gameField);

        blueCar.animate();
        redCar.animate();

        //check if the car went out of track or not
        if(redCar.getSpeed()!=10){redCar.setCrashed(false);}
        if (!redCar.isCrashed()){
            if(redCar.checkCollision()){
                redCar.setSpeed(Car.MIN_SPEED);
            }
        }
        if(blueCar.getSpeed()!=10){blueCar.setCrashed(false);}
        if (!blueCar.isCrashed()) {
            if (blueCar.checkCollision()) {
                blueCar.setSpeed(Car.MIN_SPEED);
            }
        }
        //check if cars have crashed into each other
        redCar.checkCrash(redCar,blueCar);

        if(match.isFinished()) switch (match.getWinner()) {
            case 1 -> {
                redCar.setAlert(match.winMessage());
                blueCar.setAlert(match.loserMessage());
                match.setStarted(false);
                redCar.setStatus("Finished.");
                blueCar.setStatus("Finished.");

            }
            case 2 -> {
                blueCar.setAlert(match.winMessage());
                redCar.setAlert(match.loserMessage());
                match.setStarted(false);
                redCar.setStatus("Finished.");
                blueCar.setStatus("Finished.");

            }
        }

    }


    //graphics

    private synchronized void paintMenus(Graphics2D g2){
        Color c5 = new Color(190, 88, 88);//red
        g2.setColor( c5 );
        g2.fillRect( 0, 0, 200, 700 ); //80 height; left panel
        Color c6 = new Color(0xFF07BBCD, true);//blue
        g2.setColor( c6 );
        g2.fillRect( 1050, 0, 200, 700 ); //80 height; right panel
        //g2.dispose();

        if (pNum==1) {
            //Player one menu
            redLabel.setText("<html>Player: 1<br/>Red Car Lap: " + redCar.getLap() + "<br/> " +
                    "Red Car Speed: " + redCar.getSpeed() + " mph <br/>" +
                    "Status: " + redCar.getAlert() + "<br/>" +
                    redCar.getStatus() + "<br/><br/>" +
                    "A: Left" + "<br/>" +
                    "D: Right" + "<br/>" +
                    "W: Speed Up" + "<br/>" +
                    "S: Speed Down" + "<br/>" +
                    "</html>");
            redLabel.setVerticalTextPosition(SwingConstants.TOP);
            redLabel.setForeground(Color.white);
            redLabel.setVisible(true);
            //blueLabel.setLocation(,0);
            redLabel.setLocation(0, 0);
            redLabel.setSize(200, 400);
            this.add(redLabel);
        }else if (pNum == 2) {
            //player two menu
            blueLabel.setText("<html>Player: 2<br/>Blue Car Lap: " + blueCar.getLap() + "<br/> " +
                    "Blue Car Speed: " + blueCar.getSpeed() + " mph <br/>" +
                    "Status: " + blueCar.getAlert() + "<br/>" +
                    blueCar.getStatus() + "<br/><br/>" +
                    "A: Left" + "<br/>" +
                    "D: Right" + "<br/>" +
                    "W: Speed Up" + "<br/>" +
                    "S: Speed Down" + "<br/>" +
                    "</html>");
            blueLabel.setVerticalTextPosition(SwingConstants.TOP);
            blueLabel.setForeground(Color.white);
            blueLabel.setVisible(true);
            blueLabel.setLocation(1080, 0);
            blueLabel.setSize(170, 400);
            this.add(blueLabel);
        }
        //fps Menu
        fpsLabel.setText("FPS: " +fps);
        fpsLabel.setLocation(0,610);
        fpsLabel.setForeground(Color.white);
        fpsLabel.setVisible(true);
        fpsLabel.setSize(100 , 50);
        this.add(fpsLabel);

        //controls instructions
        controlsLabel.setForeground(Color.white);
        controlsLabel.setVisible(true);
        controlsLabel.setLocation(210,610);
        controlsLabel.setSize(600 , 50);
        this.add(controlsLabel);
    }

    private void paintTrack(Graphics2D g2){
        Color c1 = Color.darkGray; //outer edge
        g2.setColor( c1 );
        outerEdge = new Rectangle(250, 100, 750, 500);
        g2.fillRect(outerEdge.x,outerEdge.y,outerEdge.width,outerEdge.height);  // outer edge
        gameField = new Rectangle(200,40,850,610);
        //g2.fillRect( 150, 200, 550, 300 ); // inner edge

        Color c2 = Color.yellow;//
        g2.setColor( c2 );
        g2.drawRect( 300, 150, 650, 400 ); // mid-lane marker

        Color c3 = Color.white;
        g2.setColor( c3 );

        g2.drawLine( 625, 500, 625, 600 ); // start line

        Color c4 = Color.green; //grass
        g2.setColor( c4 );
        grassField = new Rectangle(350, 200, 550, 300);
        g2.fillRect( grassField.x,grassField.y,grassField.width,grassField.height ); // grass

        //Set the checkpoints in the track
        Color c6 = Color.yellow;
        g2.setColor(c6);
        cP1 = new Rectangle(870,468,150,150);
        cP2 = new Rectangle(870,60,150,150);
        cP3 = new Rectangle(210,60,150,150);
        cP4 = new Rectangle(210,468,150,150);
        cP5 = new Rectangle(611,468,40,150);


        //uncomment to see the invisible checkpoints
/*        g2.fillRect(cP1.x,cP1.y,cP1.width,cP1.height);
        g2.fillRect(cP2.x,cP2.y,cP2.width,cP2.height);
        g2.fillRect(cP3.x,cP3.y,cP3.width,cP3.height);
        g2.fillRect(cP4.x,cP4.y,cP4.width,cP4.height);
        g2.fillRect(cP5.x,cP5.y,cP5.width,cP5.height);*/
    }




    //Key listener
    @Override //red car controls
    public void keyTyped(KeyEvent e) {
        //System.out.println("key is typed");
        if (pNum == 1) {
            if (!redCar.isReady()) {
                if (e.getKeyChar() == 'z') {
                    redCar.setReady(true);
                    redCar.setStatus("Ready");
                    if((redCar.isReady() && blueCar.isReady())){
                        try {
                            redCar.audioPlayer(3);
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        match.setStarted(true);
                        //sendUpdate();
                    }
                }
            }else if(blueCar.isReady()) {

                if (e.getKeyChar() == 'w') {
                    //System.out.println("key is up");
                    if (redCar.getSpeed() < Car.MAX_SPEED) {
                        redCar.increaseSpeed();
                    }
                    //System.out.println("red car speed: " + redCar.getSpeed());

                } else if (e.getKeyChar() == 'd') {
                    //System.out.println("key is right");
                    if (redCar.getDirection() == 15) {
                        redCar.setDirection(0);
                    } else {
                        redCar.setDirection(redCar.getDirection() + 1);

                    }
                } else if (e.getKeyChar() == 'a') {
                    //System.out.println("key is left");
                    if (redCar.getDirection() == 0) {
                        redCar.setDirection(15);
                    } else {
                        redCar.setDirection(redCar.getDirection() - 1);
                    }
                } else if (e.getKeyChar() == 's') {
                    //System.out.println("key is down");
                    if (redCar.getSpeed() > Car.MIN_SPEED - 10) {
                        redCar.decreaseSpeed();
                    }
                    //System.out.println("red car speed: " + redCar.getSpeed());

                }
            }
        }else if (pNum == 2){
            if (!blueCar.isReady()) {
                if (e.getKeyChar() == 'm') {
                    blueCar.setReady(true);
                    blueCar.setStatus("Ready");
                    if((redCar.isReady() && blueCar.isReady())){
                        try {
                            redCar.audioPlayer(3);
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        /*blueCar.increaseSpeed();
                        redCar.increaseSpeed();*/
                        match.setStarted(true);
                        //sendUpdate();
                    }
                }
            } else if (redCar.isReady()) {
                if (e.getKeyChar() == 'w') {
                    //System.out.println("key is up");
                    if (blueCar.getSpeed() < Car.MAX_SPEED) {
                        blueCar.increaseSpeed();
                    }
                    //System.out.println("blue car speed: " + blueCar.getSpeed());
                } else if (e.getKeyChar() == 'd') {
                    //System.out.println("key is right");
                    if (blueCar.getDirection() == 15) {
                        blueCar.setDirection(0);
                    } else {
                        blueCar.setDirection(blueCar.getDirection() + 1);

                    }
                } else if (e.getKeyChar() == 'a') {
                    //System.out.println("key is left");
                    if (blueCar.getDirection() == 0) {
                        blueCar.setDirection(15);
                    } else {
                        blueCar.setDirection(blueCar.getDirection() - 1);
                    }
                } else if (e.getKeyChar() == 's') {
                    //System.out.println("key is down");
                    if (blueCar.getSpeed() > Car.MIN_SPEED - 10) {
                        blueCar.decreaseSpeed();
                    }
                    //System.out.println("blue car speed: " + blueCar.getSpeed());

                } else if (e.getKeyChar() == 'r') {
                    restart();
                }
            }
        }
    }




    @Override//blue car controls
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public void playSounds(){
        if(redCar.getAlert().equals("")){
            redPlayed = false;
            redCar.isPlayed = false;
        }
        if(redCar.getAlert().equals("Hit the wall.") && !redPlayed){
            redCar.audioPlayer(2);
            redPlayed=true;
        } else if (redCar.getAlert().equals("Hit the grass Field.") && !redPlayed){
            redCar.audioPlayer(2);
            redPlayed=true;
        } else if (redCar.getAlert().equals("Winner!") && !redPlayed) {
            redCar.audioPlayer(4);
            redPlayed = true;
        }
        if(blueCar.getAlert().equals("")){
            bluePlayed = false;
            blueCar.isPlayed = false;
        }
        if(blueCar.getAlert().equals("Hit the wall.") && !bluePlayed){
            redCar.audioPlayer(2);
            bluePlayed=true;
        } else if (blueCar.getAlert().equals("Hit the grass Field.") && !bluePlayed){
            redCar.audioPlayer(2);
            bluePlayed=true;
        } else if (blueCar.getAlert().equals("Winner!") && !bluePlayed) {
            redCar.audioPlayer(4);
            bluePlayed = true;
        }
    }




    //timer start
    public void startAnimation() {
        if (animationTimer == null) {
            animationTimer = new Timer(1000/60, new TimeHandler());
            animationTimer.start();

            //socketClient.sendData("ping".getBytes());
        } else {
            if (!animationTimer.isRunning())
                animationTimer.restart();
        }//ifStatement
    }

    public void addCar(int pNum, CarMP car) {
        if (pNum == 1){
            this.redCar = car;
        } else if (pNum == 2) {
            this.blueCar = car;
        }else{
            System.out.println("player number invalid");
        }

    }

    public void restart(){

        int result=JOptionPane.showConfirmDialog(null, "<html>Player: "+match.getWinner()+" Wins! </br> Would you like to play again?</html>");
        switch (result) {
            case JOptionPane.YES_OPTION:
                //reset all settings
                redCar.setSpeed(0);
                redCar.setPositionX(580);
                redCar.setPositionY(550);
                redCar.setLap(0);
                redCar.setAlert("");
                redCar.setDirection(RIGHT);
                redCar.setStatus("Press 'Z' to start.");
                blueCar.setSpeed(0);
                blueCar.setPositionX(575);
                blueCar.setPositionY(500);
                blueCar.setLap(0);
                blueCar.setAlert("");
                blueCar.setDirection(RIGHT);
                blueCar.setStatus("Press 'm' to start.");
                match.setFinished(false);
                match.setWinner(0);
                match.setP1Ready(false);
                match.setP2Ready(false);
                redCar.setReady(false);
                blueCar.setReady(false);
                match.setStarted(false);
                blueCar.isPlayed = false;
                redCar.isPlayed = false;

                redCar.setCheckpoint(0);
                blueCar.setCheckpoint(0);
                break;
            case JOptionPane.NO_OPTION:
                //disconnect user from the server
                //close program.
                Packet01Disconnect packet = new Packet01Disconnect(this.getpNum());
                packet.writeData(this.socketClient);
                System.exit(0);
                break;
            case JOptionPane.CLOSED_OPTION:
                System.out.println("Closed");
                break;
        }
    }

    public void moveCars(int pNum, int x, int y, int direction, int speed,String alert,String status,boolean isReady) {
        if (pNum == 1){
            this.redCar.setPositionX(x);
            this.redCar.setPositionY(y);
            this.redCar.setDirection(direction);
            this.redCar.setSpeed(speed);
            this.redCar.setAlert(alert);
            this.redCar.setStatus(status);
            this.redCar.setReady(isReady);
        } else if(pNum == 2) {
            this.blueCar.setPositionX(x);
            this.blueCar.setPositionY(y);
            this.blueCar.setDirection(direction);
            this.blueCar.setSpeed(speed);
            this.blueCar.setAlert(alert);
            this.blueCar.setStatus(status);
            this.blueCar.setReady(isReady);
        }else{
            System.out.println("player number invalid");
        }
    }

    private class TimeHandler implements ActionListener {
        public synchronized void actionPerformed(ActionEvent actionEvent) {
            //update();
            playSounds();
            remove(redLabel);
            remove(blueLabel);
            remove(fpsLabel);
            sendUpdate();
            if(match.isFinished()) {
                restart();
            }
            //match.saveMatch();

            repaint();

        }//ActionPerformed()
    }
}
