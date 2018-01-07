package base;

import java.net.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class ClientSocket {
	private static Logger logger = LogManager.getLogger(ClientSocket.class.getName());
	
	private String ip;
	private int port;

	private Socket socket = null;
	DataOutputStream out = null;
	DataInputStream getMessageStream = null;

	public ClientSocket(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	/**
	 * 创建socket连接
	 */
	public void CreateConnection() throws Exception {
		try {
			socket = new Socket(ip, port);
		} catch (Exception e) {
			e.printStackTrace();
			if (socket != null)
				socket.close();
			throw e;
		} finally {
		}
	}

	public void sendMessage(String sendMessage) throws Exception {
		try {
			out = new DataOutputStream(socket.getOutputStream());

			out.writeUTF(sendMessage);
			out.flush();

		} catch (Exception e) {
			e.printStackTrace();
			if (out != null)
				out.close();
			throw e;
		} finally {
		}
	}

	public DataInputStream getMessageStream() throws Exception {
		try {
			getMessageStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			return getMessageStream;
		} catch (Exception e) {
			e.printStackTrace();
			if (getMessageStream != null)
				getMessageStream.close();
			throw e;
		} finally {
		}
	}

	public void shutDownConnection() {
		try {
			if (out != null)
				out.close();
			if (getMessageStream != null)
				getMessageStream.close();
			if (socket != null)
				socket.close();
		} catch (Exception e) {
			logger.error("Socket关闭异常！！！");
		}
	}
}