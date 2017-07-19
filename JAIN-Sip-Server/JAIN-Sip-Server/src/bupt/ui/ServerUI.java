package bupt.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.swing.*;
import javax.swing.border.*;

import bupt.service.ServerLogic;
import bupt.variable.Info;

public class ServerUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private final JFrame server_frame = new JFrame();
	private final JPanel leftpanel = new JPanel();
	private final JPanel rightpanel = new JPanel();
	private final JPanel leftUpPanel = new JPanel();
	private final JPanel rightUpPanel = new JPanel();
	private final JPanel rightDownPanel = new JPanel();

	private final JLabel hostIp_jlabel = new JLabel("IP地址:", JLabel.CENTER);
	private final JLabel hostPort_jlabel = new JLabel("port:", JLabel.CENTER);
	private final JLabel Sip_jlable = new JLabel("Sip地址", JLabel.CENTER);

	private final JTextField hostIp_jtf = new JTextField("127.0.0.1", 15);
	private final JTextField hostPort_jtf = new JTextField("1234", 15);
	private final JTextField message = new JTextField(20);
	private final JTextField Sip_jtf = new JTextField("", 15);

	private final Box jtaBox = Box.createVerticalBox();
	private JTextArea message_history = new JTextArea(15, 36);
	private final JScrollPane j = new JScrollPane();
	private final JScrollPane leftDownPanel = new JScrollPane();

	private final JButton start_jbt = new JButton("开始");
	private final JButton stop_jbt = new JButton("ͣ停止");
	private final JButton send_jbt = new JButton("发送");

	private JComboBox<String> users = new JComboBox<String>();
	private DefaultListModel<String> dlsModel = new DefaultListModel<String>();
	private JList<String> userList = new JList<String>(dlsModel);

	private Boolean serverStarted = false;
	private String selectedUser = null;

	private static ServerLogic serverLogic = null;

	public ServerUI() {

		hostIp_jtf.setText(getHostAddress());
		hostIp_jlabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
		hostPort_jlabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
		hostIp_jtf.setFont(new Font("微软雅黑", Font.BOLD, 12));
		hostPort_jtf.setFont(new Font("微软雅黑", Font.BOLD, 12));
		//hostIp_jtf.setEditable(false);
		stop_jbt.setEnabled(false);
		send_jbt.setEnabled(false);

		TitledBorder tBorder1 = new TitledBorder("Server：");
		tBorder1.setTitleFont(new Font("微软雅黑", Font.BOLD, 12));
		leftUpPanel.setBorder(tBorder1);
		leftUpPanel.setLayout(new GridLayout(4, 2));
		leftUpPanel.add(hostIp_jlabel);
		leftUpPanel.add(hostIp_jtf);
		leftUpPanel.add(hostPort_jlabel);
		leftUpPanel.add(hostPort_jtf);
		leftUpPanel.add(Sip_jlable);
		leftUpPanel.add(Sip_jtf);
		leftUpPanel.add(start_jbt);
		leftUpPanel.add(stop_jbt);

		TitledBorder tBorder2 = new TitledBorder("在线用户");
		tBorder2.setTitleFont(new Font("微软雅黑", Font.BOLD, 12));
		leftDownPanel.setBorder(tBorder2);
		leftDownPanel.setViewportView(userList);

		leftpanel.setLayout(new BorderLayout());
		leftpanel.add(leftUpPanel, BorderLayout.NORTH);
		leftpanel.add(leftDownPanel, BorderLayout.CENTER);

		message_history.setLineWrap(true);
		message_history.setWrapStyleWord(true);
		message_history.setEditable(false);
		message_history.setFont(new Font("微软雅黑", Font.BOLD, 13));
		message_history.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

		jtaBox.add(message_history);
		j.setViewportView(jtaBox);

		TitledBorder tBorder3 = new TitledBorder("消息区：");
		tBorder3.setTitleFont(new Font("微软雅黑", Font.BOLD, 12));
		rightUpPanel.setBorder(tBorder3);
		rightUpPanel.setLayout(new BorderLayout());
		rightUpPanel.add(j, BorderLayout.CENTER);
		rightDownPanel.add(users);
		rightDownPanel.add(message);
		rightDownPanel.add(send_jbt);
		rightpanel.setLayout(new BorderLayout());
		rightpanel.add(rightUpPanel, BorderLayout.CENTER);
		rightpanel.add(rightDownPanel, BorderLayout.SOUTH);

		start_jbt.addActionListener(this);
		stop_jbt.addActionListener(this);
		send_jbt.addActionListener(this);
		message.addActionListener(this);
		users.addActionListener(this);

		server_frame.setLayout(new BorderLayout());
		server_frame.add(leftpanel, BorderLayout.WEST);
		server_frame.add(rightpanel, BorderLayout.CENTER);

		server_frame.setTitle("Server");
		server_frame.setSize(750, 500);
		server_frame.setLocationRelativeTo(null);
		server_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		server_frame.setVisible(true);
	}

	private void addCloseListener() {
		server_frame.addWindowListener(new WindowAdapter() { 
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				serverLogic.stopServer();
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == start_jbt) {
			int hostPort = Integer.parseInt(hostPort_jtf.getText().trim());
			String ip = hostIp_jtf.getText().trim();
			if (!serverLogic.startServer(hostPort,ip))
				JOptionPane.showMessageDialog(server_frame, Info.SERVER_START_FAILED);
			else {
				serverStarted = true;
				hostPort_jtf.setEditable(false);
				start_jbt.setEnabled(false);
				stop_jbt.setEnabled(true);
				send_jbt.setEnabled(true);
			}
		} else if (e.getSource() == stop_jbt) {
			if (!serverLogic.stopServer())
				JOptionPane.showMessageDialog(server_frame, Info.SERVER_STOP_FAILED);
			else {
				hostPort_jtf.setEditable(true);
				start_jbt.setEnabled(true);
				stop_jbt.setEnabled(false);
				serverStarted = false;
			}
		} else if (e.getSource() == users)
			selectedUser = (users.getSelectedItem() == null) ? null : users.getSelectedItem().toString();
		else if (e.getSource() == send_jbt || e.getSource() == message) {
//			if (serverStarted) {
//				String sermessage = message.getText().trim();
//				if (dlsModel.isEmpty())
//					JOptionPane.showMessageDialog(server_frame, "�������ߣ��޷�ͨ����");
//				else if (selectedUser == "ALL" || selectedUser == null)
//					try {
//						//serverLogic.sendMessageToAll(sermessage);
//					} catch (ParseException | InvalidArgumentException | SipException e2) {
//						e2.printStackTrace();
//					}
//				else
//					try {
//						//serverLogic.sendMessageToSelected(sermessage, selectedUser);
//					} catch (ParseException | InvalidArgumentException | SipException e1) {
//						e1.printStackTrace();
//					}
//			} else
//				JOptionPane.showMessageDialog(server_frame, "������δ����������������������");
//			message.setText("");
		}
	}

	public static void main(String[] args) {
		ServerUI sUi = new ServerUI();
		sUi.addCloseListener();
		serverLogic = new ServerLogic(sUi);
	}

	/*
	 * 下面的都是各种 get();
	 */
	public JTextArea getMessage_history() {
		return message_history;
	}

	public JComboBox<String> getUsers() {
		return users;
	}

	public DefaultListModel<String> getDlsModel() {
		return dlsModel;
	}

	public JTextField getSip_jtf() {
		return Sip_jtf;
	}

	public static String getHostAddress() {
		String serverIpStr = "";
		try {
			serverIpStr = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return (serverIpStr = "127.0.0.1");
		}
		return serverIpStr;
	}
}
