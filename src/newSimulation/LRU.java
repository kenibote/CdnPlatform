package newSimulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import simulation.SimulationTask;

public class LRU {
	public static int MaxCapacity = 10;
	public static int MaxSpace = 100;

	public static TreeMap<Double, SimulationTask> time_line = new TreeMap<>();
	public static ArrayList<SimulationTask> history = new ArrayList<>();
	public static HashMap<String, Integer> now_capacity = new HashMap<>();

	public static HashMap<String, LinkedList<String>> ContentMap = new HashMap<>();
	public static double sim_pre_zone1 = 0.0;
	public static double sim_pre_zone2 = 0.0;
	public static double sim_pre_zone3 = 0.0;
	static double L1 = 0.29, L2 = 0.36, L3 = 0.6;

	static {
		now_capacity.put("Zone1", MaxCapacity);
		now_capacity.put("Zone2", MaxCapacity);
		now_capacity.put("Zone3", MaxCapacity);

		ContentMap.put("Zone1", new LinkedList<String>());
		ContentMap.put("Zone2", new LinkedList<String>());
		ContentMap.put("Zone3", new LinkedList<String>());

	}

	public static void addpressuremonitor(int end_time) {

		double start_time = 0;
		while (start_time < end_time) {
			start_time = start_time + 1.0;

			SimulationTask task = new SimulationTask();
			task.TaskType = SimulationTask.CheckPressure;

			if (time_line.put(start_time, task) != null) {
				System.out.println(" add monitor error");
			}
		}

	}

	public static void doSimulation() {
		System.out.println("开始仿真！");

		while (time_line.size() != 0) {
			SimulationTask task = time_line.firstEntry().getValue();
			time_line.remove(time_line.firstKey());

			if (task.TaskType.equals(SimulationTask.Request)) {
				history.add(task);

				// 如果本地有该内容，且可以服务
				if (ContentMap.get(task.TaskFrom).contains(task.TaskContnet) && now_capacity.get(task.TaskFrom) > 0) {
					int val = now_capacity.get(task.TaskFrom);
					val--;
					now_capacity.put(task.TaskFrom, val);

					task.TaskServer = task.TaskFrom;

					// 设置释放任务
					SimulationTask release = new SimulationTask();
					release.TaskType = SimulationTask.Release;
					release.TaskFrom = task.TaskFrom;
					release.TaskStartTime = task.TaskStartTime + L1;
					time_line.put(release.TaskStartTime, release);

					// 更新排序
					ContentMap.get(task.TaskFrom).remove(task.TaskContnet);
					ContentMap.get(task.TaskFrom).offerFirst(task.TaskContnet);

					continue;

				} else {
					// 如果本地不能服务，由原服务器服务
					task.TaskServer = "Original";

					// 缓存该内容
					if (ContentMap.get(task.TaskFrom).size() >= MaxSpace) {
						ContentMap.get(task.TaskFrom).removeLast();
					}

					ContentMap.get(task.TaskFrom).offerFirst(task.TaskContnet);
				}

			} // end request Task

			// 执行释放任务
			if (task.TaskType.equals(SimulationTask.Release)) {
				int val = now_capacity.get(task.TaskFrom);
				val++;
				now_capacity.put(task.TaskFrom, val);
			} // end if release

			// 如果是压力检查任务
			if (task.TaskType.equals(SimulationTask.CheckPressure)) {
				sim_pre_zone1 = sim_pre_zone1 + now_capacity.get("Zone1");
				sim_pre_zone2 = sim_pre_zone2 + now_capacity.get("Zone2");
				sim_pre_zone3 = sim_pre_zone3 + now_capacity.get("Zone3");
			}

		} // end while

	}

	public static void analysis() {
		System.out.println("开始统计！");

		HashMap<String, Double> sum_delay = new HashMap<>();
		HashMap<String, Integer> sum_count = new HashMap<>();

		sum_count.put("Zone1", 0);
		sum_count.put("Zone2", 0);
		sum_count.put("Zone3", 0);
		sum_delay.put("Zone1", 0.0);
		sum_delay.put("Zone2", 0.0);
		sum_delay.put("Zone3", 0.0);

		double original_load = 0;

		for (SimulationTask task : history) {
			int count = sum_count.get(task.TaskFrom);
			count++;
			sum_count.put(task.TaskFrom, count);

			if (task.TaskFrom.equals(task.TaskServer)) {
				double val = sum_delay.get(task.TaskFrom);
				val = val + L1;
				sum_delay.put(task.TaskFrom, val);

				continue;
			}

			if (task.TaskServer.equals("Original")) {
				double val = sum_delay.get(task.TaskFrom);
				val = val + L3;
				sum_delay.put(task.TaskFrom, val);
				original_load = original_load + 1;

				continue;
			}

			double val = sum_delay.get(task.TaskFrom);
			val = val + L2;
			sum_delay.put(task.TaskFrom, val);

		}

		// -------------------------------------------------
		System.out.println("Zone1:" + sum_delay.get("Zone1") / sum_count.get("Zone1"));
		System.out.println("Zone2:" + sum_delay.get("Zone2") / sum_count.get("Zone2"));
		System.out.println("Zone3:" + sum_delay.get("Zone3") / sum_count.get("Zone3"));

		double sum_request = sum_count.get("Zone1") + sum_count.get("Zone2") + sum_count.get("Zone3");

		double average_delay = (sum_delay.get("Zone1") + sum_delay.get("Zone2") + sum_delay.get("Zone3")) / sum_request;

		System.out.println(average_delay);
		System.out.println("Original load:" + original_load / sum_request);

		// ------------------------------------------------
		System.out.println("----------------------------");
		System.out.println("Used_zone1:" + (MaxCapacity - sim_pre_zone1 / 21600));
		System.out.println("Used_zone2:" + (MaxCapacity - sim_pre_zone2 / 21600));
		System.out.println("Used_zone3:" + (MaxCapacity - sim_pre_zone3 / 21600));

	}

	public static void main(String[] args) {
		PublicSetting.ArrivalRate.put("Zone1", 20.0);
		PublicSetting.ArrivalRate.put("Zone2", 10.0);
		PublicSetting.ArrivalRate.put("Zone3", 5.0);

		// 准备工作
		PublicSetting.GenerateZipf(0.88);
		PublicSetting.PutPopularitySame();

		PublicSetting.CalculateLikePoint();
		PublicSetting.CalculateLocalRank();
		PublicSetting.CalculateGoRank();

		// 设置仿真任务
		PublicSetting.Generate_Task(21600);
		time_line = PublicSetting.time_line;
		addpressuremonitor(21600);

		// 执行仿真
		doSimulation();

		analysis();
	}
}
