package simulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import data.randomfunction;

public class SanjayUpdata {
	public static HashMap<String, Double> History_like = new HashMap<>();
	public static HashMap<String, Double> Popularity = new HashMap<>();

	// 该地区用户喜好程度的排序，注意该顺序是从小到达，使用时候需要取得逆序
	private static TreeMap<Double, String> Popularity_Rank = new TreeMap<>();
	// 用于产生随机内容请求时候所需要的指针序列
	private static TreeMap<Double, String> Popularity_Point = new TreeMap<>();

	public static void LoadHistoryRank(int day) throws Exception {
		System.out.println("正在导入历史喜好程度数据！");

		String filePath = "D:\\QuickAccessWorkFile\\Project_3_CDN\\dataset\\YouTube\\outclick.csv";

		BufferedReader fin = new BufferedReader(new FileReader(filePath));
		String line = null;
		double sum = 0.0;
		for (int i = 1; i <= 5000; i++) {
			line = fin.readLine();
			String[] info = line.split(",");

			History_like.put("C" + i, Double.parseDouble(info[day]));
			sum = sum + Double.parseDouble(info[day]);
		} // end for fin

		fin.close();

		// 计算历史喜好程度
		for (String e : History_like.keySet()) {
			double val = History_like.get(e);
			val = val / sum;
			History_like.put(e, val);
		}
		System.out.println("C1:" + History_like.get("C1"));
		System.out.println("历史喜好程度数据导入结束！");
	}

	public static void LoadTodayLike(int day, int contentid, int click) throws Exception {
		System.out.println("正在导入目前喜好程度数据！");

		String filePath = "D:\\QuickAccessWorkFile\\Project_3_CDN\\dataset\\YouTube\\outclick.csv";

		BufferedReader fin = new BufferedReader(new FileReader(filePath));
		String line = null;
		double sum = 0.0;
		for (int i = 1; i <= 5000; i++) {
			line = fin.readLine();
			String[] info = line.split(",");

			Popularity.put("C" + i, Double.parseDouble(info[day]));
			sum = sum + Double.parseDouble(info[day]);
		} // end for fin

		fin.close();

		// 在此处修改目标id的喜好程度
		if (contentid > 0) {
			sum = sum - Popularity.get("C" + contentid);
			sum = sum + click;
			Popularity.put("C" + contentid, (double) click);
		}

		// 计算当前喜好程度
		for (String e : Popularity.keySet()) {
			double val = Popularity.get(e);
			val = val / sum;
			Popularity.put(e, val);
		}
		System.out.println("C1:" + Popularity.get("C1"));
		System.out.println("目前喜好程度数据导入结束！");

		if (contentid > 0) {
			System.out.println("--------------------------------");
			System.out.println("监测数据信息：  C" + contentid + ": " + Popularity.get("C" + contentid));
			System.out.println("--------------------------------");
		}
	}

	public static void GenerateRequest(double current_time, double end_time, double Arrival_Rate, int contentid)
			throws Exception {
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
		Random rnd_time = new Random(1);

		int time_point=0;
		
		// 记录当前时间
		while (current_time <= end_time) {
			// 产生时间间隙
			//if ((int) current_time % 100 == 1)
			//	System.out.println(current_time);

			double time = 1 / Arrival_Rate * rndfunction.expdev(rnd_time);
			current_time = current_time + time;
			// 产生请求内容
			double point = Math.random();
			String content = Popularity_Point.tailMap(point, true).firstEntry().getValue();
			// 更新task id

			// 更新喜好程度 5000-->0.004 1000-->0.01
			double up = History_like.get(content);
			up = (up * 5000 + 0.003) / 5000.003;

			for (String e : History_like.keySet()) {
				double down = History_like.get(e);
				down = down * 5000 / 5000.003;
				History_like.put(e, down);
			}

			History_like.put(content, up);

			// 输出监测数据
			if (contentid > 0) {
				// 每隔5分钟观测一次
				if ((int)current_time / 60 == time_point) {
					System.out.println(History_like.get("C"+contentid));
					time_point++;
				}
			}

		} // end of while

		// String filePath =
		// "D:\\QuickAccessWorkFile\\Project_3_CDN\\dataset\\YouTube\\Sanjay.csv";
		// FileWriter fout = new FileWriter(filePath);
		// for (int i = 1; i <= 5000; i++) {
		// fout.write("C" + i + "," + History_like.get("C" + i) + ",\r\n");
		// }
		// fout.close();

	}// end of GenerateRequest

	public static void main(String[] args) throws Exception {
		LoadHistoryRank(10);
		LoadTodayLike(10, 8, 1234567);
		GenerateRequest(0, 4 * 3600, 40, 9);
	}
}
