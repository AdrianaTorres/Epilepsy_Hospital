package mainMethodHospital;

import java.util.ArrayList;

import connectionManager.Server;
import connectionManager.Server_Doctor;
import guiHospital.GuiHospital;

public class MainHospital {

	public static void main(String[] args) throws Exception {
		new Thread(new GuiHospital(new ArrayList<String>())).start();
		new Thread(new Server()).start();
		new Thread (new Server_Doctor()).start();
	}
}