package localserver;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalServerMainFunction {
	private static Logger logger = LogManager.getLogger(LocalServerMainFunction.class.getName());

	public static void main(String[] args) {
		/**
		 * 启动localserver端的代码后，首先会启动后台监控线程，用于与monitor取得联系 这里首先有一个double
		 * check的过程，monitor要确认这个是localserver端的程序 之后，由monitor对该终端进行一些列初始化设置：
		 * 包括：LocalServer#ID,源服务器地址
		 * 
		 */

		logger.info("LocalServer Start ...");
		LocalServerBackTCP localserverbacktcp = new LocalServerBackTCP(8060);
		Thread t = new Thread(localserverbacktcp);
		t.start();
		
		// --------------------------------TEST----------------------------------
		ArrayList<String> localserver1 = new ArrayList<>();
		localserver1.add("test11.JPG");
		localserver1.add("test12.JPG");

		ArrayList<String> localserver2 = new ArrayList<>();
		localserver2.add("test21.JPG");
		localserver2.add("test22.JPG");

		LocalServerPublicSetting.ContentMap.put("LocalServer1", localserver1);
		LocalServerPublicSetting.ContentMap.put("LocalServer2", localserver2);
	}

}
