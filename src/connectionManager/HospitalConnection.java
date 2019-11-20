package connectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
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
    	String userName = null;
    	String password = null;
    	try {
			userName = bf.readLine();
			password = bf.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	if (isValidInput(userName)) {
    		List users = fileManager.FileManager.getUserAndPasswords()[0];
    		List passwords = fileManager.FileManager.getUserAndPasswords()[1];
    		
    		Iterator itu = users.iterator();
    		Iterator itp = passwords.iterator();
    		
    		while (itu.hasNext() & itp.hasNext()) {
    			if (userName == itu.next() & password == itp.next()) {
    				pw.write(userName);
    				pw.write(password);
    			}
    		}
    		
    	} 
    }
    
    public void answerNewProfile () {
    	String userName = null;
    	String password = null;
    	try {
			userName = bf.readLine();
			password = bf.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	if (isValidInput(userName)) {
    		fileManager.FileManager.setUserAndPassword(userName, password);
    	}
    }

    private boolean isValidInput(String input) {
    	char[] check=input.toCharArray();
    	char[] whitelist= {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',' ','0','1','2','3','4','5','6','7','8','9',};
    	boolean valid=false;
    	int counter=0;
    	for (int i = 0; i < check.length; i++) {
			char temp=check[i];
			boolean found=false;
			for (int j = 0; j < whitelist.length; j++) {
				if(temp==whitelist[j]) {
					found=true;
					break;
				}
			}
			if(!found) {
				break;
			}else {
				counter++;
			}
		}
    	if(counter==check.length) {
    		valid=true;
    	}
    	return valid;
    }
}
