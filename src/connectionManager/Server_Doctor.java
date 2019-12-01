package connectionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server_Doctor implements Runnable {
	
    ServerSocket serverSocket_Doctor = null;
	
    public Server_Doctor () {
		this.serverSocket_Doctor = serverSocket_Doctor;
	}
	
	public void run ()  {
   
        try {
        	serverSocket_Doctor = new ServerSocket(9000);
            while (true) {
                //This executes when we have a client
                Socket socket = serverSocket_Doctor.accept();
                try {
                	new Thread(new HospitalDoctorConnection(socket)).start();
                }catch(Exception e) {
                	System.out.println("Could not connect patient");
                }
                
            }
        } catch (IOException e) {
			e.printStackTrace();
		} finally {
            releaseResourcesServer(serverSocket_Doctor);
        }
    }

    private static void releaseResourcesServer(ServerSocket serverSocket_Doctor) {
        try {
            serverSocket_Doctor.close();
        } catch (IOException ex) {
            Logger.getLogger(HospitalConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
