package simulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class SimulationYouTube {
	public static HashMap<String, HashMap<String, Integer>> Task = new HashMap<>();
	public static HashMap<String, Double> Content_like = new HashMap<>();
	public static HashMap<String, HashSet<String>> ContentMap = new HashMap<>();
	public static HashMap<String, Double> pressure = new HashMap<>();

	static {
		Task.put("Zone1", new HashMap<>());
		Task.put("Zone2", new HashMap<>());
		Task.put("Zone3", new HashMap<>());
	}

	public static void LoadTask(int day) throws Exception {
		System.out.println("正在导入任务！");

		String filePath = "D:\\QuickAccessWorkFile\\Project_3_CDN\\dataset\\YouTube\\outclick.csv";

		BufferedReader fin = new BufferedReader(new FileReader(filePath));
		String line = null;
		for (int i = 1; i <= 10000; i++) {
			line = fin.readLine();
			String[] info = line.split(",");
			int num = Integer.parseInt(info[day]) / 3;

			for (int j = 1; j <= 3; j++) {
				Task.get("Zone" + j).put("C" + i, num);
			}

		} // end for fin
		fin.close();

		System.out.println("任务导入结束！");
	}

	public static void LoadRank(int day) throws Exception {
		System.out.println("正在导入喜好程度数据！");

		String filePath = "D:\\QuickAccessWorkFile\\Project_3_CDN\\dataset\\YouTube\\outclick.csv";

		int point = day; // 9前一天的like， 10是这一天的like，11是预测

		BufferedReader fin = new BufferedReader(new FileReader(filePath));
		String line = null;
		for (int i = 1; i <= 10000; i++) {
			line = fin.readLine();
			String[] info = line.split(",");

			Content_like.put("C" + i, Double.parseDouble(info[point]));
		} // end for fin

		fin.close();

		System.out.println("喜好程度数据导入结束！");
	}

	public static void Map(int com, int total) {
		//System.out.println("正在生成Map！");
		// 先初始化
		pressure.clear();
		ContentMap.clear();
		ContentMap.put("Zone1", new HashSet<>());
		ContentMap.put("Zone2", new HashSet<>());
		ContentMap.put("Zone3", new HashSet<>());

		// ----------------------------------------------

		TreeSet<Double> likerank = new TreeSet<>();
		for (String e : Content_like.keySet()) {
			likerank.add(Content_like.get(e));
		}

		ArrayList<String> content_rank = new ArrayList<>();
		for (Double e : likerank.descendingSet()) {
			for (String c : Content_like.keySet()) {
				if (Content_like.get(c).equals(e)) {
					content_rank.add(c);
				} // end if
			} // end content_like
		} // end likerank

		//System.out.println("Content_rank:" + content_rank.size());

		// ----------------------------------------------
		for (int i = 1; i <= 3; i++) {
			HashSet<String> zone = ContentMap.get("Zone" + i);
			double sum = 0;
			for (int j = 0; j < com; j++) {
				zone.add(content_rank.get(j));
				sum = sum + Content_like.get(content_rank.get(j));
			}
			pressure.put("Zone" + i, sum);
		}

		int point = com;
		String target = null;
		while ((target = findLocalServer(total)) != null) {
			ContentMap.get(target).add(content_rank.get(point));
			double pre = pressure.get(target);
			pre = pre + Content_like.get(content_rank.get(point));
			pressure.put(target, pre);

			point++;
		}

		//System.out.println("生成Map结束！");
	}

	private static String findLocalServer(int total) {
		TreeMap<Double, String> tree = new TreeMap<>();

		for (int i = 1; i <= 3; i++) {
			if (ContentMap.get("Zone" + i).size() < total) {
				tree.put(pressure.get("Zone" + i), "Zone" + i);
			}
		}

		if (tree.size() >= 1) {
			String target = tree.firstEntry().getValue();
			return target;
		} else {
			return null;
		}

	}

	public static double doSimulation() {
		double latency = 0;
		int sum = 0;

		for (int i = 1; i <= 3; i++) {
			String target = "Zone" + i;
			HashMap<String, Integer> zone_task = Task.get(target);
			for (String e : zone_task.keySet()) {
				sum = sum + zone_task.get(e);

				if (ContentMap.get(target).contains(e)) {
					latency = latency + 30.0 * zone_task.get(e);
				} else if (hasContent(e)) {
					latency = latency + 60.0 * zone_task.get(e);
				} else {
					latency = latency + 120.0 * zone_task.get(e);
				}

			} // end zone_task for

		} // end simulation for

		latency = latency / sum;
		//System.out.println(latency);
		return latency;
	}

	private static boolean hasContent(String id) {
		boolean result = false;

		for (int i = 1; i <= 3; i++) {
			if (ContentMap.get("Zone" + i).contains(id)) {
				result = true;
			}
		}

		return result;
	}

	public static void main(String[] args) throws Exception {

		int day = 20;
		LoadTask(day);
		LoadRank(day-4);
		
		
		Map(260, 800);
		double latency = doSimulation();
		System.out.println(240+":"+latency);

//		double min = 100;
//		int index = 0;
//		for(int i=0;i<=400;i+=20){			
//			Map(i, 800);
//			double latency = doSimulation();
//			System.out.println(i+":"+latency);
//			
//			if(latency<min){
//				min = latency;
//				index = i;
//			}
//		}
//		
//		System.out.println("Min:index:"+index+"  val:"+min);
		
				
	}
}
