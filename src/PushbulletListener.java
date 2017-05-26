import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

public class PushbulletListener {

	private static PushbulletListener pl = new PushbulletListener();
	final public static String pythonPath = "/usr/bin/python";
	final public static String scriptFolder = "/home/pi/NotiLink/scripts";
	private Process process;
	private InputStreamReader isr;
	private BufferedReader br;

	private PushbulletListener() {
		try {
			process = (new ProcessBuilder(pythonPath, "-u",
					scriptFolder + "/websocket_listener.py")).start();
			isr = new InputStreamReader(process.getInputStream());
			br = new BufferedReader(isr);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static PushbulletListener getInstance() {
		return pl;
	}

	public JSONObject getMessage() throws JSONException, IOException {
		return new JSONObject(br.readLine());
	}

	public static void main(String[] args) {
		PushbulletListener pl = PushbulletListener.getInstance();
		System.out.println("Output: ");
		try {
			while (true) {
				JSONObject obj = pl.getMessage();
				System.out.println(obj);
				if (obj.get("type").equals("nop")) {
					System.out.println("Tickle");
				} else if (obj.get("type").equals("tickle")) {
					try {
						Process process = (new ProcessBuilder(pythonPath, "-u",
								scriptFolder + "/get_push.py")).start();
						InputStreamReader isr = new InputStreamReader(process.getInputStream());
						BufferedReader br = new BufferedReader(isr);
						String line;
						while ((line = br.readLine()) != null) {
							System.out.println(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("We've got a message");
				} else if (obj.get("type").equals("push")) {
					System.out.println("We've got a notification");
				}
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}

}
