package newSimulation;

import java.util.*;

import simulation.SimulationTask;
import simulation.randomfunction;

public class PublicSetting {
	public static HashMap<String, Double> ArrivalRate = new HashMap<>();
	public static HashMap<Integer, Double> Zipf = new HashMap<>();

	public static HashMap<String, HashMap<String, Double>> Popularity = new HashMap<>();
	public static HashMap<String, TreeMap<Double, String>> LikePoint = new HashMap<>();
	public static HashMap<String, ArrayList<String>> LocalRank = new HashMap<>();
	public static ArrayList<String> GlobalRank = new ArrayList<>();

	//public static HashMap<String, HashSet<String>> Map = new HashMap<>();
	public static TreeMap<Double, SimulationTask> time_line = new TreeMap<>();

	public static int ContentNumber = 1000;
	public static int StorageSpace = 100;
	public static double Processing = 80.0;
	public static double rate =0.8;

	public static double L1 = 2.0;
	public static double L2 = 7.5;
	public static double L3 = 17.5;

	static {
		ArrivalRate.put("Zone1", 40.0);
		ArrivalRate.put("Zone2", 20.0);
		ArrivalRate.put("Zone3", 10.0);
	}

	/**
	 * 产生Zipf分布
	 */
	public static void GenerateZipf(double alpha) {
		Zipf.clear();

		double[] p = new double[ContentNumber + 1];
		double sum = 0;

		for (int i = 1; i <= ContentNumber; i++) {
			p[i] = 1 / Math.pow(i, alpha);
			sum = sum + p[i];
		}

		// 归一化
		for (int i = 1; i <= ContentNumber; i++) {
			Zipf.put(i, p[i] / sum);
		}

	}

	/**
	 * 放置内容喜好程度，这里是相同的喜好程度
	 */
	public static void PutPopularitySame() {
		for (int i = 1; i <= 3; i++) {
			HashMap<String, Double> zone_popularity = new HashMap<>();

			for (int c = 1; c <= ContentNumber; c++) {
				zone_popularity.put("C" + c, Zipf.get(c));
			}

			Popularity.put("Zone" + i, zone_popularity);
		}
	}

	/**
	 * 这里可以添加别的函数，以修改喜好程度分布情况
	 * */
	public static void ChangePopularity(int zone){
		HashMap<String, Double> localp = new HashMap<>();
		
		ArrayList<Integer> index = new ArrayList<>();
		for(int i=1;i<=ContentNumber;i++){
			index.add(i);
		}
		
		// shuffle 打乱
		Collections.shuffle(index, new Random(zone));
		
		for(int i=1;i<=ContentNumber;i++){
			localp.put("C"+index.get(i-1), Zipf.get(i));
		}
		
		//重新配置Zone3的喜好程度
		Popularity.put("Zone"+zone, localp);
		
		System.out.println("");
		
		// 输出结果用于替换AMPL文件
//		for(int i=1;i<=ContentNumber;i++){
//		 System.out.println("[S"+zone+",C"+i+"] "+localp.get("C"+i));
//		}
		
	}
	
		
	/**
	 * 计算LikePoint,必须在放完喜好程序数据之后使用；
	 */
	public static void CalculateLikePoint() {
		for (int i = 1; i <= 3; i++) {
			HashMap<String, Double> zone_popularity = Popularity.get("Zone" + i);
			TreeMap<Double, String> like_point = new TreeMap<>();

			double sum = 0;
			for (int c = 1; c <= ContentNumber; c++) {
				sum = sum + zone_popularity.get("C" + c);
				like_point.put(sum, "C" + c);
			}

			LikePoint.put("Zone" + i, like_point);
		}
	}

	/**
	 * 计算本地排序
	 * */
	public static void CalculateLocalRank() {
		for (int i = 1; i <= 3; i++) {
			HashMap<String, Double> zone_popularity = Popularity.get("Zone" + i);
			ArrayList<String> rank = new ArrayList<>();
			rank.add(" ");
			
			// popularity值从大到小
			TreeSet<Double> number_rank = new TreeSet<>(Collections.reverseOrder());
			for (int c = 1; c <= ContentNumber; c++) {
				number_rank.add(zone_popularity.get("C" + c));
			}

			// 按照number_rank的顺序逐个查找
			for (double e : number_rank) {
				for (int c = 1; c <= ContentNumber; c++) {
					if (Double.compare(e, zone_popularity.get("C" + c)) == 0)
						rank.add("C" + c);
				}
			}

			LocalRank.put("Zone" + i, rank);
		}
	}

	
	/**
	 * 计算全局rank
	 * */
	public static void CalculateGoRank() {
		HashMap<String, Double> wP = new HashMap<>();
		TreeSet<Double> number_rank = new TreeSet<>(Collections.reverseOrder());

		// 先计算加权求和
		for (int c = 1; c <= ContentNumber; c++) {
			double wval = 0.0;
			for (int i = 1; i <= 3; i++) {
				wval = wval + ArrivalRate.get("Zone" + i) * Popularity.get("Zone" + i).get("C" + c);
			}

			wP.put("C" + c, wval);
			number_rank.add(wval);
		}

		GlobalRank.add(" ");
		for (double e : number_rank) {
			for (int c = 1; c <= ContentNumber; c++) {
				if (Double.compare(e, wP.get("C" + c)) == 0)
					GlobalRank.add("C" + c);
			}

		}

	}
	
	
	/**
	 * 生成仿真任务列表
	 * */
	public static void Generate_Task(double end_time) {

		for (int i = 1; i <= 3; i++) {
			TreeMap<Double, String> zone_like_point = LikePoint.get("Zone" + i);

			double start_time = 0.0;
			
			randomfunction rndfunction = new randomfunction();
			Random rnd_time = new Random(i);
			Random rnd_content = new Random(4 * i);
			int ID = 0;

			while (start_time < end_time) {
				double time_slot = 1 / PublicSetting.ArrivalRate.get("Zone"+i) * rndfunction.expdev(rnd_time);
				start_time = start_time + time_slot;
				
				SimulationTask task = new SimulationTask();
				task.TaskType = SimulationTask.Request;
				task.TaskFrom = "Zone"+i;
				ID++;
				task.TaskID = ID;
				task.TaskContnet = zone_like_point.ceilingEntry(rnd_content.nextDouble()).getValue();
				task.TaskStartTime = start_time;

				if (time_line.put(start_time, task) != null) {
					System.out.println("error");
				}
			} // end while time

		}// end for each zone

	}
	
	
	public static void ShowMap(HashMap<String, HashSet<String>> mMap) {
		HashMap<String, Iterator<Integer>> show = new HashMap<>();

		for (int i = 1; i <= 3; i++) {
			HashSet<String> local_map = mMap.get("Zone" + i);
			TreeSet<Integer> local_map_show = new TreeSet<>();

			for (String e : local_map) {
				local_map_show.add(Integer.parseInt(e.substring(1)));
			}

			show.put("Zone" + i, local_map_show.iterator());
		}

		System.out.println("-------ShowMap-------");
		for (int c = 1; c <= StorageSpace; c++) {
			for (int i = 1; i <= 3; i++)
				System.out.print(show.get("Zone" + i).next() + "  ");

			System.out.println("");
		}

	}
	
	
	public static void main(String[] args) {
		GenerateZipf(0.88);
		PutPopularitySame();
		//-----------------
		ChangePopularity(2);
		ChangePopularity(3);
		//-----------------
		CalculateLikePoint();
		CalculateLocalRank();
		CalculateGoRank();
		
		Generate_Task(21600);
		
		System.out.println("END");
	}
}
