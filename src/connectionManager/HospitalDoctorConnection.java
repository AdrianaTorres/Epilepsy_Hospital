package connectionManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fileManager.Doctor;
import fileManager.FileManager;
import fileManager.Report;
import fileManager.User;
import guiHospital.GuiHospital;
import security.Security;

public class HospitalDoctorConnection implements Runnable {
	 Socket socket;
	 InputStream inputStream;
	 OutputStream outputStream; 
	 PrintWriter pw;
	 BufferedReader bf;
	 
	 private PrivateKey privateKey;
	 private PublicKey publicKey;
	 private PrivateKey userPC;
	 
	 Doctor currentDoctor = new Doctor (" ", " ", " ");
	 String currentUserName;
	 String currentPassword;
	 
	 public HospitalDoctorConnection (Socket socket) throws Exception{
			try {
				this.socket = socket;
				pw = new PrintWriter(socket.getOutputStream(), true);
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
		 String request = "";
		 try {
				request = bf.readLine();
			} catch (IOException e) {
				this.answerFinishSession();
				System.out.println("Doctor disconnected");
			}
		 
		 switch (request) {
			case "USER REQUESTING LOGIN":
				System.out.println("login");
				answerLogin();
				boolean connected = true;
				String request2 = "";
				while (connected) {
					try {
						request2 = bf.readLine();
					} catch (IOException e) {
						this.answerFinishSession();
						System.out.println("Doctor disconnected");
					}
					System.out.println(request2);
					switch (request2) {
					case "USER REQUESTING REPORTS LIST":
						answerReportsList();
						break;
					case "USER REQUESTING REPORT":
						try {
							answerSeeReport();
						} catch (Exception e) {
							System.out.println("Unable to show report.");
						}
						break;
					case "FINISHED MONITORING":
						answerFinishSession();
						connected = false;
						break;
					default:
						connected = false;
						break;
					}
				}

			case "USER REQUESTING NEW PROFILE":
				answerNewProfile();
				pw.println("Please finish creating the profile by filling your personal data.");
				String request3 = null;
				try {
					request3 = bf.readLine();
				} catch (Exception e) {
					System.out.println("Doctor disconnected");
					break;
				}
				if (request3.equals("USER REQUESTING NEW USER PROFILE")) {
					answerProfileData();
					answerFinishSession();
				} else
					pw.println("Please fill your personal data to be able to use our services.");

			default:
				pw.println("Please finish creating a new profile to be able to use our services.");
				break;

			}
	 
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
	            System.out.println("could not close the socket!");
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
	    			currentUserName = userName;
	    			currentPassword = password;
	    			GuiHospital.updateClients(currentUserName,"NOMINAL");
	    			pw.println("ACCEPTED");
	    			currentDoctor = fileManager.FileManager.getDoctorConfig(currentUserName);	    			
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
				userName = Security.decryptMessage(userName, publicKey);
				password = bf.readLine();
				password = Security.decryptMessage(password, publicKey);
				System.out.println(userName + "   " + password);
				if (HospitalConnection.isValidInput(userName) && HospitalConnection.isValidInput(password)) {
					List<String> users = FileManager.getUserAndPasswords()[0];
					boolean profileExists = false;
					for (Iterator iterator = users.iterator(); iterator.hasNext();) {
						String string = (String) iterator.next();
						if (string.equals(userName)) {
							profileExists = true;
							break;
						}
					}
					if (profileExists) {
						String response = Security.encryptMessage("DENIED", userPC);
						pw.println(response);
					} else {
						currentUserName = userName;
						currentPassword = password;
						String response = Security.encryptMessage("CONFIRM", userPC);
						pw.println(response);
					}
				} else {
					String response = Security.encryptMessage("DENIED", userPC);
					pw.println(response);
				}
			} catch (Exception e) {
				System.out.println("could not recieve a proper response, we still flying though");
				String response = Security.encryptMessage("DENIED", userPC);
				pw.println(response);
			}
	 }
	 
	 public void answerProfileData() {
		 String name = null;
		 String surname = null;
		 boolean rejected = false;

		 try {
			 name = bf.readLine();
			 name = Security.decryptMessage(name, publicKey);
			 surname = bf.readLine();
			 surname = Security.decryptMessage(surname, publicKey);

			 if (HospitalConnection.isValidInput(name)) {
				currentDoctor.setName(name);
			} else {
				rejected = true;
				}
			 if (HospitalConnection.isValidInput(surname)) {
				currentDoctor.setSurname(surname);
			} else {
				rejected = true;
				}
			 
				if (!rejected) {
					Doctor doctor = new Doctor (name, surname, currentUserName);
					fileManager.FileManager.setDoctorConfig(doctor);
					FileManager.setUserAndPassword(currentUserName, currentPassword);
					String response = Security.encryptMessage("ACCEPTED", userPC);
					pw.println(response);
					System.out.println("ACCEPTED");
					System.out.println(doctor);
				} else {
					String response = Security.encryptMessage("REJECTED", userPC);
					pw.println(response);
					System.out.println("REJECTED");
				}

			} catch (IOException e) {
				e.printStackTrace();
				String response = Security.encryptMessage("REJECTED", userPC);
				pw.println(response);
			}
		 
	 }

	 public void answerReportsList () {
		 ArrayList<String> reports = new ArrayList<String>();
		 ArrayList<User> users = new ArrayList<User>();
		 users = HospitalConnection.getListUsers();
		 
		 Iterator itu = users.iterator();
		 while (itu.hasNext()) { 
			 User user = (User) itu.next();
			 
			 pw.println(user.getName());
			 pw.println(user.getSurname());
			 
			 File[] files = new File(System.getProperty("user.dir")+"\\reports").listFiles(); 

			 for (File file : files) {
				 if (file.isFile()) {
					 reports.add(file.getName());
					 pw.println(file.getName());
				 }
		 }
		 }
		
	 }
	 public void answerSeeReport () throws Exception {
		
 			String reportName = null; 
		 
	    	try {
				reportName = bf.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	File report = new File (System.getProperty("user.dir")+"\\reports" + reportName);
	    	try {
				FileReader fr = new FileReader (report);
				BufferedReader br = new BufferedReader(fr);
				
				while (br.readLine() != null) {
					pw.println(br.readLine());
				}
	    	
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
	 
	 public void answerFinishSession () {
	    	releaseResources (inputStream, outputStream, pw, bf, socket);
	    	System.out.println ("Session finished.");
	    	GuiHospital.removeClients(currentUserName); //
	    }
}
