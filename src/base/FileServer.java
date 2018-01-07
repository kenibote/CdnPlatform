package base;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileServer implements Runnable {
	/**
	 * 文件服务子线程，在创建该线程时候需要初始化端口号; 该子函数，首先由客户端提供一个文件名，之后向用户传递指定文件。
	 */
	private static Logger logger = LogManager.getLogger(FileServer.class.getName());

	private int port = 8001;
	// 用于异步关闭线程
	private ServerSocket ss = null;

	// 构造器
	public FileServer(int serverport) {
		this.port = serverport;
	}

	// 关闭线程
	// 关闭后会自动转入异常处理模块，之后会自动标记线程为关闭状态
	public void stopServer() {
		try {
			ss.close();
		} catch (IOException e) {
			logger.error("异步关闭FileServer发生错误！！！");
		}
	}

	@Override
	public void run() {
		Socket s = null;
		DataInputStream fis = null;
		// ----------------------------
		DataInputStream dis = null;
		DataOutputStream ps = null;
		String filePath = null;
		File fi = null;

		try {
			// ss被提前，用于异步关闭线程
			ss = new ServerSocket(port);
			// 如果启动成功,设置该为可用
			FileServerControl.setFileServerStatus(port, FileServerStatus.AVAILABLE);

			while (true) {
				// 建立socket链接
				s = ss.accept();
				logger.info("Port:" + port + ", Build Socket Link...");

				// 取得需要下载的文件名
				dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
				filePath = "D:\\" + dis.readUTF();
				fi = new File(filePath);
				logger.info("Port:" + port + ", File:" + filePath + ", File Length:" + (int) fi.length());

				// 获得文件输入流，socket输出流
				fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
				ps = new DataOutputStream(s.getOutputStream());

				// 传送文件
				ps.writeUTF(fi.getName());
				ps.flush();
				ps.writeLong((long) fi.length());
				ps.flush();

				int bufferSize = 8192;
				byte[] buf = new byte[bufferSize];

				while (true) {
					int read = 0;
					if (fis != null) {
						read = fis.read(buf);
					}

					if (read == -1) {
						break;
					}
					ps.write(buf, 0, read);
				}
				ps.flush();

				// 关闭文件流和socket链接
				fis.close();
				s.close();
				logger.info("Port:" + port + ", File Tran Completed.");
				// 重新登记该端口可用
				FileServerControl.setFileServerStatus(port, FileServerStatus.AVAILABLE);
			}

		} catch (IOException e) {
			logger.error("Port:" + port + " Closed!!!");
			// TODO 发生任何错误之后，使得该端口不可用，需要在FileServerControl里面进行相关处理
			// 目前设置为不可用，FileServerControl会尝试重启
			FileServerControl.setFileServerStatus(port, FileServerStatus.OFF);
		}

	}// end run

	// 测试代码
	public static void main(String[] args) {
		new Thread(new FileServer(8821)).start();
	}

}