package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MMCNdata {
	public static HashMap<String, Integer> Content = new HashMap<>();
	public static HashSet<String> User = new HashSet<>();

	/**
	 * 载入数据，统计每个内容出现的次数
	 */
	public static void LoadFile() throws Exception {
		String FilePath = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-3.dat";

		BufferedReader fin = new BufferedReader(new FileReader(FilePath));
		String line = null;
		while ((line = fin.readLine()) != null) {
			String[] info = line.split(" ");

			if (Content.containsKey(info[4])) {
				int val = Content.get(info[4]) + 1;
				Content.put(info[4], val);
			} else {
				Content.put(info[4], 1);
			}

		}

		fin.close();

		String saveFile = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-3.csv";
		FileWriter fout = new FileWriter(saveFile);
		Iterator<String> it = Content.keySet().iterator();
		while (it.hasNext()) {
			String id = it.next();
			int val = Content.get(id);
			fout.write(id + "," + val + ",\r\n");
		}

		fout.close();

	}

	/**
	 * 对每个内容进行timeslot统计
	 */
	public static void analy() throws Exception {
		HashMap<String, ArrayList<Integer>> TimeList = new HashMap<>();
		HashMap<String, Integer> slot = new HashMap<>();

		// 只保留约前10000的数据
		Set<String> set = Content.keySet();
		for (String e : set) {
			if (Content.get(e) >= 0) {
				ArrayList<Integer> timelist = new ArrayList<>();
				TimeList.put(e, timelist);
				slot.put(e, 0);
			}
		}

		// 开始统计
		String FilePath = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-3.dat";
		BufferedReader fin = new BufferedReader(new FileReader(FilePath));
		String line = null;
		long time = 1201639675 + 60 * 60 * 24;
		while ((line = fin.readLine()) != null) {
			String[] info = line.split(" ");

			long now = Long.parseLong(info[0].substring(0, 10));
			if (now > time) {
				time = time + 60 * 60 * 24;

				Set<String> slot_set = slot.keySet();
				for (String e : slot_set) {
					// 将每个内容写入历史数据中
					TimeList.get(e).add(slot.get(e));
					// 同时清除当前
					slot.put(e, 0);
				}

			} else {
				// 如果在当前时间窗格中
				if (slot.containsKey(info[4])) {
					// 如果在需要记录的列表中
					int val = slot.get(info[4]) + 1;
					slot.put(info[4], val);
				}
			}
		}
		fin.close();

		// 防止漏掉最后一个时间戳
		Set<String> slot_set = slot.keySet();
		for (String e : slot_set) {
			// 将每个内容写入历史数据中
			TimeList.get(e).add(slot.get(e));
			// 同时清除当前
			slot.put(e, 0);
		}

		// 输出统计结果
		String saveFile = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-3-timeSlot.csv";
		FileWriter fout = new FileWriter(saveFile);
		Set<String> timelistset = TimeList.keySet();
		for (String e : timelistset) {
			ArrayList<Integer> cache = TimeList.get(e);

			fout.write(e + ",");

			for (int count : cache) {
				fout.write(count + ",");
			}

			fout.write(Content.get(e) + ",\r\n");
		}

		fout.close();
	}

	/**
	 * 时间轴分析，主要统计到达率问题
	 */
	public static void timeline() throws Exception {
		ArrayList<Integer> TreeCount = new ArrayList<>();
		int count = 0;

		// 开始统计
		String FilePath = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-3.dat";
		BufferedReader fin = new BufferedReader(new FileReader(FilePath));
		String line = null;
		long time = 1201639675 + 60 * 60;
		while ((line = fin.readLine()) != null) {
			String[] info = line.split(" ");

			long now = Long.parseLong(info[0].substring(0, 10));
			if (now > time) {
				time = time + 60 * 60;

				TreeCount.add(count);
				count = 1;

			} else {
				// 如果在当前时间窗格中
				count++;
			}
		}

		TreeCount.add(count);
		fin.close();

		// 输出统计结果
		String saveFile = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-3-timeline.csv";
		FileWriter fout = new FileWriter(saveFile);
		for (Integer e : TreeCount) {

			fout.write(e + ",\r\n");
		}

		fout.close();

	}

	/**
	 * 根据IP地质拆分数据
	 */
	public static void splitdata() throws Exception {
		String FilePath = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908.dat";

		BufferedReader fin = new BufferedReader(new FileReader(FilePath));
		String line = null;
		while ((line = fin.readLine()) != null) {
			String[] info = line.split(" ");

			if (User.contains(info[2])) {

			} else {
				User.add(info[2]);
			}

		}

		fin.close();
		System.out.println("User:" + User.size());

		// ----------------------------------------------------------
		HashMap<Integer, HashSet<String>> UserMap = new HashMap<>();
		UserMap.put(1, new HashSet<>());
		UserMap.put(2, new HashSet<>());
		UserMap.put(3, new HashSet<>());
		int point = 1;

		for (String e : User) {
			UserMap.get(point).add(e);
			point++;
			if (point == 4)
				point = 1;
		}

		System.out.println("User1:" + UserMap.get(1).size());
		System.out.println("User2:" + UserMap.get(2).size());
		System.out.println("User3:" + UserMap.get(3).size());

		// ----------------------------------------------------------
		String saveFile1 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-1.dat";
		String saveFile2 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-2.dat";
		String saveFile3 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-3.dat";
		FileWriter fout1 = new FileWriter(saveFile1);
		FileWriter fout2 = new FileWriter(saveFile2);
		FileWriter fout3 = new FileWriter(saveFile3);

		fin = new BufferedReader(new FileReader(FilePath));
		line = null;
		while ((line = fin.readLine()) != null) {
			String[] info = line.split(" ");

			if (UserMap.get(1).contains(info[2])) {
				fout1.write(line + "\r\n");
			}

			if (UserMap.get(2).contains(info[2])) {
				fout2.write(line + "\r\n");
			}

			if (UserMap.get(3).contains(info[2])) {
				fout3.write(line + "\r\n");
			}

		}

		fin.close();
		fout1.close();
		fout2.close();
		fout3.close();

	}

	/**
	 * 生成AI测试集
	 */
	public static void splitdatagenerateAIdata() throws Exception {
		String FilePath1 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-1-timeSlot.csv";
		String FilePath2 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-2-timeSlot.csv";
		String FilePath3 = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-3-timeSlot.csv";
		String SavePath = "D:\\DataSet\\MMCN\\012908\\youtube.parsed.012908-split-process10-test3.csv";

		ArrayList<String> FilePath = new ArrayList<>();
		// FilePath.add(FilePath1);
		// FilePath.add(FilePath2);
		FilePath.add(FilePath3);

		FileWriter fout = new FileWriter(SavePath);

		for (String e : FilePath) {
			BufferedReader fin = new BufferedReader(new FileReader(e));

			String line = null;
			while ((line = fin.readLine()) != null) {
				String[] info = line.split(",");

				for (int i = 3; i <= 12; i++) {
					fout.write(info[i] + ",");
				}
				fout.write("\r\n");

			}

			fin.close();
		}

		fout.close();

	}

	public static void main(String[] args) throws Exception {

		// timeline();
		// LoadFile();
		// analy();
		// splitdata();
		splitdatagenerateAIdata();
	}

}
