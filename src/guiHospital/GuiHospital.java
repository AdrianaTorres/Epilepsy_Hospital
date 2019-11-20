package guiHospital;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;
import javax.swing.SwingConstants;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GuiHospital extends JFrame implements Runnable{
	private static JList list;
	private JPanel contentPane;
	private JTextField tag;
	private static List<String> users;
	/**
	 * Create the frame.
	 */
	public GuiHospital(List<String>activePatients) {
		this.users=activePatients;
	}

	private static String[] filterList(String name) {
		List <String> matches=new ArrayList<String>();
		int counter=0;
		for (Iterator iterator = users.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if(string.contains(name)) {
				matches.add(string);
				counter++;
			}
		} 
		String[]match=new String[counter];
		counter=0;
		for (Iterator iterator = matches.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			match[counter]=string;
			counter++;
		}
		return (match);
	}
	private static void updateList(String[]filteredList) {
		DefaultListModel<String> lm= new DefaultListModel<String>();
		for (int i = 0; i < filteredList.length; i++) {
			lm.addElement(filteredList[i]);
		}
		list.removeAll();
		list.setModel(lm);
		list.setVisible(false);
		list.setVisible(true);
	}
	public static void updateClients(List<String> clients) {
		users=clients;
	}

	@Override
	public void run() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		super.setExtendedState(super.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		Font ui =new Font("Segoe UI",Font.PLAIN,15);

		JPanel panel = new JPanel();
		contentPane.add(panel,BorderLayout.EAST);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1,BorderLayout.WEST);

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2,BorderLayout.NORTH);

		JPanel panel_3 = new JPanel();
		contentPane.add(panel_3,BorderLayout.SOUTH);

		panel_1.setBackground(Color.BLACK);
		panel_2.setBackground(Color.BLACK);
		panel_3.setBackground(Color.BLACK);

		panel.setBackground(Color.BLACK);

		list = new JList();
		list.setCellRenderer(new FancyListSellection());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(arg0.getClickCount()==2 && arg0.getButton()==arg0.BUTTON1) {
					/*CALL FOR THE PATIENT VISUALIZER*/
					int index=list.getSelectedIndex();
					String name=(String) list.getModel().getElementAt(index);
				}
			}
		});
		try {
			list.setSelectedIndex(0);
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("list could not be initialized");
		}

		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {

			}
		});
		list.setBackground(Color.DARK_GRAY);
		list.setForeground(Color.WHITE);
		list.setFont(ui);
		contentPane.add(new JScrollPane(list), BorderLayout.CENTER);

		DefaultListModel<String> listModel =new DefaultListModel<String>();
		for (Iterator<String> iterator = this.users.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			listModel.addElement(string);
		}
		list.setModel(listModel);
		JLabel label=new JLabel("All Seeing Eye");
		JLabel label_1=new JLabel("devBuild alpha 1.0");
		JLabel label_2=new JLabel("Telemedicine 2019-2020");
		label.setForeground(Color.WHITE);
		label_1.setForeground(Color.WHITE);
		label_2.setForeground(Color.WHITE);
		label.setFont(ui);
		label_1.setFont(ui);
		label_2.setFont(ui);
		panel_2.setLayout(new GridLayout(1,3));
		panel_2.add(label);
		panel_2.add(label_1);
		panel_2.add(label_2);

		JLabel label_3=new JLabel("Browse:");
		label_3.setHorizontalAlignment(SwingConstants.LEFT);
		label_3.setForeground(Color.WHITE);
		label_3.setFont(ui);

		JButton search= new JButton("Go");
		search.setBackground(Color.GRAY);
		search.setFont(ui);
		search.setForeground(Color.WHITE);

		panel.setLayout(new GridLayout(2,1));
		JPanel panel_4=new JPanel();
		panel_4.setLayout(new BorderLayout());
		panel_4.setBackground(Color.black);

		try {
			BufferedImage nominal;
			nominal = ImageIO.read(new File("C:\\Users\\Sloth Thy Lord\\Documents\\Sloth thy lord\\Biomédica\\quinto año\\Telemedicina\\logo.jpg"));
			JLabel picLabel = new JLabel(new ImageIcon(nominal));
			panel_4.add(picLabel,BorderLayout.NORTH);
		}catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("failed to import image");
		}
		JLabel label_4=new JLabel("NOT powered by Unity");
		label_4.setHorizontalAlignment(SwingConstants.CENTER);
		label_4.setForeground(Color.WHITE);
		label_4.setFont(ui);

		panel_4.add(label_4,BorderLayout.CENTER);
		panel.add(panel_4);

		JPanel panel_5=new JPanel();
		panel_5.setBackground(Color.BLACK);
		/*panel_5.add(filler);
		panel_5.add(search);*/
		panel.add(panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel_5.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel_5.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_5.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_5.setLayout(gbl_panel_5);

		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridx = 1;
		gbc_verticalStrut.gridy = 0;
		panel_5.add(verticalStrut, gbc_verticalStrut);

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_1.gridx = 1;
		gbc_verticalStrut_1.gridy = 1;
		panel_5.add(verticalStrut_1, gbc_verticalStrut_1);

		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 2;
		panel_5.add(label_3, gbc_lblNewLabel);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut.gridx = 0;
		gbc_horizontalStrut.gridy = 3;
		panel_5.add(horizontalStrut, gbc_horizontalStrut);

		tag= new JTextField();
		tag.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()!=KeyEvent.VK_BACK_SPACE) {
					String[]updatedList =GuiHospital.filterList(tag.getText()+e.getKeyChar());
					GuiHospital.updateList(updatedList);
				}else {
					if(tag.getText().equals("")) {
						String[]clients=new String[users.size()];
						for (int i = 0; i < clients.length; i++) {
							clients[i]=users.get(i);
						}
						GuiHospital.updateList(clients);
					}else {
						char[] text=tag.getText().toCharArray();
						String search="";
						for (int i = 0; i < text.length-1; i++) {
							search=search+text[i];
						}
						String[]updatedList =GuiHospital.filterList(search);
						GuiHospital.updateList(updatedList);
					}
				}
			}
		});																												
		tag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String[]updatedList =GuiHospital.filterList(tag.getText());
				GuiHospital.updateList(updatedList);																																																			
			}																									
		});														

		tag.setBackground(Color.GRAY);
		tag.setFont(ui);
		tag.setForeground(Color.BLACK);

		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 3;
		panel_5.add(tag, gbc_textField);
		//textField.setColumns(10);

		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.insets = new Insets(0, 0, 5, 5);
		gbc_button.fill = GridBagConstraints.HORIZONTAL;
		gbc_button.gridx = 2;
		gbc_button.gridy = 3;
		panel_5.add(search, gbc_button);
		//textField.setColumns(10);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_2 = new GridBagConstraints();
		gbc_horizontalStrut_2.insets = new Insets(0, 0, 5, 0);
		gbc_horizontalStrut_2.gridx = 3;
		gbc_horizontalStrut_2.gridy = 3;
		panel_5.add(horizontalStrut_2, gbc_horizontalStrut_2);

		super.setVisible(true);
	}
}
