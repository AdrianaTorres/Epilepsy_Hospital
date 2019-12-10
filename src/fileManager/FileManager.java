package fileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import security.FileEncryptor;

public class FileManager {
	private static String userpasswdPath = System.getProperty("user.dir") + "\\whiteList.txt";
	private static String doctorPath = System.getProperty("user.dir") + "\\DoctorWhiteList.txt";
	private static String adminPath = System.getProperty("user.dir") + "\\rootMeUp.txt";
	private static String doctorData = System.getProperty("user.dir") + "\\Doctors.txt";
	private static String usersData = System.getProperty("user.dir") + "\\clients.txt";
	private static String reportDir = System.getProperty("user.dir") + "\\reports";

	private static BufferedReader bf;
	private static PrintWriter pw;

	FileManager() {
		if (isConfigured()) {
			System.out.println("Succesfully loaded/ created config files");
		} else {
			System.out.println("Fatal error. Could not open necessary config files");
		}
	}

	private static boolean isConfigured() {
		File conf = new File(userpasswdPath);
		File report = new File(reportDir);
		File usdata = new File(usersData);
		File docdata = new File(doctorPath);
		File docprofile = new File(doctorData);
		if (!conf.isFile() || !report.isDirectory() || !usdata.isFile() || !docdata.isFile() || !docprofile.isFile()) {
			try {
				conf.createNewFile();
				report.mkdir();
				usdata.createNewFile();
				System.out.println(conf.getAbsolutePath());
				docdata.createNewFile();
				docprofile.createNewFile();
				return true;
			} catch (IOException e) {
				System.out.println("Not possible to create config File or folder...");
				return false;
			}
		} else {
			return true;
		}
	}

	public static List[] getUserAndPasswords() {
		try {
			isConfigured();
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(userpasswdPath)));
			List<String> userNames = new ArrayList<String>();
			List<String> passwords = new ArrayList<String>();
			java.lang.String read;
			int counter = 0;
			while ((read = bf.readLine()) != null) {
				if (read.equals("")) {
					continue;
				}
				if (counter % 2 == 0) {
					userNames.add((String) read);
				} else {
					read = FileEncryptor.decryptString(read);
					passwords.add((String) read);
				}
				counter++;
			}
			return new List[] { userNames, passwords };
		} catch (Exception e) {
			System.out.println("Could not read users or passwords!");
			e.printStackTrace();
			return null;
		}
	}

	public static void setUserAndPassword(String username, String password) {
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(userpasswdPath)));
			String temp = "";
			int counter = 0;
			password = FileEncryptor.encryptString(password);
			ArrayList<String> passw = new ArrayList<String>();
			ArrayList<String> users = new ArrayList<String>();
			while (temp != null) {
				temp = bf.readLine();
				if (temp == null) {
					break;
				}
				System.out.println(temp);
				if (counter % 2 == 1) {
					passw.add(temp);
				} else {
					users.add(temp);
				}
				counter++;
			}
			pw = new PrintWriter(new FileOutputStream(userpasswdPath), true);
			pw.println(username);
			pw.println(password);
			Iterator iterator1 = passw.iterator();
			for (Iterator iterator = users.iterator(); iterator.hasNext();) {
				String us = (String) iterator.next();
				String pa = (String) iterator1.next();
				pw.println(us);
				pw.println(pa);
			}
			bf.close();
			pw.close();
		} catch (Exception e) {
			System.out.println("Could not add user or password");
			e.printStackTrace();
		}
	}

	public static void setreport(List[] ecg, List[] eeg, String comments, String userName) {
		int counter = 0;
		File manager = new File(
				System.getProperty("user.dir") + "\\reports\\" + userName + "_report_" + counter + ".txt");
		while (manager.isFile()) {
			counter++;
			manager = new File(
					System.getProperty("user.dir") + "\\reports\\" + userName + "_report_" + counter + ".txt");
		}
		PrintWriter data = null;
		try {
			System.out.println(manager.getAbsolutePath());
			manager.createNewFile();
			data = new PrintWriter(manager);
			Iterator iterator1 = eeg[1].iterator();
			Iterator iterator3 = ecg[1].iterator();

			data.println("EEG DATA");
			for (Iterator iterator = eeg[0].iterator(); iterator.hasNext();) {
				data.print(iterator.next());
				data.print(" ");
				data.print(iterator1.next());
				data.println();
			}
			data.println("ECG DATA");
			for (Iterator iterator2 = ecg[0].iterator(); iterator2.hasNext();) {
				data.print(iterator2.next());
				data.print(" ");
				data.print(iterator3.next());
				data.println();
			}
			data.println("COMMENTS");
			data.println(comments);
		} catch (Exception e) {
			System.out.println("Could not create report file");
			e.printStackTrace();
		} finally {
			try {
				data.close();
			} catch (Exception e) {
				System.out.println("Could not close report file");
				e.printStackTrace();
			}

		}
	}

	public static Report getReport(String report) {
		List<Double> time1 = new ArrayList<Double>();
		List<Double> time2 = new ArrayList<Double>();
		List<Double> ecg = new ArrayList<Double>();
		List<Double> eeg = new ArrayList<Double>();
		String comment = "";

		File manager = new File(reportDir+"//"+report);
		BufferedReader data = null;
		try {
			System.out.println(manager.getAbsolutePath());
			data = new BufferedReader(new InputStreamReader(new FileInputStream(manager)));
			boolean phaseOneComplete = false;
			boolean comments = false;
			String inputRead;
			while ((inputRead = data.readLine()) != null) {
				try {
					parser(inputRead);
					if (!phaseOneComplete) {
						time1.add(parser(inputRead)[0]);
						eeg.add(parser(inputRead)[1]);
					}
					if (phaseOneComplete && !comments) {
						time2.add(parser(inputRead)[0]);
						ecg.add(parser(inputRead)[1]);
					}
				} catch (Exception e) {
					if (inputRead.contains("ECG")) {
						phaseOneComplete = true;
					}
					if (inputRead.contains("COMMENTS")) {
						comments = true;
					}
					if (comments && !inputRead.contains("COMMENTS")) {
						comment = comment + "\n" + inputRead;
					}
				}
			}
			try {
				data.close();
			} catch (Exception e) {
				System.out.println("Could not close reader");
				e.printStackTrace();
			}
			return new Report((new List[] { time2, ecg }), (new List[] { time1, eeg }), comment);
		} catch (Exception e) {
			System.out.println("Could not read report");
			e.printStackTrace();
			return null;
		}
	}

	public static User getUserConfig(String userName) {
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(usersData)));
			User us = null;
			String read;
			while (true) {
				read = bf.readLine();
				if (read == null) {
					break;
				} else {
					if (read.equals(userName)) {
						String name = bf.readLine();
						String surname = bf.readLine();
						int weight = Integer.parseInt(bf.readLine());
						int age = Integer.parseInt(bf.readLine());
						char gender = bf.readLine().toCharArray()[0];
						us = new User(name, surname, weight, age, gender, userName);
						break;
					} else {
						continue;
					}
				}
			}
			return us;
		} catch (Exception e) {
			System.out.println("Could not read user configuration!");
			e.printStackTrace();
			return null;
		}
	}

	public static void setUserConfig(User user) {
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(usersData)));
			String temp = "";
			ArrayList<User> users = new ArrayList<User>();
			users.add(user);
			while (temp != null) {
				temp = bf.readLine();
				if (temp == null) {
					break;
				} else if (!temp.equals("") || !temp.equals("\n")) {
					String username = bf.readLine();
					String name = bf.readLine();
					String surname = bf.readLine();
					int weight = Integer.parseInt(bf.readLine());
					int age = Integer.parseInt(bf.readLine());
					char gender = bf.readLine().toCharArray()[0];
					User t = new User(name, surname, weight, age, gender, username);
					users.add(t);
				}
			}
			pw = new PrintWriter(new FileOutputStream(usersData), true);
			for (Iterator iterator = users.iterator(); iterator.hasNext();) {
				User us = (User) iterator.next();
				pw.println();
				pw.println(us.getUserName());
				pw.println(us.getName());
				pw.println(us.getSurname());
				pw.println(us.getWeight());
				pw.println(us.getAge());
				pw.println(us.getGender());
			}
			bf.close();
			pw.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not add the user!");
		}
	}

	private static double[] parser(String data) {
		char[] temp = data.toCharArray();
		double time = 0;
		double input = 0;
		String helper = "";
		for (int i = 0; i < temp.length; i++) {
			if (temp[i] != ' ') {
				helper = helper + temp[i];
			} else {
				time = Double.parseDouble(helper);
				helper = "";
			}
			if (i == temp.length - 1) {
				input = Double.parseDouble(helper);
			}
		}
		return new double[] { time, input };
	}

	public static void close() {
		try {
			pw.close();
		} catch (Exception e) {
			System.out.println("Could not close printwriter");
		}
		try {
			bf.close();
		} catch (Exception e) {
			System.out.println("Could not close buffered reader");
		}
	}

	public static void setDoctorUsernameAndPassword(String username, String password) {
		try {
			password = FileEncryptor.encryptString(password);
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(doctorPath)));
			pw = new PrintWriter(new FileOutputStream(doctorPath), true);
			ArrayList<String> content = new ArrayList<String>();
			String read = "";
			while (read != null) {
				read = bf.readLine();
				if (read == null) {
					break;
				}
				content.add(read);
			}
			content.add(username);
			content.add(password);
			for (Iterator iterator = content.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				pw.println(string);
			}
			pw.close();
			bf.close();
		} catch (Exception e) {
			System.out.println("Could not write the doctor down");
		}
	}

	public static void setDoctorProfile(String username, String name, String surname) {
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(doctorData)));
			pw = new PrintWriter(new FileOutputStream(doctorData), true);
			ArrayList<String> content = new ArrayList<String>();
			String read = "";
			while (read != null) {
				read = bf.readLine();
				if (read == null) {
					break;
				}
				content.add(read);
			}
			content.add(username);
			content.add(name);
			content.add(surname);
			content.add("");
			for (Iterator iterator = content.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				pw.println(string);
			}
			pw.close();
			bf.close();
		} catch (Exception e) {
			System.out.println("Could not write down the given doctor's profile");
		}
	}

	public static List[] getDoctorUsernamesAndPasswords() {
		ArrayList<String> usernames = new ArrayList<String>();
		ArrayList<String> passwords = new ArrayList<String>();
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(doctorPath)));
			String read = "";
			int counter = 0;
			while (read != null) {
				read = bf.readLine();
				if (read == null) {
					break;
				}
				if (counter % 2 == 0) {
					usernames.add(read);
				} else {
					read = FileEncryptor.decryptString(read);
					passwords.add(read);
				}
				counter++;
			}
			return new List[] { usernames, passwords };
		} catch (Exception e) {
			System.out.println("Could not read doctors!");
			return new List[] { usernames, passwords };
		}
	}

	public static Doctor getdoctorProfile(String username) {
		Doctor goAndFetch = new Doctor("", "", "");
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(doctorData)));
			String read = "";
			while (read != null) {
				read = bf.readLine();
				System.out.println(read);
				if (read == null) {
					break;
				} else {
					if (read.equals(username)) {
						goAndFetch.setUserName(read);
						read = bf.readLine();
						goAndFetch.setName(read);
						read = bf.readLine();
						goAndFetch.setSurname(read);
						break;
					}
				}
			}
			return goAndFetch;
		} catch (Exception e) {
			System.out.println("Could not fetch the doctor's profile");
			return goAndFetch;
		}
	}

	public static boolean rootMeUp(String temp, String text) {
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(adminPath)));
			String read = "";
			String root = "";
			String psw = "";
			int counter = 0;
			while (read != null) {
				read = bf.readLine();
				if (read == null) {
					break;
				}
				if (counter == 0) {
					root = read;
				} else {
					psw = read;
				}
				counter++;
			}
			psw = FileEncryptor.decryptString(psw);			
			if (psw.equals(temp) && root.equals(text)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			System.out.println("Incorrect credentials.");
			return false;
		}
	}

	public static boolean firstTimeLaunch() {
		File admin = new File(adminPath);
		if (admin.isFile()) {
			return false;
		} else {
			return true;
		}
	}
	public static void createAdminFile(String username, String pasword) {
		try {
			File admin = new File(adminPath);			
			pw = new PrintWriter(new FileOutputStream(adminPath),true);
			if(admin.isFile()) {
				pw.println(username);
				pasword=FileEncryptor.encryptString(pasword);
				pw.println(pasword);
				pw.flush();
				pw.close();
			}
		}catch(Exception e) {
			System.out.println("Could not create admin file! shutting down");
			System.exit(0);
		}
	}
}