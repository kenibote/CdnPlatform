package data;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class LocalServer {

	// 本地服务器的名字
	public String Local_Server_Name = null;

	// 用于设置该服务器可同时服务的最大用户数
	public int Max_Request_Capacity = 0;
	// 用于记录当前正在服务的用户数
	public int Current_Server_Request = 0;
	// 用于记录最高峰的时候，同时服务的用户数
	public int Indeed_Max_Request = 0;
	// 本地用户的到达率
	public double Arrival_Rate = 0;

	// 本地服务器存储空间的容量
	public int Storage_Space = 0;
	//
	public int current_storage = 0;
	// 用于存储内容
	public HashSet<String> Cache = new HashSet<>();

	// 用于记录该地区用户的喜好程度，对内容 （正序的内容ID，流行度）
	public HashMap<String, Double> Popularity = new HashMap<>();
	// 该地区用户喜好程度的排序，注意该顺序是从小到达，使用时候需要取得逆序
	private TreeMap<Double, String> Popularity_Rank = new TreeMap<>();
	// 用于产生随机内容请求时候所需要的指针序列
	private TreeMap<Double, String> Popularity_Point = new TreeMap<>();

	// 记录Task ID 为以后拓展程序做准备
	public int TaskID = 0;
	// 设置随机数种子
	public int Random_Seed = 0;
	// 计算当前可能的处理能力
	public double current_analysis_request = 0;

	// 构造器,通过构造器可以防止漏掉传递的参数
	public LocalServer(String Name, int MaxRC, double ArrivalR, int StorageS, int RandomS) {
		this.Local_Server_Name = Name;
		this.Max_Request_Capacity = MaxRC;
		this.Arrival_Rate = ArrivalR;
		this.Storage_Space = StorageS;
		this.Random_Seed = RandomS;
	}

	// 该构造器仅供测试使用
	private LocalServer() {

	}

	/**
	 * 删除该方案：产生用户请求需求 需要Setting中仿真时间的支持
	 * 更改为新方案：由用户指定起始仿真时间与结束仿真时间，有利于以后程序升级，只需要从新设定Popularity即可
	 * 该函数根据Popularity先整理出Popularity_Rank，再整理出Popularity_Point
	 * 然后通过随机函数，由到达率产生任务Task，加入到Task_Local_List列表中
	 * 
	 * @throws Exception
	 */
	public void GenerateRequest(double current_time, double end_time) throws Exception {
		// 新特性，为以后拓展程序做准备
		Popularity_Rank.clear();
		Popularity_Point.clear();

		// 根据Popularity先整理出Popularity_Rank
		// 再整理出Popularity_Point
		double pointsum = 0;
		Set<String> key = Popularity.keySet();
		for (String e : key) {
			Popularity_Rank.put(Popularity.get(e), e);

			pointsum = pointsum + Popularity.get(e);
			Popularity_Point.put(pointsum, e);
		}

		// 设置随机数种子
		randomfunction rndfunction = new randomfunction();
		Random rnd_time = new Random(Random_Seed);

		String path = "D:\\loadtask-" + Random_Seed + ".csv";
		FileWriter fout = new FileWriter(path);

		// 记录当前时间
		while (current_time <= end_time) {
			// 产生时间间隙
			double time = 1 / Arrival_Rate * rndfunction.expdev(rnd_time);
			current_time = current_time + time;
			// 产生请求内容
			double point = Math.random();
			String content = Popularity_Point.tailMap(point, true).firstEntry().getValue();
			// 更新task id
			TaskID++;

			int wait = (int) (time * 1000);
			String output = wait + "," + "192.168.1.101,8070," + TaskID + "," + content + ",\r\n";
			fout.write(output);
			// 创建新的task
			// TaskInfo task = new TaskInfo();
			// task.Task_Id = Local_Server_Name + "@" + TaskID;
			// task.Task_From = Local_Server_Name;
			// task.Task_Time = current_time;
			// task.Content_Id = content;

		} // end of while

		fout.close();
	}// end of GenerateRequest

	public static void main(String[] args) throws Exception {
		LocalServer local1 = new LocalServer("LocalServer1", 20, 4, 50, 3);
		PopularityGenerate.generateRaw(0.88);
		local1.Popularity = PopularityGenerate.ContentZipfRaw;
		local1.GenerateRequest(0, 240);
	}

}