package connectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HospitalConnection implements Runnable {

	int byteRead;
    Socket socket;

    public HospitalConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        boolean connected = true;

        try {
            //Read from the client
            InputStream inputStream = socket.getInputStream();

            while (connected == true) {
                byteRead = inputStream.read();
                //We read until is finished the connection or character 'x'
                if (byteRead == -1 || byteRead == 'x') {
                    System.out.println("Character reception finished");
                    releaseResources(inputStream, socket);
                    connected = false;
                    //System.exit(0);
                }
                char caracter = (char) byteRead;
                System.out.print(caracter);
                System.out.print(" ");
            }
        } catch (IOException ex) {
            Logger.getLogger(HospitalConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void releaseResources(InputStream inputStream, Socket socket) {

        try {
            inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(HospitalConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(HospitalConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
