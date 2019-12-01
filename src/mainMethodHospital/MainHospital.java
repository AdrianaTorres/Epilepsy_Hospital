package mainMethodHospital;

import java.util.ArrayList;

import connectionManager.Server;
import guiHospital.GuiHospital;

public class MainHospital {

	public static void main(String[] args) throws Exception {
		new Thread(new GuiHospital(new ArrayList<String>())).start();
		new Thread(new Server()).start();
	}
}