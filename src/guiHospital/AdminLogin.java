package guiHospital;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import connectionManager.HospitalConnection;
import connectionManager.HospitalDoctorConnection;
import connectionManager.Server;
import connectionManager.Server_Doctor;
import fileManager.FileManager;

public class AdminLogin {
	private JFrame f;
	private JPanel contentPane;
	public String user;
	public String password;
	public AdminLogin() {
		f= new JFrame();
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation(dim.width/2-f.getSize().width/2, dim.height/2-f.getSize().height/2);
		f.setResizable(false);
		f.setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout());
		JPanel panel_1 = new JPanel();
		JPanel panel_2 = new JPanel();
		JPanel panel_3 = new JPanel();
		
		panel_1.setBackground(Color.GRAY);
		panel_2.setBackground(Color.black);
		panel_3.setBackground(Color.BLACK);
		
		Font ui = new Font("Segoe UI", Font.PLAIN,12);
		JLabel lblServerShutDown= new JLabel("Server Shut Down");
		JLabel label_2= new JLabel("User Name");
		JLabel label_3= new JLabel("Password");
		
		lblServerShutDown.setFont(ui);
		label_2.setFont(ui);
		label_3.setFont(ui);
		
		lblServerShutDown.setForeground(Color.BLACK);
		label_2.setForeground(Color.WHITE);
		label_3.setForeground(Color.WHITE);
		
		JTextField text_1 = new JTextField();
		JPasswordField text_2 = new JPasswordField();
		text_1.setFont(ui);
		text_2.setFont(ui);
		
		text_1.setBackground(Color.GRAY);
		text_2.setBackground(Color.GRAY);
		
		JButton btnConfirm= new JButton("Confirm");
		btnConfirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String temp="";
				for (int i = 0; i < text_2.getPassword().length; i++) {
					temp= temp+text_2.getPassword()[i];			
				}
				boolean confirm=FileManager.rootMeUp(temp,text_1.getText());
				if(confirm) {
					HospitalConnection.releaseResources(HospitalConnection.inputStream, HospitalConnection.outputStream,
							HospitalConnection.pw, HospitalConnection.bf, HospitalConnection.socket);
					Server.releaseResourcesServer(Server.serverSocket);
					HospitalDoctorConnection.releaseResources(HospitalDoctorConnection.inputStream, HospitalDoctorConnection.outputStream,
							HospitalDoctorConnection.pw, HospitalDoctorConnection.bf, HospitalDoctorConnection.socket);
					Server_Doctor.releaseResourcesServer(Server_Doctor.serverSocket_Doctor);
					System.exit(0);
				}
				else {
					f.dispose();
				}
			}
		});
		JButton button_2= new JButton("Cancel");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				f.dispose();
			}
		});
		
		btnConfirm.setBackground(Color.DARK_GRAY);
		button_2.setBackground(Color.DARK_GRAY);
		
		btnConfirm.setForeground(Color.WHITE);
		button_2.setForeground(Color.WHITE);
		
		btnConfirm.setFont(ui);
		button_2.setFont(ui);
		
		contentPane.add(panel_1, BorderLayout.NORTH);
		panel_1.add(lblServerShutDown);
		contentPane.add(panel_2, BorderLayout.CENTER);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 0.5, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(80);
		GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
		gbc_horizontalStrut_1.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut_1.gridx = 0;
		gbc_horizontalStrut_1.gridy = 0;
		panel_2.add(horizontalStrut_1, gbc_horizontalStrut_1);
		
		Component horizontalStrut = Box.createHorizontalStrut(80);
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut.gridx = 0;
		gbc_horizontalStrut.gridy = 3;
		panel_2.add(horizontalStrut, gbc_horizontalStrut);
		contentPane.add(panel_3, BorderLayout.SOUTH);
		
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 1;
		gbc_label_1.gridy = 1;
		panel_2.add(label_2, gbc_label_1);
		
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.insets = new Insets(0, 0, 5, 5);
		gbc_label_2.gridx = 1;
		gbc_label_2.gridy = 3;
		panel_2.add(label_3, gbc_label_2);
		
		GridBagConstraints gbc_text_1 = new GridBagConstraints();
		gbc_text_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_text_1.insets = new Insets(0, 0, 5, 5);
		gbc_text_1.gridx = 2;
		gbc_text_1.gridy = 1;
		panel_2.add(text_1, gbc_text_1);
		
		GridBagConstraints gbc_text_2 = new GridBagConstraints();
		gbc_text_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_text_2.insets = new Insets(0, 0, 5, 5);
		gbc_text_2.gridx = 2;
		gbc_text_2.gridy = 3;
		panel_2.add(text_2, gbc_text_2);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_1.gridx = 2;
		gbc_verticalStrut_1.gridy = 2;
		panel_2.add(verticalStrut_1, gbc_verticalStrut_1);
		
		Component verticalStrut_2 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_2 = new GridBagConstraints();
		gbc_verticalStrut_2.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_2.gridx = 1;
		gbc_verticalStrut_2.gridy = 0;
		panel_2.add(verticalStrut_2, gbc_verticalStrut_2);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridx = 2;
		gbc_verticalStrut.gridy = 4;
		panel_2.add(verticalStrut, gbc_verticalStrut);
		
		panel_3.add(btnConfirm);
		panel_3.add(button_2);
		f.setVisible(true);
		
		user = text_1.getText();
		password = text_2.getText();
	}
}
