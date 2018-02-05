package simulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SimulationMMNC {

	public static TreeMap<Long, SimulationTask> TaskTimeLine = new TreeMap<>();
	public static HashMap<String, Double> Zone1 = new HashMap<>();
	public static HashMap<String, Double> Zone2 = new HashMap<>();
	public static HashMap<String, Double> Zone3 = new HashMap<>();
	public static double Arrival_rate1 = 0;
	public static double Arrival_rate2 = 0;
	public static double Arrival_rate3 = 0;

	public static HashMap<String, HashSet<String>> ContentMap = new HashMap<>();
	static {
		ContentMap.put("Zone1", new HashSet<String>());
		ContentMap.put("Zone2", new HashSet<String>());
		ContentMap.put("Zone3", new HashSet<String>());
	}

	// -----------------------------------------------------------------------------

	public static void LoadTask(int day) throws Exception {
		String FilePath1 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-1.dat";
		String FilePath2 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-2.dat";
		String FilePath3 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-3.dat";

		BufferedReader fin1 = new BufferedReader(new FileReader(FilePath1));
		BufferedReader fin2 = new BufferedReader(new FileReader(FilePath2));
		BufferedReader fin3 = new BufferedReader(new FileReader(FilePath3));

		String line = null;
		long start_time = 1000000 * (1639675L + 60 * 60 * 24 * (day - 1));
		long end_time = 1000000 * (1639675L + 60 * 60 * 24 * day);
		int taskid = 1;

		taskid = 1;
		while ((line = fin1.readLine()) != null) {
			String[] info = line.split(" ");

			String time_st = info[0].substring(3, 10) + info[0].substring(11);

			long time = Long.parseLong(time_st);
			if (time > start_time && time < end_time) {
				SimulationTask simulationtask = new SimulationTask();
				simulationtask.TaskType = "Request";
				simulationtask.TaskFrom = "Zone1";
				simulationtask.TaskID = taskid++;
				simulationtask.TaskContnet = info[4];
				simulationtask.TaskStartTime = time;
				TaskTimeLine.put(time, simulationtask);
			}
		}

		taskid = 1;
		while ((line = fin2.readLine()) != null) {
			String[] info = line.split(" ");

			String time_st = info[0].substring(3, 10) + info[0].substring(11);

			long time = Long.parseLong(time_st);
			if (time > start_time && time < end_time) {
				SimulationTask simulationtask = new SimulationTask();
				simulationtask.TaskType = "Request";
				simulationtask.TaskFrom = "Zone2";
				simulationtask.TaskID = taskid++;
				simulationtask.TaskContnet = info[4];
				simulationtask.TaskStartTime = time;
				TaskTimeLine.put(time, simulationtask);
			}
		}

		taskid = 1;
		while ((line = fin3.readLine()) != null) {
			String[] info = line.split(" ");

			String time_st = info[0].substring(3, 10) + info[0].substring(11);

			long time = Long.parseLong(time_st);
			if (time > start_time && time < end_time) {
				SimulationTask simulationtask = new SimulationTask();
				simulationtask.TaskType = "Request";
				simulationtask.TaskFrom = "Zone3";
				simulationtask.TaskID = taskid++;
				simulationtask.TaskContnet = info[4];
				simulationtask.TaskStartTime = time;
				TaskTimeLine.put(time, simulationtask);
			}
		}

		System.out.println("导入第" + day + "天的任务，共计导入任务：" + TaskTimeLine.size());

		fin1.close();
		fin2.close();
		fin3.close();
	}

	public static void LoadPopularity(int day) throws Exception {
		String FilePath1 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-1-timeSlot-12.csv";
		String FilePath2 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-2-timeSlot-12.csv";
		String FilePath3 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-3-timeSlot-12.csv";

		BufferedReader fin1 = new BufferedReader(new FileReader(FilePath1));
		BufferedReader fin2 = new BufferedReader(new FileReader(FilePath2));
		BufferedReader fin3 = new BufferedReader(new FileReader(FilePath3));

		double sum1 = 0, sum2 = 0, sum3 = 0;

		// 导入点击率数据
		String line = null;
		while ((line = fin1.readLine()) != null) {
			String[] info = line.split(",");
			double count = Double.parseDouble(info[day]);
			sum1 = sum1 + count;
			Zone1.put(info[0], count);
		}

		while ((line = fin2.readLine()) != null) {
			String[] info = line.split(",");
			double count = Double.parseDouble(info[day]);
			sum2 = sum2 + count;
			Zone2.put(info[0], count);
		}

		while ((line = fin3.readLine()) != null) {
			String[] info = line.split(",");
			double count = Double.parseDouble(info[day]);
			sum3 = sum3 + count;
			Zone3.put(info[0], count);
		}

		fin1.close();
		fin2.close();
		fin3.close();

		// 计算喜好程度
		/*
		 * for(String e:Zone1.keySet()){ double pre = Zone1.get(e); pre = pre /
		 * sum1; Zone1.put(e, pre); }
		 * 
		 * for(String e:Zone2.keySet()){ double pre = Zone2.get(e); pre = pre /
		 * sum2; Zone2.put(e, pre); }
		 * 
		 * for(String e:Zone3.keySet()){ double pre = Zone3.get(e); pre = pre /
		 * sum3; Zone3.put(e, pre); }
		 */

		// 计算到达率信息（分钟级）
		Arrival_rate1 = sum1 / (24 * 60);
		Arrival_rate2 = sum2 / (24 * 60);
		Arrival_rate3 = sum3 / (24 * 60);

		System.out.println("载入第" + day + "天喜好程度数据:");
		System.out.println("加载1号服务器文件数：" + Zone1.size());
		System.out.println("加载2号服务器文件数：" + Zone2.size());
		System.out.println("加载3号服务器文件数：" + Zone3.size());
		System.out.println("1到达率：" + Arrival_rate1);
		System.out.println("2到达率：" + Arrival_rate2);
		System.out.println("3到达率：" + Arrival_rate3);
	}

	public static void SetMap(int com, int total) {
		HashMap<String, Double> AllContent = new HashMap<>();
		HashMap<String, HashMap<String, Double>> Zone = new HashMap<>();
		Zone.put("Zone1", Zone1);
		Zone.put("Zone2", Zone2);
		Zone.put("Zone3", Zone3);

		HashMap<String, ArrayList<String>> ZoneRank = new HashMap<>();
		ZoneRank.put("Zone1", new ArrayList<String>());
		ZoneRank.put("Zone2", new ArrayList<String>());
		ZoneRank.put("Zone3", new ArrayList<String>());

		// 先找到每个最大的
		HashMap<String, Double> Max = new HashMap<>();
		Max.put("Zone1", 0.0);
		Max.put("Zone2", 0.0);
		Max.put("Zone3", 0.0);

		for (int i = 1; i <= 3; i++) {
			double max = 0;
			HashMap<String, Double> localzone = Zone.get("Zone" + i);
			for (String e : localzone.keySet()) {
				// 将所有出现过的内容ID存入AllContent中
				AllContent.put(e, 0.0);
				if (localzone.get(e) > max)
					max = localzone.get(e);
			}
			Max.put("Zone" + i, max);
		}
		System.out.println("所有内容：" + AllContent.size());

		// 将顺序写入ZoneRank中
		for (int i = 1; i <= 3; i++) {
			double point = Max.get("Zone" + i);
			ArrayList<String> zone_rank = ZoneRank.get("Zone" + i);
			HashMap<String, Double> zone = Zone.get("Zone" + i);
			while (point >= 0) {
				for (String e : zone.keySet()) {
					if (zone.get(e) == point)
						zone_rank.add(e);
				}

				point = point - 1;
			}
		}

		// 顺序存放
		for (int i = 1; i <= 3; i++) {
			HashSet<String> contentmap = ContentMap.get("Zone" + i);
			ArrayList<String> zone_rank = ZoneRank.get("Zone" + i);
			int point = 0;
			while (contentmap.size() != com) {
				contentmap.add(zone_rank.get(point));
				point++;
			}
		}

		// 计算全局喜好程度
		int count0 = 0;
		TreeSet<Double> global_like = new TreeSet<>();
		for (String e : AllContent.keySet()) {
			double like = Arrival_rate1 * findContentClick("Zone1", e) + Arrival_rate2 * findContentClick("Zone2", e)
					+ Arrival_rate3 * findContentClick("Zone3", e);
			AllContent.put(e, like);

			global_like.add(like);

			if (like <= 0)
				count0++;
		}

		// 对全局喜好程度排序
		System.out.println("global_like:" + global_like.size());
		System.out.println("0的数目:" + count0);

		ArrayList<String> global = new ArrayList<>();
		Set<Double> set_globan_like = global_like.descendingSet();
		for (Double e : set_globan_like) {
			for (String ee : AllContent.keySet()) {
				if (AllContent.get(ee).equals(e))
					global.add(ee);
			}
		}
		System.out.println("global size:" + global.size());

		// 开始分布式排序
		for (String e : global) {
			// 如果没有缓存,且仍有缓存空间
			ifCached(e, total);
		}

		System.out.println("Map结束");

	}

	private static void ifCached(String id, int total) {
		boolean result = true;
		ArrayList<String> candid = new ArrayList<>();
		TreeMap<Double, String> can = new TreeMap<>();
		HashMap<String, Double> arrival = new HashMap<>();
		arrival.put("Zone1", Arrival_rate1);
		arrival.put("Zone2", Arrival_rate2);
		arrival.put("Zone3", Arrival_rate3);

		for (int i = 1; i <= 3; i++) {
			if (ContentMap.get("Zone" + i).contains(id))
				result = false;
		}

		// 如果该内容目前没有被存储过
		if (result) {
			for (int i = 1; i <= 3; i++) {
				if (ContentMap.get("Zone" + i).size() < total)
					candid.add("Zone" + i);
			}
		} else {
			return;
		}

		// 如果内容目前可存储
		if (!candid.isEmpty()) {
			for (String e : candid) {
				double rank = arrival.get(e) * findContentClick(e, id);
				can.put(rank, e);
			}

			// 取出值最大的
			String target = can.get(can.lastKey());
			ContentMap.get(target).add(id);

		} else {
			return;
		}
	}

	private static double findContentClick(String zone, String id) {
		if ("Zone1".equals(zone)) {
			if (Zone1.containsKey(id)) {
				return Zone1.get(id);
			} else {
				return 0;
			}
		}

		if ("Zone2".equals(zone)) {
			if (Zone2.containsKey(id)) {
				return Zone2.get(id);
			} else {
				return 0;
			}
		}

		if ("Zone3".equals(zone)) {
			if (Zone3.containsKey(id)) {
				return Zone3.get(id);
			} else {
				return 0;
			}
		}

		return 0;
	}

	public static void doSimulation() {
		// 按照时间轴进行仿真
		double latency = 0;

		for (Long e : TaskTimeLine.keySet()) {
			SimulationTask task = TaskTimeLine.get(e);

			if (ContentMap.get(task.TaskFrom).contains(task.TaskContnet)) {
				task.TaskServer = task.TaskFrom;
				latency = latency + 30.0;
			} else {
				if (ZoneHasContent(task.TaskContnet)) {
					task.TaskServer = "Zone";
					latency = latency + 40.0;
				} else {
					task.TaskServer = "Original";
					latency = latency + 60.0;
				}
			}
		}

		latency = latency / TaskTimeLine.size();

		System.out.println("平均时延：" + latency + "ms");

	}

	private static boolean ZoneHasContent(String id) {
		boolean result = false;

		for (int i = 1; i <= 3; i++) {
			if (ContentMap.get("Zone" + i).contains(id))
				result = true;
		}

		return result;
	}

	public static void doSimulationLRU(int total) {
		HashMap<String, ArrayList<String>> LRUMap = new HashMap<>();
		LRUMap.put("Zone1", new ArrayList<String>());
		LRUMap.put("Zone2", new ArrayList<String>());
		LRUMap.put("Zone3", new ArrayList<String>());

		double latency = 0;

		for (Long e : TaskTimeLine.keySet()) {
			SimulationTask task = TaskTimeLine.get(e);
			if (LRUMap.get(task.TaskFrom).contains(task.TaskContnet)) {
				// 将最新访问的内容放到最后
				LRUMap.get(task.TaskFrom).remove(task.TaskContnet);
				LRUMap.get(task.TaskFrom).add(task.TaskContnet);

				latency = latency + 30.0;
			} else {
				if (LRUMap.get(task.TaskFrom).size() < total) {
					LRUMap.get(task.TaskFrom).add(task.TaskContnet);
				} else {
					LRUMap.get(task.TaskFrom).remove(0);
					LRUMap.get(task.TaskFrom).add(task.TaskContnet);
				}

				latency = latency + 60.0;
			}

		}

		latency = latency / TaskTimeLine.size();
		System.out.println("TaskNumber:" + TaskTimeLine.size());
		System.out.println("平均时延：" + latency + "ms");

	}

	public static void main(String[] args) throws Exception {
		int day = 12;

		LoadTask(day);

		// ------General---------
		LoadPopularity(day-1);// day-1
		SetMap(2500, 2500); // 此处的算法有很大的改进空间
		doSimulation();

		// -------LRU---------
		// doSimulationLRU(2500);
	}

}
