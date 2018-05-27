package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SFUdata {

	public static void recollect() throws Exception {
		String pix = "080727";
		String filePath = "D:\\DataSet\\SFU\\" + pix + "\\";
		FileWriter fout = new FileWriter(filePath + pix + ".csv");

		HashSet<String> Content = new HashSet<>();

		for (int i = 0; i <= 3; i++) {
			BufferedReader fin = new BufferedReader(new FileReader(filePath + i + ".txt"));
			String line = null;
			while ((line = fin.readLine()) != null) {
				String[] info = line.split("\t");
				if (info.length >= 6) {
					fout.write(info[0] + " ," + info[3] + "," + info[5] + ",\r\n");
					Content.add(info[0]);
				}
			}

			fin.close();
		}

		fout.close();
		System.out.println("Total Content:" + Content.size());
	}

	public static void unitone() throws Exception {
		ArrayList<String> pix = new ArrayList<>();
		pix.add("080609");
		pix.add("080611");
		pix.add("080613");
		pix.add("080615");
		pix.add("080617");
		pix.add("080619");
		pix.add("080621");
		pix.add("080623");
		pix.add("080625");
		pix.add("080627");
		pix.add("080629");
		pix.add("080701");
		pix.add("080703");
		pix.add("080705");
		pix.add("080707");
		pix.add("080709");
		pix.add("080711");
		pix.add("080713");
		pix.add("080715");
		pix.add("080717");
		pix.add("080719");
		pix.add("080721");
		pix.add("080723");
		pix.add("080725");
		pix.add("080727");
		String filePath = "D:\\DataSet\\SFU\\";

		HashMap<String, String> content_type = new HashMap<>();
		HashMap<String, ArrayList<Integer>> content_slot = new HashMap<>();

		for (String e : pix) {
			BufferedReader fin = new BufferedReader(new FileReader(filePath + e + "\\" + e + ".csv"));
			String line = null;

			while ((line = fin.readLine()) != null) {
				String[] info = line.split(",");
				if (content_type.containsKey(info[0])) {
					// 如果有该内容
					content_slot.get(info[0]).add(Integer.parseInt(info[2]));
				} else {
					// 如果没有该内容
					content_type.put(info[0], info[1]);
					content_slot.put(info[0], new ArrayList<Integer>());

					content_slot.get(info[0]).add(Integer.parseInt(info[2]));
				}
			}

			fin.close();
		} // end for

		// 输出文件
		FileWriter fout = new FileWriter(filePath + "unitone.csv");
		for (String e : content_type.keySet()) {
			ArrayList<Integer> slot = content_slot.get(e);

			fout.write(e + ",");
			fout.write(content_type.get(e) + ",");

			for (Integer a : slot) {
				fout.write(a + ",");
			}

			fout.write("\r\n");

		}

		fout.close();
	}

	public static void generateAIdata(int start, int end, int total) throws Exception {
		String filePath = "D:\\QuickAccessWorkFile\\Project_3_CDN\\dataset\\YouTube\\";

		BufferedReader fin = new BufferedReader(new FileReader(filePath + "outclick-quzao.csv"));
		FileWriter fout = new FileWriter(filePath + "test-5.csv");

		String line = null;
		for (int i = 1; i <= total; i++) {
			line = fin.readLine();
			String[] info = line.split(",");

			for (int j = start; j <= end; j++)
				fout.write(info[j] + ",");

			fout.write("\r\n");
		}

		fin.close();
		fout.close();

	}

	public static void main(String[] args) throws Exception {
		// recollect();
		// unitone();

		// 以上2个函数所处理的数据已被证实没有用
		generateAIdata(5, 9, 20000);
	}
}
