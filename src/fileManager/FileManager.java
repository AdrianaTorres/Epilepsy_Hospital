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

public class FileManager {
	private static String userpasswdPath=System.getProperty("user.dir")+"\\whiteList.txt";
	private static String usersData=System.getProperty("user.dir")+"\\clients.txt";
	private static String reportDir=System.getProperty("user.dir")+"\\reports";
	
	private static BufferedReader bf;
	private static PrintWriter pw;
	
	FileManager(){
		if(isConfigured()) {
			System.out.println("succesfully loaded/ created config files");
		}else {
			System.out.println("fatal error could not open necessary config files");
		}
	}
	private static boolean isConfigured() {
		File conf= new File(userpasswdPath);
		File report= new File(reportDir);
		File usdata =new File(usersData);
		if(!conf.isFile()||!report.isDirectory()||!usdata.isFile()) {
			try {
				conf.createNewFile();
				report.mkdir();
				usdata.createNewFile();
				System.out.println(conf.getAbsolutePath());
				return true;
			} catch (IOException e) {
				System.out.println("not possible to create config File or folder...");
				return false;
			}
		}else {
			return true;
		}
	}
	
	public static List[] getUserAndPasswords(){
		try {
			bf= new BufferedReader(new InputStreamReader(new FileInputStream(userpasswdPath)));
			List <String> userNames= new ArrayList<String>();
			List <String> passwords= new ArrayList<String>();
			java.lang.String read;
			int counter=0;
			while((read= bf.readLine())!=null) {
				if(counter%2==0) {
					userNames.add((String) read);
				}else {
					passwords.add((String) read);
				}
				counter++;
			}
			return new List[] {userNames,passwords};
		}catch(Exception e) {
			System.out.println("could not read users or passwords!");
			e.printStackTrace();
			return null;
		}
	}
	
	public static void setUserAndPassword(String username, String password) {
		try {
			pw = new PrintWriter(new FileOutputStream(userpasswdPath));
			pw.println(username);
			pw.println(password);
		}catch(Exception e) {
			System.out.println("could not add user or password");
			e.printStackTrace();
		}
	}
	
	public static void setreport(List[]ecg,List[]eeg,String comments,String userName) {
		int counter=0;
		File manager = new File(System.getProperty("user.dir")+"\\reports\\"+userName+"report_"+counter+".txt");
		while(manager.isFile()) {
			counter++;
			manager = new File(System.getProperty("user.dir")+"\\reports\\"+userName+"report_"+counter+".txt");
		}
		PrintWriter data =null;
		try {
			System.out.println(manager.getAbsolutePath());
			manager.createNewFile();
			data= new PrintWriter(manager);
			Iterator iterator1= eeg[1].iterator();
			Iterator iterator3= ecg[1].iterator();
			
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
		}catch(Exception e) {
			System.out.println("could not create report file");
			e.printStackTrace();
		}finally {
			try {
				data.close();
			}catch(Exception e) {
				System.out.println("could not close report file");
				e.printStackTrace();
			}
				
		}
	}
	
	public static Report getReport(String report) {
		List<Double> time1 = new ArrayList <Double>();
		List<Double> time2 = new ArrayList <Double>();
		List<Double> ecg = new ArrayList <Double>();
		List<Double> eeg = new ArrayList <Double>();
		String comment="";
		
		File manager = new File(report);
		BufferedReader data= null;
		try {
			System.out.println(manager.getAbsolutePath());
			data = new BufferedReader(new InputStreamReader(new FileInputStream(manager)));
			boolean phaseOneComplete=false;
			boolean comments=false;
			String inputRead;
			while((inputRead=data.readLine())!=null) {
				try {
					parser(inputRead);
					if(!phaseOneComplete) {
						time1.add(parser(inputRead)[0]);
						eeg.add(parser(inputRead)[1]);
					}
					if(phaseOneComplete && ! comments) {
						time2.add(parser(inputRead)[0]);
						ecg.add(parser(inputRead)[1]);
					}
				}catch(Exception e) {
					if(inputRead.contains("ECG")) {
						phaseOneComplete=true;
					}
					if(inputRead.contains("COMMENTS")) {
						comments=true;
					}
					if(comments && !inputRead.contains("COMMENTS")) {
						comment=comment+"\n"+inputRead;
					}
				}
			}
			try {
				data.close();
			}catch(Exception e) {
				System.out.println("could not close reader");
				e.printStackTrace();
			}
			return new Report ((new List[]{time2, ecg}),(new List[]{time1, eeg}),comment);
		} catch (Exception e) {
			System.out.println("could not read report");
			e.printStackTrace();
			return null;
		}
	}
	
	public static User getUserConfig(String userName) {
		try {
			bf= new BufferedReader(new InputStreamReader(new FileInputStream(usersData)));
			User us=null;
			String read;
			while(true) {
				read=bf.readLine();
				if(read==null) {
					break;
				}else {
					if(read.equals(userName)) {
						String name= bf.readLine();
						String surname=bf.readLine();
						int weight= Integer.parseInt(bf.readLine());
						int age= Integer.parseInt(bf.readLine());
						char gender= bf.readLine().toCharArray()[0];
						us= new User(name,surname,weight,age,gender,userName);
						break;
					}else {
						continue;
					}
				}
			}
			return us;
		}catch(Exception e) {
			System.out.println("could not read user configuration!");
			e.printStackTrace();
			return null;
		}
	}
	
	public static void setUserConfig(User us) {
		try {
			pw= new PrintWriter(new FileOutputStream(usersData));
			pw.println(us.getUserName());
			pw.println(us.getName());
			pw.println(us.getSurname());
			pw.println(us.getWeight());
			pw.println(us.getAge());
			pw.println(us.getGender());
			pw.println();
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("could not add the user!");
		}
	}
	
	private static double[]parser(String data){
		char[] temp=data.toCharArray();
		double time=0;
		double input=0;
		String helper="";
		for (int i = 0; i < temp.length; i++) {
			if(temp[i]!=' ') {
				helper=helper+temp[i];
			}else {
				time=Double.parseDouble(helper);
				helper="";
			}
			if(i==temp.length-1) {
				input=Double.parseDouble(helper);
			}
		}
		return new double[] {time, input};
	}

	public static void close() {
		try {
			pw.close();
		}catch(Exception e) {
			System.out.println("could not close printwriter");
		}
		try {
			bf.close();
		}catch(Exception e) {
			System.out.println("could not close buffered reader");
		}
	}
}