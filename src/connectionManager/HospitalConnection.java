package connectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fileManager.FileManager;
import fileManager.User;
import guiHospital.GuiHospital;

public class HospitalConnection implements Runnable {

    Socket socket;
    InputStream inputStream;
    OutputStream outputStream; 
	PrintWriter pw;
	BufferedReader bf;
	Thread t;
	Boolean requestedMonitoring;
	
	User currentUser = new User (" ", " ", 0, 0, ' ', " ");
	String currentUserName;
	String currentPassword;
	
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
        String request = null;
        
        try {
			request = bf.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        switch (request) {
        	case "USER REQUESTING LOGIN": 
        		System.out.println("login");
        		answerLogin();
        		boolean connected = true;
        		String request2 = null;
        		while (connected) {    			
        			try {
						request2=bf.readLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        			System.out.println(request2);
        			switch (request2) {
        				case "USER REQUESTING MONITORING":
        					answerMonitoring();
        					break;
        				case "USER REQUESTING NEW REPORT":
        					answerNewReport();
        					break;
        				case "USER REQUESTING ASSISTANCE":
        					answerAlert();
        					break;
        				case "FINISHED MONITORING":
        					answerFinishSession();
        					connected = false;
        					break;        				
        			}
        			}
        	
        	case "USER REQUESTING NEW PROFILE": 
        		answerNewProfile();
        		boolean connected2 = true;
        		pw.println("Please finish creating the profile by filling your personal data.");
        		String request3 = null;
        		try {
        			request3 = bf.readLine();
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		if (request3.equals("USER REQUESTING NEW USER PROFILE")) {
        			answerProfileData();
        			answerFinishSession();
        			    		}
        		else pw.println("Please fill your personal data to be able to use our services.");
        		
        	default: 
        		pw.println("Please login or create a new profile to be able to use our services.");
        		break;
        
        	}
        
    }

    private static void releaseResources(InputStream is, OutputStream os, PrintWriter pw, BufferedReader br, Socket socket) {

        try {
            is.close();
        } catch (IOException ex) {
            Logger.getLogger(HospitalConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(HospitalConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            pw.close();
        } catch (Exception ex) {
            Logger.getLogger(HospitalConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            br.close();
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
    		boolean confirmed=false;
    		while (itu.hasNext() & itp.hasNext()) {
    			if (userName.equals(itu.next()) && password.equals(itp.next())) {
    				confirmed=true;
    				break;
    			}
    		}
    		if(confirmed) {
    			pw.println("ACCEPTED");
    			currentUserName = userName;
    			currentPassword = password;
    			User user = fileManager.FileManager.getUserConfig(currentUserName);
    			pw.println(user.getName());
    			pw.println(user.getSurname());
    			pw.println(user.getWeight());
    			pw.println(user.getAge());
    			pw.println(user.getGender());
    			
    		}else {
    			pw.println("REJECTED");
    		}
    	} 
    }
    
    public void answerNewProfile () {
    	String userName = null;
    	String password = null;
    	try {
			userName = bf.readLine();
			password = bf.readLine();
			System.out.println(userName+"   "+password);
			if (isValidInput(userName) && isValidInput(password)) {
				List<String> users = FileManager.getUserAndPasswords()[0];
				boolean profileExists=false;
				for (Iterator iterator = users.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					if(string.equals(userName)) {
						profileExists=true;
						break;
					}
				}
				if(profileExists) {
					pw.println("DENIED");
				}else {
					fileManager.FileManager.setUserAndPassword(userName, password);
		    		currentUserName = userName;
		    		currentPassword = password;
		    		/*I really dunno if the application was built to create the profile and login at the same time...
		    		 * Gotta check if this works with the client, I'll have a thorough look tomorrow CONNECTIONMANAGER
		    		 * epilepsy patient*/
		    		pw.println("CONFIRM");
				}
	    	}else {
	    		pw.println("DENIED");
	    	}
		} catch (IOException e) {
			e.printStackTrace();
			pw.println("DENIED");
		}
    }
    
    //This can only be requested if login has been done before.
    public void answerProfileData () {
    	String name = null;
    	String surname = null;
    	String weightS = null;
    	String ageS = null;
    	String genderS = null;
    	int weight = 0;
    	int age = 0;
    	char gender = ' ';
    	boolean rejected=false;
    	
    	try {
			name = bf.readLine();
			surname = bf.readLine();
			weightS = bf.readLine();
			ageS = bf.readLine();
			genderS = bf.readLine();
			
			if (isValidInput(name)) {
				currentUser.setName(name);
			}else {
				rejected=true;
			}
			if (isValidInput(surname)) {
				currentUser.setSurname(surname);
			}else {
				rejected=true;
			}
			if (isValidInput(weightS)) {
				weight = Integer.parseInt(weightS);
				currentUser.setWeight(weight);
			}else {
				rejected=true;
			}
			if (isValidInput(ageS)) {
				age = Integer.parseInt(ageS);
				currentUser.setAge(age);
			}else {
				rejected=true;
			}
			if (isValidInput(genderS)) {
				gender = genderS.toCharArray()[0];
				currentUser.setGender(gender);
			}else {
				rejected=true;
			}
			if(!rejected) {
		    	User user = new User (name, surname, weight, age, gender, currentUserName);
		    	fileManager.FileManager.setUserConfig(user);
		    	FileManager.setUserAndPassword(currentUserName, currentPassword);
		    	pw.println("ACCEPTED");
		    	System.out.println("ACCEPTED");
		    	System.out.println(user);
			}else {
				pw.println("REJECTED");
				System.out.println("REJECTED");
			}

		} catch (IOException e) {
			e.printStackTrace();
			pw.println("REJECTED");
		}
    	

    }
    
    public void answerMonitoring () {
    	List <Double> ecg= new ArrayList<Double>();
    	List <Double> eeg=new ArrayList<Double>();
    	List <Double> timeEEG=new ArrayList<Double>();
    	List <Double> timeECG=new ArrayList<Double>();
    	boolean phase1=false;
    	int counter=0;
    	while (true) {
    		try {
    			String read=bf.readLine();
    			if(read.contains("ECG")) {
    				phase1=true;
    				counter=0;
    			}
    			if(read.contains("EEG")) {
    				phase1=false;
    				counter=0;
    			}
    			if(read.equals("FINISHED MONITORING")) {
    				break;
    			}
    			try {
    				Double.parseDouble(read);
    				if(phase1) {
        				if(counter%2==0) {
        					timeECG.add(Double.parseDouble(read));
        					counter++;
        				}else {
        					ecg.add(Double.parseDouble(read));
        					counter++;
        				}
        			}else {
        				if(counter%2==0) {
        					timeEEG.add(Double.parseDouble(read));
        					counter++;
        				}else {
        					eeg.add(Double.parseDouble(read));
        					counter++;
        				}
        			}
    			}catch(Exception e) {
    				continue;
    			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public void answerNewReport () {
    	
    }
    
    public void answerAlert () {
    	GuiHospital.updateClients(this.currentUserName);
    }
    
    public void answerFinishSession () {
    	releaseResources (inputStream, outputStream, pw, bf, socket);
    	System.out.println ("Session finished.");
    	boolean connection = false;
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
