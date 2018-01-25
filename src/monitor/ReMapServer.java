package monitor;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JSONObject;

public class ReMapServer implements Runnable {

	private static Logger logger = LogManager.getLogger(ReMapServer.class.getName());

	public static long autoReMapTime = 60_000;
	public static boolean flag = false;
	private static JSONObject json = null;

	public static HashMap<String, Double> arrival_rate = new HashMap<>();
	public static HashMap<String, HashMap<String, ArrayList<Integer>>> history = new HashMap<>();

	@Override
	public void run() {
		logger.info("ReMap 启动");

		// TODO 初始化载入历史数据

		while (flag) {
			try {
				Thread.sleep(autoReMapTime);
				// 回收点击率信息+到达率信息
				// 整合之后发给AI模块
				// ReMap操作
				getInfoFromLocalServer();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		logger.info("ReMap 关闭");

	}

	public static void getInfoFromLocalServer() {
		for (int i = 1; i <= 3; i++) {
			String back = null;
			try {
				back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090, "LocalServer", "TASK_018");
				json = JSONObject.fromObject(back);

				// 更新到达率
				double rate = json.getDouble("ArrivalRate");
				arrival_rate.put("LocalServer" + i, rate);

				// 更新点击率数据
				int count = json.getInt("ContentNumber");
				HashMap<String, ArrayList<Integer>> server = history.get("LocalServer" + i);
				for (int c = 1; c <= count; c++) {
					ArrayList<Integer> content = server.get("C" + c);
					int new_value = json.getInt("C" + c);
					content.add(new_value);
				}

			} catch (Exception e1) {
				System.out.println("Get info from LocalServer Fail !!!");
			}
		}
	}

}
