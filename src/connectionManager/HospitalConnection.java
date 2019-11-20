package connectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HospitalConnection implements Runnable {

    Socket socket;
	PrintWriter pw;
	BufferedReader bf;
	Thread t;
	Boolean requestedMonitoring;
	
	public HospitalConnection (Socket socket) throws Exception{
		try {
			this.socket = socket;
			pw = new PrintWriter(socket.getOutputStream(),true);
			bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			requestedMonitoring=false;
		} catch (Exception e) {
			System.out.println("could not connect to server!");
			socket = null;
			pw = null;
			bf = null;
			e.printStackTrace();
			throw new Exception();
		} 
	}

    @Override
    public void run() {
        

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
   
    public void answerLogin () {
    	String userName;
    	String password;
    	try {
			userName = bf.readLine();
			password = bf.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	//confirmar user y contraseña.
    }

}
