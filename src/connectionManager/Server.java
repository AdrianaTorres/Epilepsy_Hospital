package connectionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {
	
	public static ServerSocket serverSocket = null;
	
    public Server () {
		this.serverSocket = serverSocket;
	}
	
	public void run ()  {
   
        try {
        	serverSocket = new ServerSocket(9000);
            while (true) {
                //This executes when we have a client
                Socket socket = serverSocket.accept();
                try {
                	new Thread(new HospitalConnection(socket)).start();
                }catch(Exception e) {
                	System.out.println("Could not connect patient");
                }
                
            }
        } catch (IOException e) {
			e.printStackTrace();
		} finally {
            releaseResourcesServer(serverSocket);
        }
    }

    public static void releaseResourcesServer(ServerSocket serverSocket) {
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(HospitalConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
