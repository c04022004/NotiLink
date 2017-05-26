import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DisplayNode {

	final private int port = 8888;

	private ScheduledExecutorService checkScheduler = Executors.newScheduledThreadPool(1);

	private double temp = 25;
	private double pres = 1013;

	public void startClient() {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			System.out.println("Error: cannot set up port " + this.port + "!");
			e.printStackTrace();
			System.out.println("Exiting...");
			System.exit(0);
		}
		System.out.println("serverSocket created on " + this.port);

		checkScheduler.scheduleAtFixedRate(new TempCheck(), 0, 20, TimeUnit.SECONDS);

		while (true) {
			try (Socket clientSocket = serverSocket.accept();
					ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

				ArrayList<Object> inArr = (ArrayList<Object>) in.readObject();
				System.out.println(inArr);

				ArrayList<Object> outArr = new ArrayList<Object>();
				outArr.add(MessageStatus.TEMPERATURE);
				outArr.add(new Double(temp));
				outArr.add(new Double(pres));
				out.writeObject(outArr);
				out.flush();

				switch ((MessageStatus) inArr.get(0)) {
					case NOTIFY:
						Process processN = (new ProcessBuilder(PushbulletListener.pythonPath, "-u",
								PushbulletListener.scriptFolder + "/set_numled.py",
								((Integer) inArr.get(1)).toString())).start();
						break;
					case DOORBELL:
						Process processD = (new ProcessBuilder(PushbulletListener.pythonPath, "-u",
								PushbulletListener.scriptFolder + "/blink.py")).start();
						try (InputStreamReader isr = new InputStreamReader(
								processD.getInputStream());
								BufferedReader br = new BufferedReader(isr)) {
							while (br.readLine() != null) {
							}
							Thread.sleep(1000);
							Process processDN = (new ProcessBuilder(PushbulletListener.pythonPath,
									"-u", PushbulletListener.scriptFolder + "/set_numled.py",
									((Integer) inArr.get(1)).toString())).start();
						} catch (IOException | InterruptedException e1) {
							e1.printStackTrace();
						}
						break;
					default:
						break;
				}

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[]) {
		DisplayNode d = new DisplayNode();
		d.startClient();
	}

	class TempCheck implements Runnable {

		@Override
		public void run() {
			try {
				Process process = (new ProcessBuilder(PushbulletListener.pythonPath, "-u",
						PushbulletListener.scriptFolder + "/get_temp.py")).start();
				InputStreamReader isr = new InputStreamReader(process.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				temp = Double.parseDouble(br.readLine());
				pres = Double.parseDouble(br.readLine());
				br.close();
				isr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// System.out.println("Temp: " + temp);
			// System.out.println("Pres: " + pres);
		}

	}
}
