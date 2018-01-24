package monitor;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReMapServer implements Runnable {

	private static Logger logger = LogManager.getLogger(ReMapServer.class.getName());

	public static long autoReMapTime = 60_000;
	public static boolean flag = false;
	
	public static HashMap<String,Double> arrival_rate = new HashMap<>();
	

	@Override
	public void run() {
		logger.info("ReMap 启动");

		while (flag) {
			try {
				Thread.sleep(autoReMapTime);
				// 回收点击率信息+到达率信息
				// 整合之后发给AI模块
				// ReMap操作
				
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		logger.info("ReMap 关闭");

	}

}
