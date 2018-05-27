package data;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class Generate {

	/**
	 * 用于生成内容文件
	 */
	public static void ContentGenerate() throws Exception {

		byte[] buf = new byte[1024];
		for (int i = 0; i < 1024; i++) {
			buf[i] = (byte) i;
		}

		for (int i = 1; i <= 300; i++) {

			DataOutputStream fileOut = new DataOutputStream(
					new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream("E:\\Content\\C" + i))));

			for (int j = 1; j <= 256; j++) {
				fileOut.write(buf, 0, 1024);
			}

			fileOut.close();
		}
	}

	// 当各个地区喜好程度，存储容量，到达率都相同的时候
	public static void ContentMapGenerate(int com, int total) throws Exception {
		HashMap<String, ArrayList<String>> map = new HashMap<>();
		ArrayList<String> list1 = new ArrayList<>();
		ArrayList<String> list2 = new ArrayList<>();
		ArrayList<String> list3 = new ArrayList<>();

		map.put("LocalServer1", list1);
		map.put("LocalServer2", list2);
		map.put("LocalServer3", list3);

		// 产生zipf分布
		PopularityGenerate.ContentNumber = 300;
		PopularityGenerate.generateRaw(0.88);

		// 用于记录当前压力和
		HashMap<String, Double> pres = new HashMap<>();
		pres.put("LocalServer1", 0.0);
		pres.put("LocalServer2", 0.0);
		pres.put("LocalServer3", 0.0);

		for (String e : map.keySet()) {
			ArrayList<String> ee = map.get(e);
			double y = 0;
			for (int i = 1; i <= com; i++) {
				ee.add("C" + i);
				y = y + PopularityGenerate.ZipfRaw.get(i);
			}

			pres.put(e, y);
		}

		// -----------------------------------------------------
		int point = com + 1;
		while ((list1.size() != total) || (list2.size() != total) || (list3.size() != total)) {
			TreeMap<Double, String> tree = new TreeMap<>();
			// 先找到候选名单
			if (list1.size() < total)
				tree.put(pres.get("LocalServer1"), "LocalServer1");
			if (list2.size() < total)
				tree.put(pres.get("LocalServer2"), "LocalServer2");
			if (list3.size() < total)
				tree.put(pres.get("LocalServer3"), "LocalServer3");

			// 找到压力最小的
			String target = tree.firstEntry().getValue();
			// 更新内容
			map.get(target).add("C" + point);
			double newpres = pres.get(target) + PopularityGenerate.ZipfRaw.get(point);
			pres.put(target, newpres);

			// 更新指针
			point++;
		}

		// -----------------------------------------------------
		// TODO 以下代码今后可能需要重写
		FileWriter fout = new FileWriter("D:\\Map.txt");
		
		for (String e : map.keySet()) {
			ArrayList<String> ee = map.get(e);
			String result = "";

			Iterator<String> it = ee.iterator();
			while (it.hasNext()) {
				result = result + it.next();

				if (it.hasNext())
					result = result + "-";
			}

			fout.write(e+":");
			fout.write(result+"\r\n");
		}
		
		fout.close();

	}

	public static void main(String[] args) throws Exception {
		//ContentGenerate();
		ContentMapGenerate(5,50);
	}

}
