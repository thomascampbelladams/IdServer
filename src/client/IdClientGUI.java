package client;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.Box;
import java.awt.FlowLayout;

import server.Server;
import server.User;
import server.Users;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JList;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;


public class IdClientGUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4158719252817858652L;
	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JPasswordField passwordField;
	private JPasswordField passwordField_1;
	private JTextField textField_2;
	private JTextArea textArea;
	private JLabel lblNewLabel_1;

	private IdClient client;
	private Server server;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextField Username;
	private JPasswordField Password;
	private JTextField NewUsername;
	private JPasswordField passwordField_2;
	private JTextField OldUsername;
	private JTextField UsernameText;
	private JPasswordField NewPassword;
	private JPasswordField OldPassword;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IdClientGUI frame = new IdClientGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public IdClientGUI() {

		setTitle("IdClientGUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 360);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);

		JMenuItem mntmConnect = new JMenuItem("Connect");
		mntmConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				try {
					client = new IdClient(JOptionPane.showInputDialog("Host: "));
					if (client.error != null)
						throw new Exception(client.error);
					server = client.getServer();
				} catch (Exception e) {
					ErrorMsg(e.getMessage());
					e.printStackTrace();
					return;
				}
				SuccessMsg("Connection Succeeded");
			}
		});
		mnNewMenu.add(mntmConnect);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		mnNewMenu.add(mntmExit);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		tabbedPane.addTab("Create", null, panel, null);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new TitledBorder(null, "Create User", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_6);
		panel_6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		Box verticalBox_1 = Box.createVerticalBox();
		panel_6.add(verticalBox_1);

		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_1);

		JLabel lblLoginName = new JLabel("Login Name: ");
		horizontalBox_1.add(lblLoginName);

		textField = new JTextField();
		horizontalBox_1.add(textField);
		textField.setColumns(10);

		Box horizontalBox_2 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_2);

		JLabel lblRealUsername = new JLabel("Real Username: ");
		horizontalBox_2.add(lblRealUsername);

		textField_1 = new JTextField();
		horizontalBox_2.add(textField_1);
		textField_1.setColumns(10);
		textField_1.setText(System.getProperty("user.name"));

		Box horizontalBox_3 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_3);

		JLabel lblPassword = new JLabel("Password: ");
		horizontalBox_3.add(lblPassword);

		passwordField = new JPasswordField();
		horizontalBox_3.add(passwordField);

		Box horizontalBox_4 = Box.createHorizontalBox();
		verticalBox_1.add(horizontalBox_4);

		JLabel lblNewLabel = new JLabel("Verify: ");
		horizontalBox_4.add(lblNewLabel);

		passwordField_1 = new JPasswordField();
		horizontalBox_4.add(passwordField_1);

		JButton btnNewButton = new JButton("Create");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = textField.getText();
				String realusername = textField_1.getText();
				String password = "";
				if (Arrays.equals(passwordField.getPassword(), passwordField_1.getPassword() ) )
					password = new String(passwordField.getPassword());
				else
				{
					ErrorMsg("Password mismatch.");
					return;
				}

				try {

					server.CreateUser(username, realusername, password);
				} catch (Exception e1) {
					ErrorMsg(e1.getMessage());
					e1.printStackTrace();
					return;
				}
				SuccessMsg("Created User " + username);
			}
		});
		verticalBox_1.add(btnNewButton);

		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("Lookup", null, panel_1, null);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));

		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new TitledBorder(null, "Lookup User", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.add(panel_7);

		Box verticalBox = Box.createVerticalBox();
		panel_7.add(verticalBox);

		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);

		 lblNewLabel_1 = new JLabel("Username: ");
		horizontalBox.add(lblNewLabel_1);

		textField_2 = new JTextField();
		horizontalBox.add(textField_2);
		textField_2.setColumns(10);

		final JCheckBox chckbxReverseLookup = new JCheckBox("Reverse Lookup");
		chckbxReverseLookup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if ( chckbxReverseLookup.isSelected() )
					 lblNewLabel_1.setText("UUID: ");
				else
					lblNewLabel_1.setText("Username: ");
			}
		});
		
		
		verticalBox.add(chckbxReverseLookup);	


		JPanel panel_8 = new JPanel();
		panel_8.setBorder(new TitledBorder(null, "User Info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.add(panel_8);
		panel_8.setLayout(new BorderLayout(0, 0));
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		panel_8.add(textArea, BorderLayout.CENTER);
		
		JButton btnLookup = new JButton("Lookup");
		btnLookup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String usernameuuid = textField_2.getText();
				User user = null;
				try
				{
					if(chckbxReverseLookup.isSelected() )
						user = server.RLookup(usernameuuid);
					else
						user = server.Lookup(usernameuuid);
				}catch (Exception e2)
				{
					ErrorMsg(e2.getMessage());
					e2.printStackTrace();
					return;
				}
				if(user != null)
					textArea.setText(user.toString());
				else
					textArea.setText("");
				
			}
		});
		verticalBox.add(btnLookup);

		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("Modify Username", null, panel_3, null);
		panel_3.setLayout(null);
		
		NewUsername = new JTextField();
		NewUsername.setBounds(111, 46, 154, 19);
		panel_3.add(NewUsername);
		NewUsername.setColumns(10);
		
		passwordField_2 = new JPasswordField();
		passwordField_2.setBounds(111, 77, 154, 19);
		panel_3.add(passwordField_2);
		
		OldUsername = new JTextField();
		OldUsername.setBounds(111, 15, 154, 19);
		panel_3.add(OldUsername);
		OldUsername.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("Old Username");
		lblNewLabel_4.setBounds(12, 17, 99, 15);
		panel_3.add(lblNewLabel_4);
		
		JLabel lblNewLabel_5 = new JLabel("New Username");
		lblNewLabel_5.setBounds(12, 48, 93, 15);
		panel_3.add(lblNewLabel_5);
		
		JLabel lblNewLabel_6 = new JLabel("Password");
		lblNewLabel_6.setBounds(12, 79, 61, 15);
		panel_3.add(lblNewLabel_6);
		
		JButton btnNewButton_1 = new JButton("Change");
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String oldUsername = OldUsername.getText();
				String newUsername = NewUsername.getText();
				String password = "";
				try {
					password = (new String(passwordField_2.getPassword()));
				} catch (Exception e5) {
					ErrorMsg(e5.getMessage());
					return;
				}
				
				int rval = 0;
				try {
					rval = server.ModifyUser(oldUsername, newUsername, password);
				} catch (Exception e6) {
					ErrorMsg(e6.getMessage());
					return;
				}
				if (rval >= 1)
					SuccessMsg(oldUsername + " changed to " + newUsername);
				else
					ErrorMsg("Nothing modified.");
			}
		});
		btnNewButton_1.setBounds(80, 135, 107, 25);
		panel_3.add(btnNewButton_1);
		
		JPanel panel_9 = new JPanel();
		tabbedPane.addTab("Modify Password", null, panel_9, null);
		panel_9.setLayout(null);
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setBounds(12, 14, 99, 15);
		panel_9.add(lblUsername);
		
		JLabel lblOldPassword = new JLabel("Old Password");
		lblOldPassword.setBounds(12, 45, 93, 15);
		panel_9.add(lblOldPassword);
		
		JLabel lblNewPassword = new JLabel("New Password");
		lblNewPassword.setBounds(12, 76, 93, 15);
		panel_9.add(lblNewPassword);
		
		UsernameText = new JTextField();
		UsernameText.setColumns(10);
		UsernameText.setBounds(111, 12, 154, 19);
		panel_9.add(UsernameText);
		
		NewPassword = new JPasswordField();
		NewPassword.setBounds(111, 74, 154, 19);
		panel_9.add(NewPassword);
		
		JButton button = new JButton("Change");
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String username = UsernameText.getText();
				String oldPassword = "";
				String newPassword = "";
				try {
					oldPassword = (new String( OldPassword.getPassword()));
					newPassword = (new String (NewPassword.getPassword()));
				} catch (Exception e8) {
					ErrorMsg(e8.getMessage());
					return;
				}
				int rval = 0;
				try {
					rval = server.modifyPassword(username, oldPassword, newPassword);
					
				} catch (Exception e7) {
					ErrorMsg(e7.getMessage());
					return;
				}
				
				if (rval >= 1)
					SuccessMsg("Password changed succesfully.");
				else
					ErrorMsg("Error, maybe bad username or password.");
				
			}
		});
		button.setBounds(80, 132, 107, 25);
		panel_9.add(button);
		
		OldPassword = new JPasswordField();
		OldPassword.setBounds(111, 43, 154, 19);
		panel_9.add(OldPassword);

		JPanel panel_4 = new JPanel();
		tabbedPane.addTab("Delete", null, panel_4, null);
		panel_4.setLayout(null);
		
		Username = new JTextField();
		Username.setBounds(78, 14, 187, 19);
		panel_4.add(Username);
		Username.setColumns(10);
		
		JButton DeleteButton = new JButton("Delete");
		DeleteButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {


				String username = Username.getText();
				String password = "";
				try {
					password = ( new String(Password.getPassword() ) );
				} catch (Exception e1){
					ErrorMsg(e1.getMessage());
					return;
				}
				int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this user?");
				if(answer == 0){
					int rval = 0;
					try {
						rval = server.DeleteUser(username, password);
						
					} catch (Exception e1) 
					{
						ErrorMsg(e1.getMessage());
						return;
					}
					if (rval >= 1)
						SuccessMsg(username + " deleted.");
					else
						ErrorMsg("Unable to delete: "+username);
				}
			}
		});
		DeleteButton.setBounds(103, 135, 74, 25);
		panel_4.add(DeleteButton);
		
		Password = new JPasswordField();
		Password.setToolTipText("Password");
		Password.setBounds(78, 45, 187, 19);
		panel_4.add(Password);
		
		JLabel lblNewLabel_2 = new JLabel("Username");
		lblNewLabel_2.setBounds(12, 14, 73, 15);
		panel_4.add(lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("Password");
		lblNewLabel_3.setBounds(12, 45, 74, 19);
		panel_4.add(lblNewLabel_3);

		JPanel panel_5 = new JPanel();
		tabbedPane.addTab("Get", null, panel_5, null);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		Box verticalBox_2 = Box.createVerticalBox();
		panel_5.add(verticalBox_2);
		
		Box horizontalBox_5 = Box.createHorizontalBox();
		verticalBox_2.add(horizontalBox_5);
		
		JPanel panel_11 = new JPanel();
		panel_11.setBorder(new TitledBorder(null, "Select Users", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		horizontalBox_5.add(panel_11);
		
		JPanel panel_10 = new JPanel();
		verticalBox_2.add(panel_10);
		panel_10.setBorder(new TitledBorder(null, "User list", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_10.setLayout(new BorderLayout(0, 0));
		
		final JList list = new JList();
		list.setModel(new DefaultListModel());
		panel_10.add(list);
		
		JRadioButton radioButton = new JRadioButton("UUID");
		radioButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				try {
					Users users = server.GetUsers();
					ArrayList<User> userlist = users.getUserList();
					Iterator<User> it = userlist.iterator();
					DefaultListModel model = (DefaultListModel) list.getModel();
					model.clear();
					while(it.hasNext() )
						model.addElement(it.next().getUuid());
					
				}catch (Exception e1)
				{
					ErrorMsg(e1.getMessage());
					e1.printStackTrace();
				}
				

				
			}
		});
		panel_11.add(radioButton);
		buttonGroup.add(radioButton);
		
		JRadioButton radioButton_1 = new JRadioButton("Name");
		radioButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					Users users = server.GetUsers();
					ArrayList<User> userlist = users.getUserList();
					Iterator<User> it = userlist.iterator();
					DefaultListModel model = (DefaultListModel) list.getModel();
					model.clear();
					while(it.hasNext() )
						model.addElement(it.next().getUsername());
					
				}catch (Exception e1)
				{
					ErrorMsg(e1.getMessage());
					e1.printStackTrace();
				}
				
				
			}
		});
		panel_11.add(radioButton_1);
		buttonGroup.add(radioButton_1);
		
		JRadioButton radioButton_2 = new JRadioButton("All");
		radioButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					Users users = server.GetUsers();
					ArrayList<User> userlist = users.getUserList();
					Iterator<User> it = userlist.iterator();
					DefaultListModel model = (DefaultListModel) list.getModel();
					model.clear();
					while(it.hasNext() )
						model.addElement(it.next());
					
				}catch (Exception e1)
				{
					ErrorMsg(e1.getMessage());
					e1.printStackTrace();
				}
			}
		});
		panel_11.add(radioButton_2);
		buttonGroup.add(radioButton_2);
		

	}

	private void ErrorMsg(String e)
	{
		JOptionPane.showMessageDialog(null,
				(e == null) ? "Unkown error." : e,
						"Error",
						JOptionPane.ERROR_MESSAGE);
	}

	private void SuccessMsg(String e)
	{		
		JOptionPane.showMessageDialog(null,
				(e == null) ? "Success!": e ,
						"Success",
						JOptionPane.INFORMATION_MESSAGE);

	}
}
