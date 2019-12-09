package connectionManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SortingFocusTraversalPolicy;
import javax.xml.bind.DatatypeConverter;

import fileManager.FileManager;
import fileManager.User;
import guiHospital.GuiHospital;
import security.Security;

public class HospitalConnection implements Runnable {

	public static Socket socket;
	public static InputStream inputStream;
	public static OutputStream outputStream;
	public static PrintWriter pw;
	public static BufferedReader bf;
	Thread t;
	Boolean requestedMonitoring;

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private PrivateKey userPC;

	
	User currentUser = new User(" ", " ", 0, 0, ' ', " ");
	String currentUserName;
	String currentPassword;
	static ArrayList<User> currentUsers = new ArrayList <User>();

	public HospitalConnection(Socket socket) throws Exception {
		try {
			this.socket = socket;
			pw = new PrintWriter(socket.getOutputStream(), true);
			bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			requestedMonitoring = false;

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
		Map<String, Object> keys = Security.createKeys();
		privateKey = (PrivateKey) keys.get("private");
		publicKey = (PublicKey) keys.get("public");
		try {
			this.handshake();
			request = bf.readLine();
			request = Security.decryptMessage(request, publicKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(request);
		switch (request) {
		case "USER REQUESTING LOGIN":
			answerLogin();
			boolean connected = true;
			String request2 = "";
			while (connected) {
				try {
					request2 = bf.readLine();
					request2 = Security.decryptMessage(request2, publicKey);
				} catch (IOException e) {
					this.answerFinishSession();
					System.out.println("Client disconnected");
				}
				System.out.println("While connected client asks for:"+ request2);
				switch (request2) {
				case "USER REQUESTING MONITORING":
					answerMonitoring();
					break;
				case "USER REQUESTING NEW REPORT":
					System.out.println("PASO PASO PASO");
					answerNewReport();
					break;
				case "USER REQUESTING ASSISTANCE":
					answerAlert();
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
			String request3 = "";
			String reply="";
			try {
				request3 = bf.readLine();
				request3=Security.decryptMessage(request3, publicKey);
			} catch (Exception e) {
				System.out.println("There was a problem creating the profile.");
				break;
			}
			if (request3.equals("USER REQUESTING NEW USER PROFILE")) {
				answerProfileData();
				answerFinishSession();
			} else
				reply= Security.encryptMessage("There was a problem creating the profile.", userPC);
				pw.println(reply);

		default:
			reply="The server cannot answer the request.";
			reply= Security.encryptMessage(reply, userPC);
			pw.println(reply);
			break;

		}

	}

	public static void releaseResources(InputStream is, OutputStream os, PrintWriter pw, BufferedReader br,
			Socket socket) {
		System.out.println("BEGINNING CLIENT SERVER SHUTDOWN");
		System.out.println("...");
		try {
			System.out.println("Attempting to close input stream...");
			is.close();
		} catch (Exception ex) {
			System.out.println("Could not close the input stream.");
		}

		try {
			System.out.println("Attempting to close output stream...");
			os.close();
		} catch (Exception ex) {
			System.out.println("Could not close output Stream.");
		}

		try {
			System.out.println("Attempting to close printwriter...");
			pw.close();
		} catch (Exception ex) {
			System.out.println("Could not close the printwriter.");
		}

		try {
			System.out.println("Attempting to close buffered reader...");
			br.close();
		} catch (Exception ex) {
			System.out.println("Could not close the buffered reader.");
		}

		try {
			System.out.println("Attempting to close Client Socket...");
			socket.close();
		} catch (Exception ex) {
			System.out.println("Could not close the client socket.");
		}
		System.out.println("...");
		System.out.println("CLIENT SERVER SHUTDOWN COMPLETED");
	}

	public void answerLogin() {
		String userName = null;
		String password = null;
		try {
			userName = bf.readLine();
			userName = Security.decryptMessage(userName, publicKey);
			password = bf.readLine();
			password = Security.decryptMessage(password, publicKey);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (isValidInput(userName)) {
			List users = fileManager.FileManager.getUserAndPasswords()[0];
			List passwords = fileManager.FileManager.getUserAndPasswords()[1];

			Iterator itu = users.iterator();
			Iterator itp = passwords.iterator();
			boolean confirmed = false;
			while (itu.hasNext() & itp.hasNext()) {
				if (userName.equals(itu.next()) && password.equals(itp.next())) {
					confirmed = true;
					break;
				}
			}
			if (confirmed) {
				GuiHospital.updateClients(userName, "NOMINAL");
				String response = Security.encryptMessage("ACCEPTED", userPC);
				pw.println(response);
				currentUserName = userName;
				currentPassword = password;
				User user = fileManager.FileManager.getUserConfig(currentUserName);
				currentUsers.add(user);
				response = Security.encryptMessage(user.getName(), userPC);
				pw.println(response);
				response = Security.encryptMessage(user.getSurname(), userPC);
				pw.println(response);
				response = Security.encryptMessage("" + user.getWeight(), userPC);
				pw.println(response);
				response = Security.encryptMessage("" + user.getAge(), userPC);
				pw.println(response);
				response = Security.encryptMessage("" + user.getGender(), userPC);
				pw.println(response);

			} else {
				String response = Security.encryptMessage("REJECTED", userPC);
				pw.println(response);
			}
		}
	}

	public void answerNewProfile() {
		String userName = null;
		String password = null;
		try {
			userName = bf.readLine();
			userName = Security.decryptMessage(userName, publicKey);
			password = bf.readLine();
			password = Security.decryptMessage(password, publicKey);
			System.out.println(userName + "   " + password);
			if (isValidInput(userName) && isValidInput(password)) {
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
			System.out.println("Could not recieve a proper response.");
			String response = Security.encryptMessage("DENIED", userPC);
			pw.println(response);
		}
	}

	// This can only be requested if login has been done before.
	public void answerProfileData() {
		String name = null;
		String surname = null;
		String weightS = null;
		String ageS = null;
		String genderS = null;
		int weight = 0;
		int age = 0;
		char gender = ' ';
		boolean rejected = false;

		try {
			name = bf.readLine();
			name = Security.decryptMessage(name, publicKey);
			surname = bf.readLine();
			surname = Security.decryptMessage(surname, publicKey);
			weightS = bf.readLine();
			weightS = Security.decryptMessage(weightS, publicKey);
			ageS = bf.readLine();
			ageS = Security.decryptMessage(ageS, publicKey);
			genderS = bf.readLine();
			genderS = Security.decryptMessage(genderS, publicKey);

			System.out.println("name:"+name+" surname:"+surname+" weight:"+weightS+" age:"+ageS+" gender:"+genderS);
			if (isValidInput(name)) {
				currentUser.setName(name);
			} else {
				rejected = true;
			}
			if (isValidInput(surname)) {
				currentUser.setSurname(surname);
			} else {
				rejected = true;
			}
			if (isValidInput(weightS)) {
				weight = Integer.parseInt(weightS);
				currentUser.setWeight(weight);
			} else {
				rejected = true;
			}
			if (isValidInput(ageS)) {
				age = Integer.parseInt(ageS);
				currentUser.setAge(age);
			} else {
				rejected = true;
			}
			if (isValidInput(genderS)) {
				gender = genderS.toCharArray()[0];
				currentUser.setGender(gender);
			} else {
				rejected = true;
			}
			if (!rejected) {
				System.out.println("Valid profile data.");
				User user = new User(name, surname, weight, age, gender, currentUserName);
				fileManager.FileManager.setUserConfig(user);
				FileManager.setUserAndPassword(currentUserName, currentPassword);
				String response = Security.encryptMessage("ACCEPTED", userPC);
				pw.println(response);
				System.out.println("ACCEPTED");
				System.out.println(user);
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

	public void answerMonitoring() {
		List<Double> data = new ArrayList<Double>();
		List<Double> time = new ArrayList<Double>();
		boolean phase1 = false;
		int counter = 0;
		while (true) {
			try {
				String read = bf.readLine();
				read = Security.decryptMessage(read, publicKey);
				if (read.contains("ECG")) {
					phase1 = true;
					counter = 0;
				}
				if (read.contains("EEG")) {
					phase1 = false;
					counter = 0;
				}
				if (read.equals("FINISHED MONITORING")) {
					break;
				}
				try {
					Double.parseDouble(read);
					if (phase1) {
						if (counter % 2 == 0) {
							time.add(Double.parseDouble(read));
							counter++;
						} else {
							data.add(Double.parseDouble(read));
							counter++;
						}
					} else {
						if (counter % 2 == 0) {
							time.add(Double.parseDouble(read));
							counter++;
						} else {
							data.add(Double.parseDouble(read));
							counter++;
						}
					}
				} catch (Exception e) {
					continue;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void answerNewReport() {
		List<Double> ecg = new ArrayList<Double>();
		List<Double> eeg = new ArrayList<Double>();
		List<Double> timeEEG = new ArrayList<Double>();
		List<Double> timeECG = new ArrayList<Double>();
		String temp;
		String instruction = "";
		String comments = "";
		int counter = 0;
		GuiHospital.updateClients(currentUserName, "SYMPTOMS");
		while (true) {
			try {
				temp = bf.readLine();
				temp = Security.decryptMessage(temp, publicKey);
				try {
					double data = Double.parseDouble(temp);
					if (instruction.contains("ECG")) {
						if (counter % 2 == 0) {
							timeECG.add(data);
						} else {
							ecg.add(data);
						}
						counter++;
					}
					if (instruction.contains("EEG")) {
						if (counter % 2 == 0) {
							timeEEG.add(data);
						} else {
							eeg.add(data);
						}
						counter++;
					}

				} catch (Exception e) {
					System.out.println(temp);
					instruction = temp;
					counter = 0;
					if (instruction.equals("COMMENTS")) {
						comments = temp;
					}
					if (instruction.contains("DONE")) {
						break;
					}
				}
			} catch (IOException e) {
				System.out.println("Error reading report ");
				e.printStackTrace();
			}
		}
		FileManager.setreport(new List[] { timeECG, ecg }, new List[] { timeEEG, eeg }, comments, currentUserName);
	}

	public void answerAlert() {
		GuiHospital.updateClients(this.currentUserName, "CRITICAL");
	}

	public void answerFinishSession() {
		releaseResources(inputStream, outputStream, pw, bf, socket);
		System.out.println("Session finished.");
		GuiHospital.removeClients(currentUserName);
	}

	public static boolean isValidInput(String input) {
		char[] check = input.toCharArray();
		char[] whitelist = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
				's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
				'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
				'X', 'Y', 'Z' };
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

	public static ArrayList<User> getListUsers (){
		return currentUsers;
	}
	
	private void handshake() {
		String request = "";
		try {
			request = bf.readLine();
			
		    byte[] blob= DatatypeConverter.parseBase64Binary(request);
		    KeyFactory kf = KeyFactory.getInstance("RSA");
		    userPC=kf.generatePrivate(new PKCS8EncodedKeySpec(blob));
			
		    request = DatatypeConverter.printBase64Binary(privateKey.getEncoded());
			pw.println(request);
			
		} catch (Exception e) {
			System.out.println("FAILED HANDSHAKE");
			e.printStackTrace();
		}

	}
}
