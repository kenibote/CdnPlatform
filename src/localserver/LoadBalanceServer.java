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
	private static void findServer(String ID) {
		result.clear();

		// 如果服务不可用，port为-1；
		int port = -1;
		// 先检查本地是否有该内容
		if (LocalServerPublicSetting.DoContentMap("FIND", LocalServerPublicSetting.ID, ID)) {
			if ((port = FileServerControl.findAvailbaleServer()) != -1) {
				// 如果本地有该内容,且本地可以服务
				result.put("RESULT", "SUCCESS");
				result.put("IP", "192.168.1.101");
				result.put("PORT", "" + port);
				return;
			}
		}

		String redirect = "";
		// 如果由于某种原因，本地无法提供服务；
		Iterator<String> it = LocalServerPublicSetting.Neighbor.keySet().iterator();
		// 逐个查找远端服务器
		while (it.hasNext()) {
			String server = it.next();
			// 如果目标服务器有该内容，且不是自己
			if (LocalServerPublicSetting.DoContentMap("FIND", server, ID)
					&& (!server.equals(LocalServerPublicSetting.ID))) {
				// 获取目标服务器地址
				redirect = redirect + LocalServerPublicSetting.Neighbor.get(server) + "-";
			} // end if
		} // end while
		redirect = redirect + LocalServerPublicSetting.OriginalServerIP;
		result.put("RESULT", "FAIL");
		result.put("REDIRECT", redirect);

	}

	// 统计服务功能
	public static void Static(String id) {
		// 总计数器加1
		LocalServerPublicSetting.total_arrival++;

		// 每个内容的计数器加1
		int add1 = LocalServerPublicSetting.content_count.get(id) + 1;
		LocalServerPublicSetting.content_count.put(id, add1);
		
		// 对每一个内容执行sanjay操作 (已经优化)
		double like_log = LocalServerPublicSetting.content_live_like.get(id);
		like_log = (like_log * LocalServerPublicSetting.Content_N + 1)
				/ (LocalServerPublicSetting.Content_N + 1);
		for (String key : LocalServerPublicSetting.content_live_like.keySet()) {
			double like = LocalServerPublicSetting.content_live_like.get(key);
			like = (like * LocalServerPublicSetting.Content_N) / (LocalServerPublicSetting.Content_N + 1);
			LocalServerPublicSetting.content_live_like.put(key, like);
		}
		// 对指定内容执行另一个sanjay操作
		LocalServerPublicSetting.content_live_like.put(id, like_log);
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

					// 如果开启了统计服务功能
					if (LocalServerPublicSetting.localstaticfunction) {
						Static(id);
					}

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
