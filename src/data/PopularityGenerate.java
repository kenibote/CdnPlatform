package data;

import java.util.HashMap;
import java.util.TreeMap;

public class PopularityGenerate {

	// 记录内容名字前缀
	public static String contentpix = null;
	// 用于记录最基础的分布数据
	public static TreeMap<Integer, Double> ZipfRaw = new TreeMap<>();
	public static HashMap<String, Double> ContentZipfRaw = new HashMap<>();

	//
	public static int ContentNumber = 300;

	/**
	 * 在使用该类的时候，应优先调用该函数一下 接收一个参数：alpha 该方法需要Setting中content_number参数的支持
	 */
	public static void generateRaw(double alpha) {
		// 当内容数量发生变化的时候，可以重新调用该函数
		// 为以后升级做准备
		ZipfRaw.clear();

		double[] p = new double[ContentNumber + 1];
		double sum = 0;

		for (int i = 1; i <= ContentNumber; i++) {
			p[i] = 1 / Math.pow(i, alpha);
			sum = sum + p[i];
		}

		// 归一化
		for (int i = 1; i <= ContentNumber; i++) {
			ZipfRaw.put(i, p[i] / sum);
			ContentZipfRaw.put("C"+i, p[i] / sum);
		}

	}

	

}
