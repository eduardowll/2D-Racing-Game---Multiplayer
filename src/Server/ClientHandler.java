package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private GameServerTCP server;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private boolean running = true;

    public ClientHandler(Socket socket, GameServerTCP server) {
        this.socket = socket;
        this.server = server;
        try {
            this.dataOut = new DataOutputStream(socket.getOutputStream());
            this.dataIn = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (running) {
                int length = dataIn.readInt();
                if (length > 0 && length < 10000) {
                    byte[] data = new byte[length];
                    dataIn.readFully(data);

                    InetAddress address = socket.getInetAddress();
                    int port = socket.getPort();
                    server.parsePacket(data, address, port);
                }
            }
        } catch (EOFException e) {
            System.out.println("Jogador desconectou (EOF).");
        } catch (IOException e) {
            System.err.println("Erro de leitura TCP no ClientHandler: " + e.getMessage());
        } finally {
            closeSocket();
        }
    }

    public synchronized void sendData(byte[] data) {
        try {
            if (socket != null && !socket.isClosed() && dataOut != null) {
                dataOut.writeInt(data.length);
                dataOut.write(data);
                dataOut.flush();
            }
        } catch (IOException e) {
            System.err.println("Erro ao enviar dados TCP no ClientHandler: " + e.getMessage());
            closeSocket();
        }
    }

    private void closeSocket() {
        this.running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
