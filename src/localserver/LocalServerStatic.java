package localserver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalServerStatic implements Runnable {
	private static Logger logger = LogManager.getLogger(LocalServerStatic.class.getName());
	private static int time_flag = 1;

	public static double Average_ArrivalRate() {
		int now = LocalServerPublicSetting.total_arrival_rate.size();
		double sum = 0;
		for (int i = time_flag; i <= now; i++) {
			sum = sum + LocalServerPublicSetting.total_arrival_rate.get(i);
		}
		sum = sum / (now - time_flag);
		time_flag = now;

		return sum;
	}

	@Override
	public void run() {
		logger.info("Static 线程启动！！！");

		int point = 0;
		int pre = LocalServerPublicSetting.total_arrival;
		int rate = 0;
		// 当服务是启动的时候,当服务结束之后，该线程自动结束
		while (LocalServerPublicSetting.localstaticfunction) {

			// 休息指定间隔
			try {
				Thread.sleep(LocalServerPublicSetting.time_period);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// 统计该本地端到达率
			point++;
			rate = LocalServerPublicSetting.total_arrival - pre;
			pre = LocalServerPublicSetting.total_arrival;
			LocalServerPublicSetting.total_arrival_rate.put(point, rate);

		} // end of while

		logger.info("Static 线程结束！！！");

		// ----------------------------------------------------------------------
		// 输出统计结果？
		String saveFile = "D:\\Content\\" + "StaticsArrivalRate" + ".csv";

		try {
			FileWriter fout = new FileWriter(saveFile);

			Iterator<Integer> it = LocalServerPublicSetting.total_arrival_rate.keySet().iterator();
			while (it.hasNext()) {
				int key = (int) it.next();
				int value = LocalServerPublicSetting.total_arrival_rate.get(key);

				String output = key + "," + value + ",\r\n";
				fout.write(output);
			}

			// 关闭文件
			fout.flush();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// -------------------------输出另一个统计结果----------------------------------
		saveFile = "D:\\Content\\" + "StaticsLiveLike" + ".csv";
		try {
			FileWriter fout = new FileWriter(saveFile);

			Iterator<String> it = LocalServerPublicSetting.content_live_like.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				double value = LocalServerPublicSetting.content_live_like.get(key);

				String output = key + "," + value + ",\r\n";
				fout.write(output);
			}

			// 关闭文件
			fout.flush();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
