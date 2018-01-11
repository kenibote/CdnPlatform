package localserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import base.FileServerControl;
import net.sf.json.JSONObject;

public class LoadBalanceServer implements Runnable {
	/**
	 * 该线程也应该是一直运行
	 * 
	 */
	private static Logger logger = LogManager.getLogger(LoadBalanceServer.class.getName());

	// 服务器监听端口，默认8070
	private static int server_port = LocalServerPublicSetting.LoadBalancePort;
	private static HashMap<String, String> result = new HashMap<>();

	// 由该函数查找指定服务器
	@SuppressWarnings("unchecked")
	private static void findServer(String ID) {
		// 如果服务不可用，port为-1；
		Socket socket = null;
		BufferedReader input = null;
		PrintWriter write = null;
		JSONObject json = null;

		result.clear();
		int port = -1;
		// 先检查本地是否有该内容
		System.out.println("先检查本地是否有该内容？");
		if (LocalServerPublicSetting.DoContentMap("FIND", LocalServerPublicSetting.ID, ID)) {
			if ((port = FileServerControl.findAvailbaleServer()) != -1) {
				// 如果本地有该内容,且本地可以服务
				result.put("RESULT", "SUCCESS");
				result.put("IP", LocalServerPublicSetting.Neighbor.get(LocalServerPublicSetting.ID));
				result.put("PORT", "" + port);
				
				System.out.println("本地拥有该内容。");
				return;
			}
		}

		// 如果由于某种原因，本地无法提供服务；
		System.out.println("开始检查邻居服务器是否有该内容？");
		Iterator<String> it = LocalServerPublicSetting.Neighbor.keySet().iterator();
		// 逐个查找远端服务器
		while (it.hasNext()) {
			String server = it.next();
			if (LocalServerPublicSetting.DoContentMap("FIND", server, ID)) {
				System.out.println("在该服务器发现存在该内容："+server);
				// 尝试连接远端服务器
				try {
					socket = new Socket(LocalServerPublicSetting.Neighbor.get(server), server_port + 1);
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					write = new PrintWriter(socket.getOutputStream());

					write.println("{\"ID\":\"" + ID + "\"}");
					write.flush();

					String command = input.readLine();
					// 记得关闭socket端口
					socket.close();

					json = JSONObject.fromObject(command);
					if ("SUCCESS".equals((String) json.getOrDefault("RESULT", "NULL"))) {
						// 如果找到
						result.put("RESULT", "SUCCESS");
						result.put("IP", (String) json.getOrDefault("IP", "NULL"));
						result.put("PORT", (String) json.getOrDefault("PORT", "-1"));
						System.out.println("确定该服务器存在该内容。"+server);
						return;
					}

				} catch (Exception e) {

				} // end try
			} // end if
		} // end while

		// 最后，请求源服务器的帮助
		try {
			socket = new Socket(LocalServerPublicSetting.OriginalServerIP, server_port + 1);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			write = new PrintWriter(socket.getOutputStream());

			write.println("{\"ID\":\"" + ID + "\"}");
			write.flush();

			String command = input.readLine();
			// 记得关闭socket端口
			socket.close();

			json = JSONObject.fromObject(command);
			if ("SUCCESS".equals((String) json.getOrDefault("RESULT", "NULL"))) {
				// 如果找到
				result.put("RESULT", "SUCCESS");
				result.put("IP", (String) json.getOrDefault("IP", "NULL"));
				result.put("PORT", (String) json.getOrDefault("PORT", "-1"));
			} else {
				result.put("RESULT", "FAIL");
			}

			return;
		} catch (Exception e) {

		} // end try

	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		// 将这些声明拿到上面，防止大量重出创建产生内存压力
		Socket socket = null;
		BufferedReader input = null;
		PrintWriter write = null;
		String getCommand = null;
		JSONObject json = null;

		try {
			serverSocket = new ServerSocket(server_port);

			while (true) {
				logger.info("Start Listerning ... Port:" + server_port);

				// 当有连接接入的时候,接收连接
				socket = serverSocket.accept();
				logger.info("Connect in...Remote:" + socket.getRemoteSocketAddress());

				// 获取socket写入与写出句柄
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				write = new PrintWriter(socket.getOutputStream());

				getCommand = null;
				// 连接建立方可以主动关闭界面
				while (!socket.isClosed() && (getCommand = input.readLine()) != null) {
					// 此时已经获得了输入信息,记录信息
					logger.info(getCommand);
					// 获取ID
					String id = (String) JSONObject.fromObject(getCommand).getOrDefault("ID", "NULL");
					// 由该函数查找指定服务器
					findServer(id);

					// 返回结果
					json = JSONObject.fromObject(result);
					write.println(json.toString());
					write.flush();

					// 主动关闭连接?
					// socket.close();
				}

			}
		} catch (IOException e) {
			// 此处发生异常，代表socket服务无法启动，或工作过程中发生异常；程序即将关闭
			// TODO 检查程序异常关闭后有什么需要保存的？
			e.printStackTrace();
		} finally {
			// 如论是否发生异常，从操作系统层面释放资源，均尝试关闭连接
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} // end of finally

	}

}
