package localserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalServerStatic implements Runnable {
	private static Logger logger = LogManager.getLogger(LocalServerStatic.class.getName());

	@Override
	public void run() {
		logger.info("Static 线程启动！！！");
		
		int point = 0;
		// 当服务是启动的时候,当服务结束之后，该线程自动结束
		while (LocalServerPublicSetting.localserverflag) {

			// 休息指定间隔
			try {
				Thread.sleep(LocalServerPublicSetting.time_period);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			point++;
			// 统计该本地端总到达率
			LocalServerPublicSetting.total_arrival_rate.put(point, LocalServerPublicSetting.total_arrival);

		} // end of while

		logger.info("Static 线程结束！！！");

	}

}
