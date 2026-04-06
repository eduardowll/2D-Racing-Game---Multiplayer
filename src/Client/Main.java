package Client;

///////////////////////////////////////////
//
//  Sid: 1955004
//
///////////////////////////////////////////
/**
 Set ipAddress with "localhost" or preferred ipAddresses
 Set nLaps to desired number of laps
 **/
///////////////////////////////////////////
public class Main {


    static String ipAddress = "26.210.145.27";
    static int nLaps=1;


    public static void main(String[] args) {
        Frame frame = new Frame(ipAddress,nLaps);
        frame.setVisible(true);

    }
}