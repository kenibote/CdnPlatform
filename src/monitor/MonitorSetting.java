package monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import net.sf.json.JSONObject;

public class MonitorSetting {

	public static HashMap<Integer, String> local_server_info = new HashMap<>();
	public static HashMap<Integer, String> user_server_info = new HashMap<>();
	public static String original_server = "173.0.0.1";
	public static String monitor_ip = "10.10.12.00";

	{
		local_server_info.put(1, "10.10.12.101");
		local_server_info.put(2, "10.10.12.102");
		local_server_info.put(3, "10.10.12.103");

		user_server_info.put(1, "192.168.1.101");
		user_server_info.put(2, "192.168.1.102");
		user_server_info.put(3, "192.168.1.103");
	}

	public static String setLocalServer(int id) throws Exception {
		Socket socket = new Socket(local_server_info.get(id), 8091);

		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter write = new PrintWriter(socket.getOutputStream());

		HashMap<String, String> result = new HashMap<>();
		result.put("KEY", "LocalServer");
		result.put("TYPE", "TASK_003");
		result.put("ID", "LocalServer" + id);
		result.put("OriginalServerIP", original_server);
		result.put("LoadBalancePort", "8070");
		result.put("StartPort", "8001");
		result.put("EndPort", "8020");
		result.put("LocalServerNumber", "3");
		result.put("Neighbor1", local_server_info.get(1));
		result.put("Neighbor2", local_server_info.get(2));
		result.put("Neighbor3", local_server_info.get(3));

		JSONObject json = JSONObject.fromObject(result);
		write.println(json.toString());
		write.flush();

		String back = input.readLine();
		socket.close();

		return back;
	}

	public static String setOriginalServer() throws Exception {
		Socket socket = new Socket(original_server, 8090);

		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter write = new PrintWriter(socket.getOutputStream());

		HashMap<String, String> result = new HashMap<>();
		result.put("KEY", "OriginalServer");
		result.put("TYPE", "TASK_007");
		result.put("ID", "OriginalServer");
		result.put("OriginalServerIP", original_server);
		result.put("RemoteBalancePort", "8071");
		result.put("StartPort", "8001");
		result.put("EndPort", "8050");
		result.put("LocalServerNumber", "3");
		result.put("Neighbor1", local_server_info.get(1));
		result.put("Neighbor2", local_server_info.get(2));
		result.put("Neighbor3", local_server_info.get(3));

		JSONObject json = JSONObject.fromObject(result);
		write.println(json.toString());
		write.flush();

		String back = input.readLine();
		socket.close();

		return back;
	}

	public static String Usually(String IP, int port, String KEY, String task) throws Exception {
		Socket socket = new Socket(IP, port);

		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter write = new PrintWriter(socket.getOutputStream());

		HashMap<String, String> result = new HashMap<>();
		result.put("KEY", KEY);
		result.put("TYPE", task);

		JSONObject json = JSONObject.fromObject(result);
		write.println(json.toString());
		write.flush();

		String back = input.readLine();
		socket.close();

		return back;
	}

	public static String setUser(int id) throws Exception {
		Socket socket = new Socket(local_server_info.get(id), 8091);

		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter write = new PrintWriter(socket.getOutputStream());

		HashMap<String, String> result = new HashMap<>();
		result.put("KEY", "USER");
		result.put("TYPE", "TASK_002");
		result.put("ID", "User" + id);
		result.put("LocalServerIP", user_server_info.get(id));
		result.put("LocalServerPort", "8070");

		JSONObject json = JSONObject.fromObject(result);
		write.println(json.toString());
		write.flush();

		String back = input.readLine();
		socket.close();

		return back;
	}

	public static String UserLoadTask(int id) throws Exception {
		Socket socket = new Socket(local_server_info.get(id), 8091);

		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter write = new PrintWriter(socket.getOutputStream());

		HashMap<String, String> result = new HashMap<>();
		result.put("KEY", "USER");
		result.put("TYPE", "TASK_011");
		result.put("FileName", "loadtask-" + id);
		result.put("MonitorIP", monitor_ip);
		result.put("MonitorPort", "8095");

		JSONObject json = JSONObject.fromObject(result);
		write.println(json.toString());
		write.flush();

		String back = input.readLine();
		socket.close();

		return back;
	}

}
