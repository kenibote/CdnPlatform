package newSimulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import simulation.SimulationTask;

public class LRU {
	public static int MaxCapacity = 10;
	public static int MaxSpace = 100;

	public static TreeMap<Double, SimulationTask> time_line = new TreeMap<>();
	public static ArrayList<SimulationTask> history = new ArrayList<>();
	public static HashMap<String, Integer> now_capacity = new HashMap<>();

	public static HashMap<String, LinkedHashSet<String>> ContentMap = new HashMap<>();
	public static HashMap<String, Double> pressure = new HashMap<>();
	static double L1 = 0.29, L2 = 0.36, L3 = 0.6;

	static {
		now_capacity.put("Zone1", MaxCapacity);
		now_capacity.put("Zone2", MaxCapacity);
		now_capacity.put("Zone3", MaxCapacity);

		ContentMap.put("Zone1", new LinkedHashSet<String>());
		ContentMap.put("Zone2", new LinkedHashSet<String>());
		ContentMap.put("Zone3", new LinkedHashSet<String>());

		pressure.put("Zone1", 0.0);
		pressure.put("Zone2", 0.0);
		pressure.put("Zone3", 0.0);
	}
	
	public static void addpressuremonitor(int end_time){
		
		double start_time=0;
		while(start_time<end_time){
			start_time = start_time + 1.0;
			
			SimulationTask task = new SimulationTask();
			task.TaskType = SimulationTask.CheckPressure;
			
			if (time_line.put(start_time, task) != null) {
				System.out.println(" add monitor error");
			}
		}
		
	}
	
	public static void doSimulation(){
		
		
	}

	public static void main(String[] args) {
		PublicSetting.ArrivalRate.put("Zone1", 10.0);
		PublicSetting.ArrivalRate.put("Zone2", 10.0);
		PublicSetting.ArrivalRate.put("Zone3", 10.0);

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
		
	}
}
