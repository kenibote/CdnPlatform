package originalserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JSONObject;

public class OriginalServerBackTCP implements Runnable {
	/**
	 * 
	 * */
	private static Logger logger = LogManager.getLogger(OriginalServerBackTCP.class.getName());

	// 服务器监听端口，默认8060
	private int server_port = 8060;

	public OriginalServerBackTCP(int port) {
		this.server_port = port;
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
		String key = null;
		HashMap<String, String> result = new HashMap<>();
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
				// 将该信息写入到公共空间中
				OriginalServerPublicSetting.MonitorSocketInput = input;
				OriginalServerPublicSetting.MonitorSocketWrite = write;

				getCommand = null;
				// 连接建立方可以主动关闭界面
				while (!socket.isClosed() && (getCommand = input.readLine()) != null) {
					// 此时已经获得了输入信息,记录信息
					logger.info(getCommand);

					// 或许鉴权信息,增加安全措施
					key = (String) JSONObject.fromObject(getCommand).getOrDefault("KEY", "NULL");
					if (OriginalServerPublicSetting.Key.equals(key)) {
						// 正确，则进入命令执行函数
						OriginalServerControl.Router(getCommand);
					} else {
						// 否则鉴权错误，主动关闭socket
						result.clear();
						result.put("CODE", "ERROR_001");
						result.put("DEVICE", "OriginalServer");
						result.put("ID", OriginalServerPublicSetting.ID);
						json = JSONObject.fromObject(result);

						logger.info("Key Wrong！！！");
						write.println(json.toString());
						write.flush();
						socket.close();
					}

				}

				// 当连接断开,清空公共空间中的信息，此处来讲应该是monitor端正常断开链接
				// TODO 需要检查是否有什么漏掉的地方
				logger.info("Clearing Something...");
				OriginalServerPublicSetting.MonitorSocketInput = null;
				OriginalServerPublicSetting.MonitorSocketWrite = null;

			} // end of while

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
	}// end run

}
