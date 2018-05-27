package simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

public class AMPLdata {

	static int LocalSeverNumber = 3;
	static int ContentNumber = 1000;
	static int ServerSpace = 100;
	static double ComputCapacity = 80.0;
	static double Lone = 2.5, Ltwo = 7.5, Lthree = 17.5;
	static double[] ArrivalRate = { 0, 40.0, 10.0, 10.0 };

	static HashMap<String, Double> ZIPF = new HashMap<>();

	public static void LoadZipfRank() {
		System.out.println("》》开始生成zipf分布");

		double sum = 0;

		for (int i = 1; i <= ContentNumber; i++) {
			double val = 1 / Math.pow(i, 0.88);
			ZIPF.put("C" + i, val);
			sum = sum + val;
		}

		for (int i = 1; i <= ContentNumber; i++) {
			double val = ZIPF.get("C" + i);
			val = val / sum;
			ZIPF.put("C" + i, val);
		}

		System.out.println("》》开始生成zipf分布--结束");

	}

	public static void main(String[] args) throws Exception {
		File fout = new File("D:\\QuickAccessWorkFile\\Project_3_CDN\\dataset\\MixWithCData2018.dat");
		BufferedWriter write = new BufferedWriter(new FileWriter(fout));

		write.write("set SERVER := ");
		for (int count = 1; count <= LocalSeverNumber; count++) {
			write.write("S" + count + " ");
		}
		write.write(";\r\n");

		write.write("set CONTENT := \r\n");
		for (int count = 1; count <= ContentNumber; count++) {
			write.write("C" + count + " ");
			if (count % 10 == 0)
				write.write("\r\n");
		}
		write.write(";\r\n");

		write.write("param Lone := " + Lone + " ; \r\n");
		write.write("param Ltwo := " + Ltwo + " ; \r\n");
		write.write("param Lthree := " + Lthree + " ; \r\n");
		write.write("param ServerNumber :=" + LocalSeverNumber + " ; \r\n");
		write.write("param ComputCapacity := " + ComputCapacity + " ; \r\n");
		write.write("param Delta := 1000 ; \r\n");

		write.write("param ArrivalRate := \r\n");
		for (int i = 1; i <= LocalSeverNumber; i++) {
			write.write("[S" + i + "] " + ArrivalRate[i] + " \r\n");
		}
		write.write(";\r\n");

		write.write("param ServerSpace :=\r\n");
		for (int i = 1; i <= LocalSeverNumber; i++) {
			write.write("[S" + i + "] " + ServerSpace + " \r\n");
		}
		write.write(";\r\n");

		write.write("param ContentSpace := \r\n");
		for (int i = 1; i <= ContentNumber; i++) {
			write.write("[C" + i + "] 1 \r\n");
		}
		write.write(";\r\n");

		LoadZipfRank();
		write.write("param Popularity := \r\n");
		for (int s = 1; s <= LocalSeverNumber; s++) {
			for (int i = 1; i <= ContentNumber; i++) {
				write.write("[S" + s + ",C" + i + "] " + ZIPF.get("C" + i) + "  \r\n");
			}
		}
		write.write(";\r\n");

		write.flush();
		write.close();

	}
}
