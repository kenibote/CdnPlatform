package originalserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OriginalServerMainFunction {
	private static Logger logger = LogManager.getLogger(OriginalServerMainFunction.class.getName());

	public static void main(String[] args) {
		/**
		 * 启动originalserver端的代码后，首先会启动后台监控线程，用于与monitor取得联系 这里首先有一个double
		 * check的过程，monitor要确认这个是originalserver端的程序 之后，由monitor对该终端进行一些列初始化设置
		 */

		logger.info("OriginalServer Start ...");
		OriginalServerBackTCP originalserverbacktcp = new OriginalServerBackTCP(8090);
		Thread t = new Thread(originalserverbacktcp);
		t.start();
	}
}
