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
	public static Socket socket;
	public static InputStream inputStream;
	public static OutputStream outputStream;
	public static PrintWriter pw;
	public static BufferedReader bf;

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private PrivateKey userPC;

	Doctor currentDoctor = new Doctor(" ", " ", " ");
	String currentUserName;
	String currentPassword;

	public HospitalDoctorConnection(Socket socket) throws Exception {
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
			System.out.println("DOCTOR REUQESTS THE FOLLOWING:  " + request);
		} catch (IOException e) {
			this.answerFinishSession();
			System.out.println("Doctor disconnected");
		}

		switch (request) {
		case "USER REQUESTING LOGIN":
			answerLogin();
			boolean connected = true;
			String request2 = "";
			while (connected) {
				try {
					request2 = bf.readLine();
				} catch (IOException e) {
					this.answerFinishSession();
					System.out.println("Doctor disconnected");
					releaseResources(inputStream, outputStream, pw, bf, socket);
					break;
				}
				System.out.println(request2);
				switch (request2) {
				case "USER REQUESTING REPORTS LIST":
					answerReportsList();
					break;
				case "USER REQUESTING REPORT":
					try {
						System.out.println("imma send you something nice!");
						answerSeeReport();
					} catch (Exception e) {
						System.out.println("Unable to show report.");
					}
					break;
				case "FINISHED MONITORING":
					answerFinishSession();
					connected = false;
					break;
				case "USER REQUESTING PATIENT PROFILE":
					answerPatientProfile();
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

	private void answerPatientProfile() {
		try {
			String request = bf.readLine();
			char[]converter=request.toCharArray();
			String username="";
			for (int i = 0; i < converter.length; i++) {
				if(converter[i]=='_') {
					break;
				}else {
					username= username+converter[i];
				}
			}
			System.out.println(username);
			User temp= FileManager.getUserConfig(username);
			pw.println(temp.getName());
			pw.println(temp.getSurname());
			pw.println(temp.getWeight());
			pw.println(temp.getAge());
			pw.println(temp.getGender());
		}catch(Exception e) {
			System.out.println("could not determine profile");
		}
	}

	public static void releaseResources(InputStream is, OutputStream os, PrintWriter pw, BufferedReader br,
			Socket socket) {
		System.out.println("BEGINNING SERVER2 SHUT DOWN");
		System.out.println("...");
		try {
			System.out.println("attempting to close input stream...");
			is.close();
		} catch (Exception ex) {
			System.out.println("could not close input Stream... Maybe it was never used?");
		}

		try {
			System.out.println("attempting to close output stream...");
			os.close();
		} catch (Exception ex) {
			System.out.println("could not close output Stream... Maybe it was never used?");
		}

		try {
			System.out.println("attempting to close printwriter...");
			pw.close();
		} catch (Exception ex) {
			System.out.println("could not close the printwriter... Maybe it was never used?");
		}

		try {
			System.out.println("attempting to close buffered reader...");
			br.close();
		} catch (Exception ex) {
			System.out.println("could not close the buffered reader... Maybe it was never used?");
		}

		try {
			System.out.println("attempting to close Doctor Socket...");
			socket.close();
		} catch (Exception ex) {
			System.out.println("The socket just commited Sepoku");
		}
		System.out.println("...\nSERVER2 SHUTDOWN COMPLETED");

	}

	public void answerLogin() {
		String userName = null;
		String password = null;
		try {
			userName = bf.readLine();
			password = bf.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (HospitalConnection.isValidInput(userName)) {
			List users = fileManager.FileManager.getDoctorUsernamesAndPasswords()[0];
			List passwords = fileManager.FileManager.getDoctorUsernamesAndPasswords()[1];

			Iterator itu = users.iterator();
			Iterator itp = passwords.iterator();
			boolean confirmed = false;
			while (itu.hasNext() & itp.hasNext()) {
				System.out.println(userName);
				String o = (String) itu.next();
				String z = (String) itp.next();
				System.out.println(o + "   " + z);
				if (userName.equals(o) && password.equals(z)) {
					confirmed = true;
					break;
				}
			}
			if (confirmed) {
				currentUserName = userName;
				currentPassword = password;
				pw.println("ACCEPTED");
				currentDoctor = fileManager.FileManager.getdoctorProfile(currentUserName);
				pw.println(currentDoctor.getName());
				pw.println(currentDoctor.getSurname());
			} else {
				pw.println("REJECTED");
			}
		}
	}

	public void answerNewProfile() {
		String userName = null;
		String password = null;
		try {
			userName = bf.readLine();
			password = bf.readLine();
			System.out.println(userName + "   " + password);
			if (HospitalConnection.isValidInput(userName) && HospitalConnection.isValidInput(password)) {
				List<String> users = FileManager.getDoctorUsernamesAndPasswords()[0];
				boolean profileExists = false;
				for (Iterator iterator = users.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					if (string.equals(userName)) {
						profileExists = true;
						break;
					}
				}
				if (profileExists) {
					String response = "DENIED";
					pw.println(response);
				} else {
					currentUserName = userName;
					currentPassword = password;
					String response = "CONFIRM";
					pw.println(response);
				}
			} else {
				String response = "DENIED";
				pw.println(response);
			}
		} catch (Exception e) {
			System.out.println("could not recieve a proper response, we still flying though");
			String response = "DENIED";
			pw.println(response);
		}
	}

	public void answerProfileData() {
		String name = null;
		String surname = null;
		boolean rejected = false;

		try {
			name = bf.readLine();
			surname = bf.readLine();
			/* comprobar que el username no existe tienes la funcion ya hecha. */

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
				Doctor doctor = new Doctor(name, surname, currentUserName);
				fileManager.FileManager.setDoctorProfile(doctor.getUserName(), doctor.getName(), doctor.getSurname());
				FileManager.setDoctorUsernameAndPassword(currentUserName, currentPassword);
				String response = "ACCEPTED";
				pw.println(response);
				System.out.println("ACCEPTED");
				System.out.println(doctor);
			} else {
				String response = "REJECTED";
				pw.println(response);
				System.out.println("REJECTED");
			}

		} catch (IOException e) {
			e.printStackTrace();
			String response = "REJECTED";
			pw.println(response);
		}

	}

	public void answerReportsList() {
		ArrayList<String> reports = new ArrayList<String>();
		System.out.println("knock knock");
		File[] files = new File(System.getProperty("user.dir") + "\\reports").listFiles();
		System.out.println(files.toString());
		for (File file : files) {
			if (file.isFile()) {
				reports.add(file.getName());
				System.out.println(file.getName());
				pw.println(file.getName());
			}
		}
		pw.println("DONE");
	}

	public void answerSeeReport() throws Exception {

		String reportName = null;

		try {
			reportName = bf.readLine();
			System.out.println(reportName);
			Report rp=FileManager.getReport(reportName);
			List<Double> time = rp.getEcgData()[0];
			List<Double> data = rp.getEcgData()[1];
			System.out.println("sending report now!");
			String petition;
			petition = "SENDING ECG";
			pw.println(petition);
			Iterator iterator_1 = time.iterator();
			for (Iterator iterator = data.iterator(); iterator.hasNext();) {
				pw.println(iterator_1.next());
				pw.println(iterator.next());
			}
			petition = "SENDING EEG";
			pw.println(petition);
			time = rp.getEegData()[0];
			data = rp.getEegData()[1];
			iterator_1 = time.iterator();
			for (Iterator iterator = data.iterator(); iterator.hasNext();) {
				pw.println(iterator_1.next());
				pw.println(iterator.next());
			}
			petition = "SENDING COMMENTS";
			pw.println(petition);
			pw.println(rp.getComments());
			petition = "DONE";
			pw.println(petition);
		} catch (IOException e) {
			System.out.println("could not send report from server to doctor...");
			e.printStackTrace();
		}
	}

	public void answerFinishSession() {
		releaseResources(inputStream, outputStream, pw, bf, socket);
		System.out.println("Session finished.");
		GuiHospital.removeClients(currentUserName); //
	}
}
