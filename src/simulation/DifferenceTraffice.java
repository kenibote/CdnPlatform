package simulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;


public class DifferenceTraffice {
	public static HashMap<String, Double> Content_like = new HashMap<>();
	public static TreeMap<Double, String> like_point = new TreeMap<>();
	public static double sum = 0;

	public static HashMap<String, Double> speed = new HashMap<>();
	public static TreeMap<Double, SimulationTask> time_line = new TreeMap<>();
	public static ArrayList<SimulationTask> history = new ArrayList<>();
	public static int MaxCapacity = 80;
	public static HashMap<String, Integer> now_capacity = new HashMap<>();
	
	public static HashMap<String, HashSet<String>> ContentMap = new HashMap<>();
	public static HashMap<String, Double> pressure = new HashMap<>();

	public static double sim_pre_zone1 = 0.0;
	public static double sim_pre_zone2 = 0.0;
	public static double sim_pre_zone3 = 0.0;
	public static double average_delay = 0;
	static double L1=2.0,L2=7.5,L3=17.5;
	
	static {
		speed.put("Zone1", 40.0);
		speed.put("Zone2", 20.0);
		speed.put("Zone3", 10.0);

		now_capacity.put("Zone1", MaxCapacity);
		now_capacity.put("Zone2", MaxCapacity);
		now_capacity.put("Zone3", MaxCapacity);
		
		ContentMap.put("Zone1", new HashSet<String>());
		ContentMap.put("Zone2", new HashSet<String>());
		ContentMap.put("Zone3", new HashSet<String>());
		
		pressure.put("Zone1", 0.0);
		pressure.put("Zone2", 0.0);
		pressure.put("Zone3", 0.0);
	}
	
	public static void init(){
		Content_like.clear();
		like_point.clear();
		
		ContentMap.get("Zone1").clear();
		ContentMap.get("Zone2").clear();
		ContentMap.get("Zone3").clear();
	
		pressure.put("Zone1", 0.0);
		pressure.put("Zone2", 0.0);
		pressure.put("Zone3", 0.0);
		
		sum=0.0;
		time_line.clear();
		history.clear();
		
		now_capacity.put("Zone1", MaxCapacity);
		now_capacity.put("Zone2", MaxCapacity);
		now_capacity.put("Zone3", MaxCapacity);
		
		sim_pre_zone1=0.0;
		sim_pre_zone2=0.0;
		sim_pre_zone3=0.0;
	}
	

	public static void LoadRank(int day) throws Exception {
		System.out.println("正在导入喜好程度数据！");

		String filePath = "D:\\QuickAccessWorkFile\\Project_3_CDN\\dataset\\YouTube\\outclick.csv";

		int point = day;

		BufferedReader fin = new BufferedReader(new FileReader(filePath));
		String line = null;
		for (int i = 1; i <= 10000; i++) {
			line = fin.readLine();
			String[] info = line.split(",");

			Content_like.put("C" + i, Double.parseDouble(info[point]) / 100);
			sum = sum + Double.parseDouble(info[point]) / 100;
			like_point.put(sum, "C" + i);
		} // end for fin

		fin.close();

		System.out.println("喜好程度数据导入结束！");
	}
	
	
	public static void LoadZipfRank(){
		System.out.println("正在导入喜好程度数据！");

		for (int i = 1; i <= 1000; i++) {
			
			double val = 100/(Math.pow(i, 0.88));

			Content_like.put("C" + i, val);
			sum = sum + val;
			like_point.put(sum, "C" + i);
		} // end for fin


		System.out.println("喜好程度数据导入结束！");
		
	}
	

	public static void Generate_task(String zone, int seed, double end_time) {

		double start_time = 0;
		// 设置随机数种子
		randomfunction rndfunction = new randomfunction();
		Random rnd_time = new Random(seed);
		Random rnd_content = new Random(2*seed);
		int ID = 0;

		while (start_time < end_time) {
			double time_slot = 1 / speed.get(zone) * rndfunction.expdev(rnd_time);

			start_time = start_time + time_slot;

			SimulationTask task = new SimulationTask();
			task.TaskType = SimulationTask.Request;
			task.TaskFrom = zone;
			ID++;
			task.TaskID = ID;
			task.TaskContnet = like_point.ceilingEntry(rnd_content.nextDouble() * sum).getValue();
			task.TaskStartTime = start_time;

			if (time_line.put(start_time, task) != null) {
				System.out.println("error");
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

				// 如果可以由本地服务
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

					continue;
				}

				// 如果由其它服务器服务
				String server = findServer(task);
				if (server != null) {
					int val = now_capacity.get(server);
					val--;
					now_capacity.put(server, val);

					task.TaskServer = server;

					// 设置释放任务
					SimulationTask release = new SimulationTask();
					release.TaskType = SimulationTask.Release;
					release.TaskFrom = server;
					release.TaskStartTime = task.TaskStartTime + L2;
					time_line.put(release.TaskStartTime, release);

					continue;
				}

				// 最后由原服务器服务
				task.TaskServer = "Original";

			} // end if request

			// 执行释放任务
			if (task.TaskType.equals(SimulationTask.Release)) {
				int val = now_capacity.get(task.TaskFrom);
				val++;
				now_capacity.put(task.TaskFrom, val);
			} // end if release
			
			// 如果是压力检查任务
			if(task.TaskType.equals(SimulationTask.CheckPressure)){
				sim_pre_zone1 = sim_pre_zone1 + now_capacity.get("Zone1");
				sim_pre_zone2 = sim_pre_zone2 + now_capacity.get("Zone2");
				sim_pre_zone3 = sim_pre_zone3 + now_capacity.get("Zone3");
			}
			
		} // end while
	}// end doSimulation

	private static String findServer(SimulationTask task) {
		for (String e : ContentMap.keySet()) {
			if (e != task.TaskFrom) {
				if (ContentMap.get(e).contains(task.TaskContnet) && now_capacity.get(e) > 0)
					return e;
			}
		}

		return null;
	}
	
	public static void analysis(){
		System.out.println("开始统计！");
		
		HashMap<String,Double> sum_delay = new HashMap<>();
		HashMap<String,Integer> sum_count = new HashMap<>();
		
		sum_count.put("Zone1", 0);
		sum_count.put("Zone2", 0);
		sum_count.put("Zone3", 0);
		sum_delay.put("Zone1", 0.0);
		sum_delay.put("Zone2", 0.0);
		sum_delay.put("Zone3", 0.0);
		
		for(SimulationTask task:history){
			int count = sum_count.get(task.TaskFrom);
			count++;
			sum_count.put(task.TaskFrom,count);
			
			if(task.TaskFrom.equals(task.TaskServer)){
				double val = sum_delay.get(task.TaskFrom);
				val=val+L1;
				sum_delay.put(task.TaskFrom,val);
				
				continue;
			}
			
			if(task.TaskServer.equals("Original")){
				double val = sum_delay.get(task.TaskFrom);
				val=val+L3;
				sum_delay.put(task.TaskFrom,val);
				
				continue;
			}
			
			double val = sum_delay.get(task.TaskFrom);
			val=val+L2;
			sum_delay.put(task.TaskFrom,val);
			
		}
		
		//-------------------------------------------------
		System.out.println("Zone1:"+sum_delay.get("Zone1")/sum_count.get("Zone1"));
		System.out.println("Zone2:"+sum_delay.get("Zone2")/sum_count.get("Zone2"));
		System.out.println("Zone3:"+sum_delay.get("Zone3")/sum_count.get("Zone3"));
		
		average_delay = (sum_delay.get("Zone1")+sum_delay.get("Zone2")+sum_delay.get("Zone3"))
				/
				(sum_count.get("Zone1")+sum_count.get("Zone2")+sum_count.get("Zone3"));
		
		System.out.println(average_delay);
		
		//------------------------------------------------
		System.out.println("----------------------------");
		System.out.println("pre_left_zone1:"+sim_pre_zone1/21600);
		System.out.println("pre_left_zone1:"+sim_pre_zone2/21600);
		System.out.println("pre_left_zone1:"+sim_pre_zone3/21600);
		
	}
	
	public static void generateMap(int[] partone,int total){
		
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
		
		
		// ----------------设置第一部分-----------------------
		for (int i = 1; i <= 3; i++) {
			HashSet<String> zone = ContentMap.get("Zone" + i);
			double sum = 0;
			for (int j = 0; j < partone[i]; j++) {
				zone.add(content_rank.get(j));
				sum = sum + Content_like.get(content_rank.get(j));
			}
			pressure.put("Zone" + i, sum*speed.get("Zone"+i)*2.9);
		}
		
		int point = Math.max(Math.max(partone[1], partone[2]),partone[3]);
		String target = null;
		while((target=findsaveserver(total))!=null){
			ContentMap.get(target).add(content_rank.get(point));
			double pre = pressure.get(target);
			pre = pre + Content_like.get(content_rank.get(point)) * 
					(speed.get("Zone1")*4.2+speed.get("Zone2")*4.2+speed.get("Zone3")*4.2-speed.get(target)*1.3);
			pressure.put(target, pre);

			point++;			
		}
		
		System.out.println("-------------------------");
		System.out.println("pressure-zone1:"+pressure.get("Zone1")/like_point.lastKey());
		System.out.println("pressure-zone2:"+pressure.get("Zone2")/like_point.lastKey());
		System.out.println("pressure-zone3:"+pressure.get("Zone3")/like_point.lastKey());
		
	}
	
	public static String findsaveserver(int total){
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
	
	//-----------------------------------------------------------
	
	
	public static void LoadMap() throws Exception{
		BufferedReader br = new BufferedReader(new FileReader("D:\\JavaWorkSpace\\CdnPlatform\\ampl\\40_20_10_c100.txt"));
		
		String in = null;
		while((in = br.readLine())!=null){
			String[] sp = in.split("\\s+");
			int point = Integer.parseInt(sp[0].substring(1));
			
			if(sp[1].equals("1")){
				ContentMap.get("Zone1").add("C"+point);
			}
			
			if(sp[2].equals("1")){
				ContentMap.get("Zone2").add("C"+point);
			}
			
			if(sp[3].equals("1")){
				ContentMap.get("Zone3").add("C"+point);
			}
		}
	
		br.close();
	}
	
	
	public static void showMap(){
		HashMap<String,TreeSet<Integer>> show = new HashMap<>();
		show.put("Zone1", new TreeSet<Integer>());
		show.put("Zone2", new TreeSet<Integer>());
		show.put("Zone3", new TreeSet<Integer>());
		
		for(int i=1;i<=3;i++){
			HashSet<String> zoneC = ContentMap.get("Zone"+i);
			TreeSet<Integer> zoneShow = show.get("Zone"+i);
			
			for(String e:zoneC){
				zoneShow.add(Integer.parseInt(e.substring(1)));
			}
		}
		
		System.out.println("-------MapShow------");
		Iterator<Integer> it1 = show.get("Zone1").iterator();
		Iterator<Integer> it2 = show.get("Zone2").iterator();
		Iterator<Integer> it3 = show.get("Zone3").iterator();
		
		while(it1.hasNext()){
			System.out.print(it1.next()+"  ");
			System.out.print(it2.next()+"  ");
			System.out.print(it3.next()+"  ");
			
			System.out.println("");
		}
		
	}

	
	public static void loop(int x,int y,int z) throws Exception{
		MaxCapacity = 150;
		HPTest.Capacity = 150;
		
		init();
		
		// 载入喜好程度信息
		//LoadRank(1);
		LoadZipfRank();
		// 生成TASK
		Generate_task("Zone1", 1, 21600);
		Generate_task("Zone2", 2, 21600);
		Generate_task("Zone3", 3, 21600);
		addpressuremonitor(21600);
		
		
		// 生成Map
		//generateMap(new int[]{0,x,y,z},100);

//		LoadMap();
		
//		HPTest.add_rate = 2.0;//1.6;
//		HPTest.callMap();
//		HPTest.showMap();
//		ContentMap.put("Zone1", HPTest.Map.get("ZONE1"));
//		ContentMap.put("Zone2", HPTest.Map.get("ZONE2"));
//		ContentMap.put("Zone3", HPTest.Map.get("ZONE3"));
		
		Htest.LoadZipfRank();
		Htest.CalMap();
		ContentMap.put("Zone1", Htest.Map.get("ZONE1"));
		ContentMap.put("Zone2", Htest.Map.get("ZONE2"));
		ContentMap.put("Zone3", Htest.Map.get("ZONE3"));
		
		// 执行仿真
		doSimulation();

		// 统计结果
		analysis();
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		//loop(20,100); //500 capacity
		//loop(26,100); //100 capacity
		//loop(32,52);  //80 capacity
		//loop(32,32);  //70 capacity
		//loop(28,28);  //60 capacity
		loop(36,36,16);
		
		//LoadMap();
		//showMap();
		
		
//		FileWriter fout = new FileWriter("D:\\different_zipf_40_10_10_60_100.csv");
//		
//		for(int x=10;x<=100;x+=2){
//			for(int y=10;y<=100;y+=2){
//				for(int z=10;z<=100;z+=2){
//					loop(y,y,x);
//					fout.write(average_delay+",");
//					fout.flush();
//				}				
//				fout.write("\n");
//			}
//		}
//		
//		fout.close();
		
	}
}
