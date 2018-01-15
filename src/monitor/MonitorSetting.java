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
	public static String monitor_ip = "10.10.12.136";

	{
		local_server_info.put(1, "10.10.12.92");
		local_server_info.put(2, "10.10.12.100");
		local_server_info.put(3, "10.10.12.29");

		user_server_info.put(1, "192.168.1.92");
		user_server_info.put(2, "192.168.1.100");
		user_server_info.put(3, "192.168.1.29");
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

	public static String SetLocalServerMap(int id) throws Exception {
		Socket socket = new Socket(local_server_info.get(id), 8090);

		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter write = new PrintWriter(socket.getOutputStream());

		HashMap<String, String> result = new HashMap<>();
		result.put("KEY", "LocalServer");
		result.put("TYPE", "TASK_014");
		result.put("LocalServer1",
				"C1-C2-C3-C4-C5-C6-C7-C8-C9-C10-C11-C12-C13-C14-C15-C16-C17-C18-C19-C20-C23-C24-C27-C32-C34-C37-C39-C44-C45-C50-C52-C55-C57-C62-C65-C66-C70-C73-C75-C80-C83-C84-C87-C92-C95-C96-C100-C103-C106-C109");
		result.put("LocalServer2",
				"C1-C2-C3-C4-C5-C6-C7-C8-C9-C10-C11-C12-C13-C14-C15-C16-C17-C18-C19-C20-C22-C25-C28-C31-C33-C38-C40-C43-C46-C49-C51-C56-C59-C60-C63-C68-C71-C72-C76-C79-C81-C86-C89-C90-C93-C98-C101-C102-C105-C110");
		result.put("LocalServer3",
				"C1-C2-C3-C4-C5-C6-C7-C8-C9-C10-C11-C12-C13-C14-C15-C16-C17-C18-C19-C20-C21-C26-C29-C30-C35-C36-C41-C42-C47-C48-C53-C54-C58-C61-C64-C67-C69-C74-C77-C78-C82-C85-C88-C91-C94-C97-C99-C104-C107-C108");

		JSONObject json = JSONObject.fromObject(result);
		write.println(json.toString());
		write.flush();

		String back = input.readLine();
		socket.close();

		return back;
	}

}
