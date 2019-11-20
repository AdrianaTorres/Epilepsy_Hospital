package mainMethodHospital;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import connectionManager.HospitalConnection;
import connectionManager.Server;
import guiHospital.GuiHospital;

public class MainHospital {
	
	public static void main(String[] args) throws Exception {
	new Thread(new GuiHospital(new ArrayList<String>())).start();
	new Thread(new Server()).start();
	}
	}