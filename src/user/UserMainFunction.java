package user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import base.FileServer;
import base.FileServerControl;

public class UserMainFunction {
	private static Logger logger = LogManager.getLogger(UserMainFunction.class.getName());

	public static void main(String[] args) {
		/**
		 * 启动user端的代码后，首先会启动后台监控线程，用于与monitor取得联系 这里首先有一个double
		 * check的过程，monitor要确认这个是user端的程序 之后，由monitor对该终端进行一些列初始化设置： 包括：USER#ID,
		 * 优先服务器地址
		 * 
		 */
		logger.info("User Simulator Start ...");

		UserBackTCP userbacktcp = new UserBackTCP(8091);
		Thread t = new Thread(userbacktcp);
		t.start();
		
		//-------------------实验结果下载服务-------------------------
		FileServerControl fc = new FileServerControl();
		new Thread(fc).start();
		FileServer.PathPix = "E:\\";
		
		FileServerControl.setPort(8092, 8092);
		FileServerControl.initFileServer();
		FileServerControl.startFileServer();
	}
}
