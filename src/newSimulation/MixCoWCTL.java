package newSimulation;

import java.util.*;

public class MixCoWCTL {
	public static HashMap<String, HashSet<String>> Map = new HashMap<>();

	public static void MainFunction() {
		int first_a = 0, first_b = 0, first_c = 0;

		while (true) {

			TreeMap<Double, String> evalution = new TreeMap<>();
			evalution.put(subMap(first_a + 1, first_b, first_c), "case1");
			evalution.put(subMap(first_a + 1, first_b + 1, first_c), "case2");
			evalution.put(subMap(first_a + 1, first_b + 1, first_c + 1), "case3");

			if (evalution.firstEntry().getValue().equals("case1")) {
				break;
			}

			if (evalution.firstEntry().getValue().equals("case2")) {
				System.out.println("CASE2");
				first_a++;
				first_b++;
			}

			if (evalution.firstEntry().getValue().equals("case3")) {
				System.out.println("CASE3");
				first_a++;
				first_b++;
				first_c++;
			}

		}
		
		subMap(first_a, first_b, first_c);

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
		for (int c = 1; c <= PublicSetting.StorageSpace; c++) {
			for (int i = 1; i <= 3; i++)
				System.out.print(show.get("Zone" + i).next() + "  ");

			System.out.println("");
		}

	}

	public static double subMap(int first_a, int first_b, int first_c) {
		// 先清理Map
		Map.clear();

		// 用于记录已经被放过的内容
		HashSet<String> mark = new HashSet<>();

		int[] first = new int[4];
		first[1] = first_a;
		first[2] = first_b;
		first[3] = first_c;

		// 先根据a,b,c 的安排第一部分内容
		for (int i = 1; i <= 3; i++) {
			HashSet<String> localmap = new HashSet<>();
			ArrayList<String> localrank = PublicSetting.LocalRank.get("Zone" + i);

			for (int r = 1; r <= first[i]; r++) {
				localmap.add(localrank.get(r));
				mark.add(localrank.get(r));
			}

			Map.put("Zone" + i, localmap);
		}

		// 之后放置第二部分
		// 当还有空间的时候
		while (hasStorageSpace()) {
			// 先找到下一个要放的内容
			int point = GetNextGlobalPoint(mark);
			String content_id = PublicSetting.GlobalRank.get(point);

			TreeMap<Double, String> candidate = new TreeMap<>(Collections.reverseOrder());
			for (int i = 1; i <= 3; i++) {
				if (Map.get("Zone" + i).size() < PublicSetting.StorageSpace) {
					double val = PublicSetting.ArrivalRate.get("Zone" + i)
							* PublicSetting.Popularity.get("Zone" + i).get(content_id);

					candidate.put(val, "Zone" + i);
				}
			}

			String target = candidate.firstEntry().getValue();
			Map.get(target).add(content_id);

			// 更新mark
			mark.add(content_id);
		}

		// 计算可能的时延
		return evaluateLatency(mark);
	}

	// 评估当前Map下的平均时延
	private static double evaluateLatency(HashSet<String> mark) {
		double result = 0.0;

		for (int c = 1; c <= PublicSetting.ContentNumber; c++) {
			for (int i = 1; i <= 3; i++) {
				double arr_rate = PublicSetting.ArrivalRate.get("Zone" + i);
				double p = PublicSetting.Popularity.get("Zone" + i).get("C" + c);

				// 如果本地有该内容
				if (Map.get("Zone" + i).contains("C" + c)) {
					result = result + arr_rate * p * PublicSetting.L1;
				} else {
					if (mark.contains("C" + c)) {
						result = result + arr_rate * p * PublicSetting.L2;
					} else {
						result = result + arr_rate * p * PublicSetting.L3;
					}
				}

			}
		}

		return result;
	}

	private static boolean hasStorageSpace() {
		for (int i = 1; i <= 3; i++) {
			if (Map.get("Zone" + i).size() < PublicSetting.StorageSpace)
				return true;
		}

		return false;
	}

	private static int GetNextGlobalPoint(HashSet<String> mark) {
		int gr = 1;

		while (gr < PublicSetting.ContentNumber) {
			if (mark.contains(PublicSetting.GlobalRank.get(gr))) {
				gr++;
			} else {
				break;
			}
		}

		return gr;
	}

	public static void main(String[] args) {
		PublicSetting.GenerateZipf(0.88);
		PublicSetting.PutPopularitySame();
		//-----------------------------
		PublicSetting.ChangePopularity(2);
		PublicSetting.ChangePopularity(3);
		//-----------------------------
		PublicSetting.CalculateLikePoint();
		PublicSetting.CalculateLocalRank();
		PublicSetting.CalculateGoRank();
		
		MainFunction();
		ShowMap(Map);
	}
}
