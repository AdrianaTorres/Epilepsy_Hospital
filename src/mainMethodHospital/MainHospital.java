package mainMethodHospital;

import java.util.ArrayList;

import connectionManager.Server;
import connectionManager.Server_Doctor;
import fileManager.FileManager;
import guiHospital.AdminCreate;
import guiHospital.GuiHospital;

public class MainHospital {

	public static void main(String[] args) throws Exception {
		if (FileManager.firstTimeLaunch()) {
			AdminCreate ac= new AdminCreate();
		} else {
			new Thread(new GuiHospital(new ArrayList<String>())).start();
			new Thread(new Server()).start();
			new Thread(new Server_Doctor()).start();
		}

	}
}