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

public class LoadBalanceServer implements Runnable {
	/**
	 * 该线程也应该是一直运行
	 * 
	 */
	private static Logger logger = LogManager.getLogger(LoadBalanceServer.class.getName());

	// 服务器监听端口，默认8070
	private int server_port = 8070;
	private static HashMap<String, String> result = new HashMap<>();

	// 由该函数查找指定服务器
	private static void findServer(String ID) {
		/**
		 * TODO 此处有大量逻辑代码需要完善；
		 * 先查找本地知否有该内容，如果有则检查服务是否可用；
		 * 如果没有，则请求别的服务器；
		 * */
		// 如果服务不可用，port为-1；
		int port = FileServerControl.findAvailbaleServer();

		result.clear();
		result.put("RESULT", "SUCCESS");
		result.put("IP", "10.10.12.98");
		result.put("PORT", "" + port);
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
