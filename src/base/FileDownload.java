package base;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileDownload implements Runnable {
	private static Logger logger = LogManager.getLogger(FileDownload.class.getName());

	private ClientSocket cs = null;

	// 基础信息
	private String ip = "10.10.12.82";
	private int port = 8001;
	private String sendMessage = "Group1.jpg";
	private String savePath = "E:\\";
	// 默认，用于向上层返回状态
	private int ID = 0;
	private long start_time = 0;
	private long end_time = 0;

	/**
	 * run()函数的一个运行思路：
	 * 先向localserver确认下载地址，此处可能会修改IP:PORT这2个参数，需要在task执行记录里面登记，此处也要记录时间；
	 * 之后调用download函数执行下载，记录时间； TODO 需要补充该内容
	 */
	@Override
	public void run() {

		// 记录起始时间
		start_time = System.currentTimeMillis();
		// 开始下载
		boolean result = download();
		// 记录结束时间
		end_time = System.currentTimeMillis();
		// 记录下载时间与下载结果
		logger.info(ID + ":" + (end_time - start_time));
		logger.info(result);
	}

	// 通用下载函数
	public boolean download() {
		try {
			if (createConnection()) {
				sendMessage();
				getMessage();
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

}