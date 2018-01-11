package localserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import base.FileServerControl;
import net.sf.json.JSONObject;

public class RemoteBalanceServer implements Runnable {
	/**
	 * 该线程也应该是一直运行
	 * 
	 */
	private static Logger logger = LogManager.getLogger(RemoteBalanceServer.class.getName());

	// 服务器监听端口，默认8071
	private int server_port = LocalServerPublicSetting.LoadBalancePort + 1;
	private static HashMap<String, String> result = new HashMap<>();

	// 由该函数查找指定服务器
	private static void findServerForRemote(String ID) {
		System.out.println("接受到远端请求，检查该内容现在是否可用？");
		
		result.clear();
		// 如果服务不可用，port为-1；
		int port = FileServerControl.findAvailbaleServer();

		if (port != -1) {
			result.put("RESULT", "SUCCESS");
			result.put("IP", LocalServerPublicSetting.Neighbor.get(LocalServerPublicSetting.ID));
			result.put("PORT", "" + port);
			System.out.println("目前服务器可用。");
		} else {
			result.put("RESULT", "FAIL");
		}
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
				logger.info("Start RemoteBalanceServer ... Port:" + server_port);

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
					findServerForRemote(id);

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
