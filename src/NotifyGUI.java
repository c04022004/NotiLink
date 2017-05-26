import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.json.JSONException;
import org.json.JSONObject;

public class NotifyGUI {
	private JFrame frame = new JFrame();
	private JLabel label1 = new JLabel("NotiLink System v1.01\n");
	private JTextArea dispBox;
	private JScrollPane scrollPane;
	private JButton resetButton;
	private JButton emergencyButton;

	private Collection<Integer> notiRecord = new HashSet<Integer>();
	private Collection<RaspiNode> nodeRecord = new HashSet<RaspiNode>();

	public void nodeSetup() {
		nodeRecord.add(new RaspiNode("pi151", "192.168.1.151", 8888));
		nodeRecord.add(new RaspiNode("pi152", "192.168.1.152", 8888));
	}

	public void createAndShowGUI() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container pane = frame.getContentPane();

		dispBox = new JTextArea("", 10, 20);
		dispBox.setEditable(false);
		scrollPane = new JScrollPane(dispBox);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		resetButton = new JButton("Reset all notification");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				notiRecord.clear();
				for (RaspiNode rn : nodeRecord) {
					new Thread(new NodeUpdater(rn, MessageStatus.NOTIFY)).run();
				}

				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// dispBox.append(obj.toString() + "\n");
						dispBox.append("You have " + notiRecord.size() + " unread message\n");
					}
				});
			}
		});
		emergencyButton = new JButton("Send emergency SMS");
		emergencyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Process process;
				try {
					process = (new ProcessBuilder(PushbulletListener.pythonPath, "-u",
							PushbulletListener.scriptFolder + "/send_sms.py", "I am in a trouble!"))
									.start();
					try (InputStreamReader isr = new InputStreamReader(process.getInputStream());
							BufferedReader br = new BufferedReader(isr)) {
						while (br.readLine() != null) {
						}
						Thread.sleep(1000);
						if (process.exitValue() == 0) {
							System.out.println("SMS sent");
						} else {
							System.out.println("SMS failed, press again to retry.");
						}
					} catch (IOException | InterruptedException e2) {
						e2.printStackTrace();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});
		buttonPane.add(resetButton);
		buttonPane.add(emergencyButton);
		pane.setLayout(new BorderLayout());

		JPanel labelPane = new JPanel();
		labelPane.setLayout(new BorderLayout());
		labelPane.add(label1, BorderLayout.NORTH);

		labelPane.add(new JSeparator(), BorderLayout.SOUTH);
		labelPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		pane.add(labelPane, BorderLayout.NORTH);
		pane.add(buttonPane, BorderLayout.CENTER);
		pane.add(scrollPane, BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);
		frame.setTitle("GUI interface");

		PushbulletTask pt = new PushbulletTask();
		pt.execute();

		DoorbellTask dt = new DoorbellTask();
		dt.execute();

	}

	public static void main(String args[]) {
		NotifyGUI myGUI = new NotifyGUI();
		myGUI.nodeSetup();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				myGUI.createAndShowGUI();
			}
		});
	}

	class PushbulletTask extends SwingWorker<Object, Void> {

		@Override
		protected Object doInBackground() {
			PushbulletListener pl = PushbulletListener.getInstance();
			try {
				while (true) {
					JSONObject obj = pl.getMessage();
					System.out.println(obj);

					if (obj.get("type").equals("nop")) {
						for (RaspiNode rn : nodeRecord) {
							new Thread(new NodeUpdater(rn, MessageStatus.NOTIFY)).run();
						}
					} else if (obj.get("type").equals("tickle")) {
					} else if (obj.get("type").equals("push")) {
						try {
							JSONObject push = (JSONObject) obj.get("push");
							if (push.get("type").equals("mirror")) {
								notiRecord.add(
										Integer.parseInt(push.get("notification_id").toString()));
							} else if (push.get("type").equals("dismissal")) {
								notiRecord.remove(
										Integer.parseInt(push.get("notification_id").toString()));
							}
							for (RaspiNode rn : nodeRecord) {
								new Thread(new NodeUpdater(rn, MessageStatus.NOTIFY)).run();
							}
							// System.out.println(notiRecord);
						} catch (NumberFormatException | JSONException e) {
							e.printStackTrace();
						}
					}

					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							// dispBox.append(obj.toString() + "\n");
							dispBox.append("You have " + notiRecord.size() + " unread message\n");
						}
					});
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void done() {
			System.out.println("Pushbullet Thread stopped.");
			return;
		}
	}

	class DoorbellTask extends SwingWorker<Object, Void> {

		@Override
		protected Object doInBackground() {

			int port = 9080;
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Exiting...");
				System.exit(0);
			}
			System.out.println("serverSocket created on " + port);

			while (true) {
				try (Socket clientSocket = serverSocket.accept();
						Scanner in = new Scanner(clientSocket.getInputStream());
						PrintWriter out = new PrintWriter(clientSocket.getOutputStream())) {

					String s = in.nextLine();
					System.out.println(s);

					if (s.equals("doorbell")) {
						javax.swing.SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								dispBox.append("Someone is at the door!\n");
							}
						});

						for (RaspiNode rn : nodeRecord) {
							new Thread(new NodeUpdater(rn, MessageStatus.DOORBELL)).run();
						}

						try {
							Process process = (new ProcessBuilder(PushbulletListener.pythonPath,
									"-u", PushbulletListener.scriptFolder + "/stream_push.py",
									"Someone is at the door!")).start();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						javax.swing.SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								dispBox.append(s + "\n");
							}
						});
						try {
							Process process = (new ProcessBuilder(PushbulletListener.pythonPath,
									"-u", PushbulletListener.scriptFolder + "/stream_push.py", s))
											.start();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					out.write("success");
					out.flush();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void done() {
			System.out.println("Pushbullet Thread stopped.");
			return;
		}
	}

	class NodeUpdater implements Runnable {

		private RaspiNode node;
		private MessageStatus msg;

		public NodeUpdater(RaspiNode node, MessageStatus msg) {
			this.node = node;
			this.msg = msg;
		}

		@Override
		public void run() {
			try (Socket socket = new Socket(node.getIp(), node.getPort());
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
				ArrayList<Object> outArr = new ArrayList<Object>();
				outArr.add(this.msg);
				outArr.add(new Integer(notiRecord.size()));
				out.writeObject(outArr);
				out.flush();

				ArrayList<Object> inArr = (ArrayList<Object>) in.readObject();
				node.setTemp((Double) inArr.get(1));
				node.setPres((Double) inArr.get(2));
				System.out.println("Temp: " + node.getTemp() + "; Pres: " + node.getPres());

			} catch (IOException e) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						dispBox.append("\nCan't connect to raspi node!\n");
						dispBox.append("\nCheck the kitchen for the risk of fire\n");
					}
				});

				try {
					Process process = (new ProcessBuilder(PushbulletListener.pythonPath, "-u",
							PushbulletListener.scriptFolder + "/stream_push.py",
							"Check the kitchen for the risk of fire!")).start();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}

	}

}

class RaspiNode {

	final private String name;
	final private String ip;
	final private int port;
	private double temp = 25;
	private double pres = 1013;

	public RaspiNode(String name, String ip, int port) {
		this.name = name;
		this.ip = ip;
		this.port = port;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the temp
	 */
	synchronized public double getTemp() {
		return temp;
	}

	/**
	 * @param temp
	 *            the temp to set
	 */
	synchronized public void setTemp(double temp) {
		this.temp = temp;
	}

	/**
	 * @return the pres
	 */
	synchronized public double getPres() {
		return pres;
	}

	/**
	 * @param pres
	 *            the pres to set
	 */
	synchronized public void setPres(double pres) {
		this.pres = pres;
	}

}