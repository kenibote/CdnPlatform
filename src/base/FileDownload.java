package base;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JSONObject;

public class FileDownload implements Runnable {
	private static Logger logger = LogManager.getLogger(FileDownload.class.getName());

	private ClientSocket cs = null;

	// 基础信息
	private String ip = "10.10.12.82";
	private int port = 8001;
	private String sendMessage = "Group1.jpg";
	private String savePath = "E:\\";
	private String DownloadModel = "Real";
	// 默认，用于向上层返回状态
	private int ID = 0;
	private long start_time = 0;
	private long end_time = 0;
	private TaskInfo task = null;

	/**
	 * run()函数的一个运行思路：
	 * 先向localserver确认下载地址，此处可能会修改IP:PORT这2个参数，需要在task执行记录里面登记，此处也要记录时间；
	 * 之后调用download函数执行下载，记录时间； TODO 需要补充该内容
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		// TODO 此处还有大量代码需要完善
		JSONObject json = null;
		boolean flag = false;

		try {
			// 与本地服务器建立连接
			start_time = System.currentTimeMillis();
			Socket socket = new Socket(ip, port);
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter write = new PrintWriter(socket.getOutputStream());

			// 发送任务请求
			write.println("{\"ID\":\"" + sendMessage + "\"}");
			write.flush();

			// 接收重定向信息，并修改本地信息
			String command = input.readLine();
			// 关闭连接
			socket.close();
			// ---------------------------------------------------------------------------------------

			json = JSONObject.fromObject(command);
			// 如果成功
			if ("SUCCESS".equals(json.getString("RESULT"))) {
				ip = (String) json.getOrDefault("IP", "NULL");
				port = Integer.parseInt((String) json.getOrDefault("PORT", "0"));
				flag = true;
			} else {
				// 如果失败
				String[] redirect = json.getString("REDIRECT").split("-");
				// 逐个查找重定向列表
				for (String server : redirect) {
					// 建立连接
					socket = new Socket(server, 8071);
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					write = new PrintWriter(socket.getOutputStream());
					// 发送任务请求
					write.println("{\"ID\":\"" + sendMessage + "\"}");
					write.flush();
					// 收到回复，即时关闭连接
					command = input.readLine();
					socket.close();

					// 如果有空闲资源
					json = JSONObject.fromObject(command);
					if ("SUCCESS".equals(json.getString("RESULT"))) {
						ip = (String) json.getOrDefault("IP", "NULL");
						port = Integer.parseInt((String) json.getOrDefault("PORT", "0"));
						flag = true;
						// 跳出for循环
						break;
					}
				} // end for
			} // end else

			end_time = System.currentTimeMillis();

			// 记录信息
			if (task != null) {
				task.task_server_ip = ip;
				task.task_server_port = port;
				task.redirect_time = end_time - start_time;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ---------------------------------------------------------------------------------------

		if (flag) {
			// 记录起始时间
			start_time = System.currentTimeMillis();
			// 开始下载
			try {
				if (ip.startsWith("192")) {
					Thread.sleep(125);
				}
				if (ip.startsWith("10")) {
					Thread.sleep(375);
				}
				if (ip.startsWith("172")) {
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			boolean result = download();
			// 记录结束时间
			end_time = System.currentTimeMillis();
			// 记录下载时间与下载结果
			logger.info(ID + ":" + (end_time - start_time) + ":" + result);
			if (task != null) {
				task.download_time = end_time - start_time;
				task.task_flag = true;
			}
		}
	}

	// 通用下载函数
	public boolean download() {
		try {
			if (createConnection()) {
				sendMessage();
				if ("Real".equals(DownloadModel))
					getMessage();
				else
					getMessageImage();
			}
			// 如果下载成功
			return true;

		} catch (Exception ex) {
			// 如果下载失败
			return false;
		}
	}

	// 初始化下载信息
	public FileDownload(String localserver, int server_port, String targetfile, int taskid) {
		this.ip = localserver;
		this.port = server_port;
		this.sendMessage = targetfile;
		this.ID = taskid;
	}

	public FileDownload(TaskInfo taskinfo) {
		this.task = taskinfo;

		this.ip = taskinfo.task_from;
		this.port = taskinfo.load_balance_port;
		this.ID = taskinfo.task_id;
		this.sendMessage = taskinfo.file_name;

		this.DownloadModel = "Image";
	}

	private boolean createConnection() {
		cs = new ClientSocket(ip, port);
		try {
			cs.CreateConnection();
			logger.info("连接服务器成功!" + ip + ":" + port + "|" + sendMessage);
			return true;
		} catch (Exception e) {
			logger.error("连接服务器失败!" + ip + ":" + port + "|" + sendMessage);
			return false;
		}
	}

	private void sendMessage() {
		if (cs == null)
			return;
		try {
			cs.sendMessage(sendMessage);
		} catch (Exception e) {
			logger.error("发送消息失败!");
		}
	}

	private void getMessage() {
		if (cs == null)
			return;
		DataInputStream inputStream = null;
		try {
			inputStream = cs.getMessageStream();
		} catch (Exception e) {
			logger.error("接收消息缓存错误");
			return;
		}

		try {
			int bufferSize = 8192;
			byte[] buf = new byte[bufferSize];

			savePath += inputStream.readUTF();
			DataOutputStream fileOut = new DataOutputStream(
					new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
			// 记录文件长度
			inputStream.readLong();

			while (true) {
				int read = 0;
				if (inputStream != null) {
					read = inputStream.read(buf);
				}

				if (read == -1) {
					break;
				}

				fileOut.write(buf, 0, read);
			}
			fileOut.close();

			logger.info("接收完成，文件存为" + savePath);
		} catch (Exception e) {
			logger.error("接收消息错误!!!");
			return;
		}
	}

	private void getMessageImage() {
		if (cs == null)
			return;
		DataInputStream inputStream = null;
		try {
			inputStream = cs.getMessageStream();
		} catch (Exception e) {
			logger.error("接收消息缓存错误");
			return;
		}

		try {
			int bufferSize = 8192;
			byte[] buf = new byte[bufferSize];

			savePath += inputStream.readUTF();
			// 记录文件长度
			inputStream.readLong();

			while (true) {
				int read = 0;
				if (inputStream != null) {
					read = inputStream.read(buf);
				}

				if (read == -1) {
					break;
				}

			}

			logger.info("接收完成，文件存为" + savePath);
		} catch (Exception e) {
			logger.error("接收消息错误!!!");
			return;
		}
	}

}