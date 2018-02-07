package monitor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import data.PopularityGenerate;
import net.sf.json.JSONObject;

public class ReMapServer implements Runnable {

	private static Logger logger = LogManager.getLogger(ReMapServer.class.getName());

	public static long autoReMapTime = 60_000;
	public static boolean flag = false;
	private static JSONObject json = null;

	public static HashMap<String, Double> arrival_rate = new HashMap<>();
	public static HashMap<String, HashMap<String, ArrayList<Integer>>> history = new HashMap<>();
	public static HashMap<String, TreeMap<Double, String>> predict_tree = new HashMap<>();
	public static HashMap<String, HashMap<String, Double>> predict_hash = new HashMap<>();

	@Override
	public void run() {
		logger.info("ReMap 启动");

		// TODO 初始化载入历史数据
		LoadHistory();

		while (flag) {
			try {
				Thread.sleep(autoReMapTime);
				logger.info("ReMaping...");
				// 回收点击率信息+到达率信息
				getInfoFromLocalServer();
				// 整合之后发给AI模块
				// ReMap操作
				ReMap_1(20, 50);
				// 下发新的Map表
				SendMap();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		ForTest();
		logger.info("ReMap 关闭");

	}

	public static void ReMap_1(int com, int total) {
		HashMap<String, ArrayList<String>> content_map = new HashMap<>();
		HashMap<String, Double> pressure = new HashMap<>();
		TreeMap<Double, String> global = new TreeMap<>();

		// 初始化存储内容
		for (int i = 1; i <= 3; i++) {
			ArrayList<String> local_map = new ArrayList<>();
			content_map.put("LocalServer" + i, local_map);

			pressure.put("LocalServer" + i, 0.0);
		}

		// 先每个本地服务器优先放置
		for (int i = 1; i <= 3; i++) {
			TreeMap<Double, String> local = predict_tree.get("LocalServer" + i);
			ArrayList<String> local_content_map = content_map.get("LocalServer" + i);

			Set<Double> set = local.descendingKeySet();
			for (double e : set) {
				// 存入内容
				local_content_map.add(local.get(e));
				// 计算压力
				double val = pressure.get("LocalServer" + i);
				val = val + e * arrival_rate.get("LocalServer" + i);
				pressure.put("LocalServer" + i, val);
				// 判断是否终止
				if (local_content_map.size() == com)
					break;
			}

		}

		// 计算全局喜好程度
		for (int c = 1; c <= 300; c++) {
			double val = 0;
			for (int j = 1; j <= 3; j++)
				val = val + arrival_rate.get("LocalServer" + j) * predict_hash.get("LocalServer" + j).get("C" + c);

			global.put(val, "C" + c);
		}

		// 分布式存储
		Iterator<Double> g_set = global.descendingKeySet().iterator();
		while (content_map.get("LocalServer1").size() < total || content_map.get("LocalServer2").size() < total
				|| content_map.get("LocalServer3").size() < total) {
			// 如果有本地服务器还有存储空间，先逐个取出内容
			double val = g_set.next();
			String id = global.get(val);
			// 如果有任何一个服务器已经存储过该内容
			if (content_map.get("LocalServer1").contains(id) || content_map.get("LocalServer2").contains(id)
					|| content_map.get("LocalServer3").contains(id)) {
				continue;
			}

			// 生成候选节点
			ArrayList<String> list = new ArrayList<>();
			HashMap<String, Double> candi = new HashMap<>();
			double max_candi = 0;
			for (int i = 1; i <= 3; i++) {
				if (content_map.get("LocalServer" + i).size() < total) {
					list.add("LocalServer" + i);

					double pre_count = arrival_rate.get("LocalServer" + i)
							* predict_hash.get("LocalServer" + i).get(id);
					candi.put("LocalServer" + i, pre_count);

					if (pre_count > max_candi)
						max_candi = pre_count;
				}
			}

			// 判断是否有多个
			Set<String> set_candi = candi.keySet();
			list.clear();
			for (String e : set_candi) {
				if (max_candi == candi.get(e)) {
					list.add(e);
				}
			}

			// 如果有多个，则比较压力
			if (list.size() > 1) {
				candi.clear();
				TreeMap<Double, String> pres_tree = new TreeMap<>();
				for (String e : list) {
					pres_tree.put(pressure.get(e), e);
				}

				list.clear();
				// 找到压力最小的
				list.add(pres_tree.firstEntry().getValue());
			}

			// 存入map，更新压力
			String target = list.get(0);
			content_map.get(target).add(id);
			double new_p = pressure.get(target) + arrival_rate.get(target) * predict_hash.get(target).get(id);
			pressure.put(target, new_p);

		}

		// 写入文件
		String saveFile = "D:\\Content\\Map.txt";

		try {
			FileWriter fout = new FileWriter(saveFile);

			for (int i = 1; i <= 3; i++) {
				fout.write("LocalServer" + i + ":");

				ArrayList<String> local_map = content_map.get("LocalServer" + i);
				Iterator<String> it = local_map.iterator();
				while (it.hasNext()) {
					fout.write(it.next());

					if (it.hasNext())
						fout.write("-");
				}

				fout.write("\r\n");
			}

			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 输出每个服务器，每个内容的历史数据
	 */
	public static void ForTest() {
		for (int i = 1; i <= 3; i++) {
			String saveFile = "D:\\Content\\" + "ReMapCounterLocalServer" + i + ".csv";

			try {
				FileWriter fout = new FileWriter(saveFile);

				HashMap<String, ArrayList<Integer>> server = history.get("LocalServer" + i);
				for (int c = 1; c <= 300; c++) {
					ArrayList<Integer> content = server.get("C" + c);
					for (int e : content)
						fout.write(e + ",");

					fout.write("\r\n");
				}

				fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void LoadHistory() {

		// 先初始化History
		for (int i = 1; i <= 3; i++) {
			HashMap<String, ArrayList<Integer>> LocalServer = new HashMap<>();

			for (int c = 1; c <= 300; c++) {
				ArrayList<Integer> content = new ArrayList<>();
				LocalServer.put("C" + c, content);
			}

			history.put("LocalServer" + i, LocalServer);
		}

		// TODO 载入数据

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

	public static void SendMap() {
		for (int i = 1; i <= 3; i++) {
			String back = null;
			try {
				back = MonitorSetting.SetLocalServerMap(i);
			} catch (Exception e1) {
				System.out.println("Local Server UpdataMap Fail !!!");
			}
			System.out.println(back);
		}
	}

	public static void main(String[] args) {
		PopularityGenerate.generateRaw(0.88);

		for (int i = 1; i <= 3; i++) {
			TreeMap<Double, String> local_tree = new TreeMap<>();
			HashMap<String, Double> local_hash = new HashMap<>();

			Set<String> set = PopularityGenerate.ContentZipfRaw.keySet();
			for (String e : set) {
				local_tree.put(PopularityGenerate.ContentZipfRaw.get(e), e);
				local_hash.put(e, PopularityGenerate.ContentZipfRaw.get(e));
			}

			predict_tree.put("LocalServer" + i, local_tree);
			predict_hash.put("LocalServer" + i, local_hash);
		}

		arrival_rate.put("LocalServer1", 4.0);
		arrival_rate.put("LocalServer2", 4.0);
		arrival_rate.put("LocalServer3", 4.0);

		ReMap_1(5, 50);
	}

}
