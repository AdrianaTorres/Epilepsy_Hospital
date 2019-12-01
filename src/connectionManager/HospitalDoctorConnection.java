package connectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fileManager.FileManager;
import fileManager.User;
import guiHospital.GuiHospital;

public class HospitalDoctorConnection implements Runnable {
	 Socket socket;
	 InputStream inputStream;
	 OutputStream outputStream; 
	 PrintWriter pw;
	 BufferedReader bf;
	 
	 User currentUser = new User (" ", " ", 0, 0, ' ', " ");
	 String currentUserName;
	 String currentPassword;
	 
	 public HospitalDoctorConnection (Socket socket) throws Exception{
			try {
				this.socket = socket;
				pw = new PrintWriter(socket.getOutputStream(),true);
				bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (Exception e) {
				System.out.println("Could not connect to server!");
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
	 
	 private static void releaseResources(InputStream is, OutputStream os, PrintWriter pw, BufferedReader br, Socket socket) {

	        try {
	            is.close();
	        } catch (Exception ex) {
	            System.out.println("could not close input Stream!");
	        }
	        
	        try {
	            os.close();
	        } catch (Exception ex) {
	            System.out.println("could not close output Stream!");
	        }
	        
	        try {
	            pw.close();
	        } catch (Exception ex) {
	            System.out.println("could not close the printwriter!");
	        }

	        try {
	            br.close();
	        } catch (Exception ex) {
	            System.out.println("could not close the buffered reader!");
	        }
	        
	        try {
	            socket.close();
	        } catch (Exception ex) {
	            System.out.println("The socket just fuckin died, must have been the client");
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
	    	
	    	if (HospitalConnection.isValidInput(userName)) {
	    		List users = fileManager.FileManager.getUserAndPasswords()[0];
	    		List passwords = fileManager.FileManager.getUserAndPasswords()[1];
	    		
	    		Iterator itu = users.iterator();
	    		Iterator itp = passwords.iterator();
	    		boolean confirmed=false;
	    		while (itu.hasNext() & itp.hasNext()) {
	    			if (userName.equals(itu.next()) && password.equals(itp.next())) {
	    				confirmed=true;
	    				break;
	    			}
	    		}
	    		if(confirmed) {
	    			GuiHospital.updateClients(userName,"NOMINAL");
	    			pw.println("ACCEPTED");
	    			currentUserName = userName;
	    			currentPassword = password;
	    			currentUser = fileManager.FileManager.getDoctorConfig(currentUserName);	    			
	    		}else {
	    			pw.println("REJECTED");
	    		}
	    	} 
	    }

	 public void answerSeeReport () {
	    	
	    }
	 
	 public void answerFinishSession () {
	    	releaseResources (inputStream, outputStream, pw, bf, socket);
	    	System.out.println ("Session finished.");
	    	GuiHospital.removeClients(currentUserName);
	    }
}
