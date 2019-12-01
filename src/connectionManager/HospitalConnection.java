package connectionManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fileManager.FileManager;
import fileManager.User;
import guiHospital.GuiHospital;
import security.Security;

public class HospitalConnection implements Runnable {

	Socket socket;
	InputStream inputStream;
	OutputStream outputStream;
	PrintWriter pw;
	BufferedReader bf;
	Thread t;
	Boolean requestedMonitoring;

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private PrivateKey userPC;

	User currentUser = new User(" ", " ", 0, 0, ' ', " ");
	String currentUserName;
	String currentPassword;

	public HospitalConnection(Socket socket) throws Exception {
		try {
			this.socket = socket;
			pw = new PrintWriter(socket.getOutputStream(), true);
			bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			requestedMonitoring = false;

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
					System.out.println("client disconnected");
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
				System.out.println("miss me with that disconnection shit");
				break;
			}
			if (request3.equals("USER REQUESTING NEW USER PROFILE")) {
				answerProfileData();
				answerFinishSession();
			} else
				pw.println("Please fill your personal data to be able to use our services.");

		default:
			pw.println("Please login or create a new profile to be able to use our services.");
			break;

		}

	}

	private static void releaseResources(InputStream is, OutputStream os, PrintWriter pw, BufferedReader br,
			Socket socket) {

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
			System.out.println("could not recieve a proper response, we still flying though");
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
					if (instruction.contains("COMMENTS")) {
						comments = temp;
					}
					if (instruction.contains("DONE")) {
						break;
					}
				}
			} catch (IOException e) {
				System.out.println("error reading report ");
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
		boolean connection = false;
		GuiHospital.removeClients(currentUserName);
	}

	private boolean isValidInput(String input) {
		char[] check = input.toCharArray();
		char[] whitelist = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
				's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
				'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
				'X', 'Y', 'Z' };
		boolean valid = false;
		int counter = 0;
		for (int i = 0; i < check.length; i++) {
			char temp = check[i];
			boolean found = false;
			for (int j = 0; j < whitelist.length; j++) {
				if (temp == whitelist[j]) {
					found = true;
					break;
				}
			}
			if (!found) {
				break;
			} else {
				counter++;
			}
		}
		if (counter == check.length) {
			valid = true;
		}
		return valid;
	}

	private void handshake() {
		String request = "";
		try {
			request = bf.readLine();
			
			ByteArrayInputStream bis = new ByteArrayInputStream(request.getBytes());
		    ObjectInputStream oInputStream = new ObjectInputStream(bis);
			userPC = (PrivateKey) oInputStream.readObject();
			oInputStream.close();
			
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutputStream os = new ObjectOutputStream(bos);
		    os.writeObject(privateKey);
		    request = bos.toString();
			System.out.println("this is my petition:"+request);
			pw.println(request);
			
		} catch (Exception e) {
			System.out.println("FAILED HANDSHAKE");
			e.printStackTrace();
		}

	}
}
