package base;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileServerControl implements Runnable {
	private static Logger logger = LogManager.getLogger(FileServerControl.class.getName());

	// 连接池起始端口与终止端口
	private static int start_port = 8001;
	private static int end_port = 8002;
	private static HashMap<Integer, FileServerInfo> ThreadPool = new HashMap<>();
	private static boolean flag = false;
	private static int sleepTime = 5000;

	// 初始化连接池端口范围
	public static void setPort(int startport, int endport) {
		start_port = startport;
		end_port = endport;
	}

	public static void startFileServer() {
		flag = true;
	}

	public static void stopFileServer() {
		flag = false;

		// 安全事件，确定run函数进入do no thing状态
		try {
			Thread.sleep(2 * sleepTime);
		} catch (InterruptedException e) {
			logger.error("stopFileServer方法中sleep异常！！！");
		}

		int port = start_port;
		while (port <= end_port) {
			// fileserver类中的异常处理会重置为OFF状态，但其实也没有用了
			ThreadPool.get(port).fileserver.stopServer();
			logger.info("Shutdown Thread " + port);
			port++;
		}

	}

	// 带锁的方法
	public synchronized static void setFileServerStatus(int port, FileServerStatus status) {
		ThreadPool.get(port).fss = status;
	}

	// 查找空闲的服务器
	public synchronized static int findAvailbaleServer() {
		int port = start_port;
		while (port <= end_port) {
			if (ThreadPool.get(port).fss == FileServerStatus.AVAILABLE) {
				// 如果找到可用的，先设置该端口已占用，之后直接返回该端口号
				ThreadPool.get(port).fss = FileServerStatus.BUSY;
				return port;
			}

			port++;
		}
		// 否则返回-1
		return -1;
	}

	// 初始化文件服务
	public static void initFileServer() {
		int port = start_port;

		logger.info("Start init File server, from " + start_port + " to " + end_port);
		// 先清空之前的记录信息
		ThreadPool.clear();
		// 登记线程信息
		while (port <= end_port) {
			// 新建一个信息记录条目
			FileServerInfo fsi = new FileServerInfo();
			// 在线程池中登记该信息
			ThreadPool.put(port, fsi);
			// 记录端口号
			fsi.port = port;
			// 记录线程信息，稍后启动线程
			// fsi.fileserver = new FileServer(port);
			// fsi.thread = new Thread(fsi.fileserver);

			// 端口号往后移动
			port++;
		}

		logger.info("Init FileServer End.");
	}

	/**
	 * run函数的主要功能应该是检查各个线程的状态，在必要时候重启
	 */
	@Override
	public void run() {

		while (true) {
			// 如果是启动状态，则检查每一个连接
			if (flag) {
				int port = start_port;
				while (port <= end_port) {
					if (ThreadPool.get(port).fss == FileServerStatus.OFF) {
						logger.info("Start Thread " + port);
						FileServer fs = new FileServer(port);
						ThreadPool.get(port).fileserver =  fs;
						ThreadPool.get(port).thread = new Thread(fs);
						ThreadPool.get(port).thread.start();
					}

					port++;
				} // end inter-while
			} // end if flag
			else {
				// 什么事情也不做
			}

			// 休眠若干秒
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.error("run方法中sleep异常！！！");
			}
		} // end while

	}// end run

	// 测试代码
	public static void main(String[] args) throws Exception {
		new Thread(new FileServerControl()).start();

		setPort(8001, 8002);
		initFileServer();
		startFileServer();
		// 睡眠一段时间
		Thread.sleep(1000 * 10);
		stopFileServer();

		// 关闭服务之后，如果要再重启服务，则需要重新初始化
		Thread.sleep(1000 * 10);
		initFileServer();
		startFileServer();
	}

}
