package simulation;

import java.util.*;

public class Htest {
	static int TOTAL_CONTENT = 1000;
	static int TOTAL_ZONE = 3;
	static int ZONE_SPACE = 100;

	static double H1 = 40, H2 = 10, H3 = 10;
	static double L1 = 2.0, L2 = 7.5, L3 = 17.5;
	static HashMap<String, Double> ZIPF = new HashMap<>();
	static HashMap<String, HashSet<String>> Map = new HashMap<>();

	static {
		Map.put("ZONE1", new HashSet<>());
		Map.put("ZONE2", new HashSet<>());
		Map.put("ZONE3", new HashSet<>());
	}

	public static void LoadZipfRank() {
		System.out.println("》》开始生成zipf分布");

		double sum = 0;

		for (int i = 1; i <= TOTAL_CONTENT; i++) {
			double val = 1 / Math.pow(i, 0.88);
			ZIPF.put("C" + i, val);
			sum = sum + val;
		}

		for (int i = 1; i <= TOTAL_CONTENT; i++) {
			double val = ZIPF.get("C" + i);
			val = val / sum;
			ZIPF.put("C" + i, val);
		}

		System.out.println("》》开始生成zipf分布--结束");

	}

	public static void CalMap() {

		int point = FirstPart();

		while (Map.get("ZONE1").size() < ZONE_SPACE || Map.get("ZONE2").size() < ZONE_SPACE
				|| Map.get("ZONE3").size() < ZONE_SPACE) {
			
			if (Map.get("ZONE1").size() < ZONE_SPACE) {
				Map.get("ZONE1").add("C" + point);
			} else if (Map.get("ZONE2").size() < ZONE_SPACE) {
				Map.get("ZONE2").add("C" + point);
			} else {
				Map.get("ZONE3").add("C" + point);
			}

			point++;
		}

		System.out.println(">>内容安排结束！！！");
	}

	public static int FirstPart() {
		// 起初假设是每个内容只放1份。
		int point = 1;
		int b = 299;
		int c = 300;

		while (true) {

			double v1 = 0, v2 = 0, v3 = 0;

			v1 = H1 * ZIPF.get("C" + point) * L1 + (H2 + H3) * ZIPF.get("C" + point) * L2 + H2 * ZIPF.get("C" + b) * L1
					+ (H1 + H3) * ZIPF.get("C" + b) * L2 + (H1 + H2) * ZIPF.get("C" + c) * L2
					+ H3 * ZIPF.get("C" + c) * L1;

			v2 = (H1 + H2) * ZIPF.get("C" + point) * L1 + H3 * ZIPF.get("C" + point) * L2 + H3 * ZIPF.get("C" + b) * L1
					+ (H1 + H2) * ZIPF.get("C" + b) * L2 + (H1 + H2 + H3) * ZIPF.get("C" + c) * L3;

			v3 = (H1 + H2 + H3) * ZIPF.get("C" + point) * L1 + (H1 + H2 + H3) * ZIPF.get("C" + b) * L3
					+ (H1 + H2 + H3) * ZIPF.get("C" + c) * L3;

			System.out.println(v1);
			System.out.println(v2);
			System.out.println(v3);

			TreeMap<Double, String> rank = new TreeMap<>();
			rank.put(v1, "case1");
			rank.put(v2, "case2");
			rank.put(v3, "case3");

			if (rank.firstEntry().getValue().equals("case1")) {
				return point;
			}

			if (rank.firstEntry().getValue().equals("case2")) {
				Map.get("ZONE1").add("C" + point);
				Map.get("ZONE2").add("C" + point);

				point++;
				b -= 1;
				c -= 1;
			}

			if (rank.firstEntry().getValue().equals("case3")) {
				Map.get("ZONE1").add("C" + point);
				Map.get("ZONE2").add("C" + point);
				Map.get("ZONE3").add("C" + point);

				point++;
				b -= 2;
				c -= 2;
			}

		}

	}

	
	public static void showMap(){
		HashMap<String,TreeSet<Integer>> show = new HashMap<>();
		show.put("ZONE1", new TreeSet<Integer>());
		show.put("ZONE2", new TreeSet<Integer>());
		show.put("ZONE3", new TreeSet<Integer>());
		
		for(int i=1;i<=TOTAL_ZONE;i++){
			HashSet<String> zoneC = Map.get("ZONE"+i);
			TreeSet<Integer> zoneShow = show.get("ZONE"+i);
			
			for(String e:zoneC){
				zoneShow.add(Integer.parseInt(e.substring(1)));
			}
		}
		
		System.out.println("-------MapShow------");
		Iterator<Integer> it1 = show.get("ZONE1").iterator();
		Iterator<Integer> it2 = show.get("ZONE2").iterator();
		Iterator<Integer> it3 = show.get("ZONE3").iterator();
		
		while(it1.hasNext()){
			System.out.print(it1.next()+"  ");
			System.out.print(it2.next()+"  ");
			System.out.print(it3.next()+"  ");
			
			System.out.println("");
		}
		
	}
	
	public static void main(String[] args) {
		LoadZipfRank();
		CalMap();
		showMap();
		
	}
}
