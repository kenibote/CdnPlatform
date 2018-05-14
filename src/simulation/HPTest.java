package simulation;

import java.util.*;

public class HPTest {
	static int TOTAL_CONTENT = 1000;
	static int TOTAL_ZONE = 3;
	static int ZONE_SPACE = 100;
	static double Capacity = 100.0;

	static double L1 = 2.9, L2 = 4.2, L3 = 6.0;
	static double w1 = 1.0, w2 = 4.0, w3 = 4.0 , alpha = 1;
	static double PRESSURE1,PRESSURE2,PRESSURE3;
	
	static HashMap<String, Double> ZIPF = new HashMap<>();
	static HashMap<String, HashSet<String>> Map = new HashMap<>();
	static HashMap<String, Double> Arrival_Rate = new HashMap<>();

	static {
		Arrival_Rate.put("ZONE1", 40.0);
		Arrival_Rate.put("ZONE2", 10.0);
		Arrival_Rate.put("ZONE3", 10.0);
	}

	public static void generate_zipf() {
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
		
		sum=0;
		for(int i=1;i<=ZONE_SPACE;i++){
			sum = sum + ZIPF.get("C"+i);
		}
		PRESSURE1 = sum * Arrival_Rate.get("ZONE1") * L1;
		PRESSURE2 = sum * Arrival_Rate.get("ZONE2") * L1;
		PRESSURE3 = sum * Arrival_Rate.get("ZONE3") * L1;

		System.out.println("》》开始生成zipf分布--结束");
	}

	public static void pre_map() {
		System.out.println("》》开始生成预Map");

		for (int i = 1; i <= TOTAL_ZONE; i++) {
			HashSet<String> zone_map = new HashSet<>();

			for (int c = 1; c <= ZONE_SPACE; c++) {
				zone_map.add("C" + c);
			}

			Map.put("ZONE" + i, zone_map);
		}

		System.out.println("》》开始生成预Map--结束");
	}

	public static void reMap() {
		int point = ZONE_SPACE;
		int b = ZONE_SPACE + 1;
		int c = ZONE_SPACE + 2;

		// 当符合跳出条件时，自动终止循环
		while (true) {
			System.out.println("-------POINT:"+point+"------");
			
			// 计算之前的平均时延  和  每个服务器的平均压力
			double before_latency = (Arrival_Rate.get("ZONE1") + Arrival_Rate.get("ZONE2") + Arrival_Rate.get("ZONE3"))
					* ZIPF.get("C" + point) * L1
					+ (Arrival_Rate.get("ZONE1") + Arrival_Rate.get("ZONE2") + Arrival_Rate.get("ZONE3"))
							* (ZIPF.get("C" + b) + ZIPF.get("C" + c)) * L3;

			double pressure_1 = Arrival_Rate.get("ZONE1")*ZIPF.get("C" + point)*L1;
			double pressure_2 = Arrival_Rate.get("ZONE2")*ZIPF.get("C" + point)*L1;
			double pressure_3 = Arrival_Rate.get("ZONE3")*ZIPF.get("C" + point)*L1;
			
			//w1 = Capacity - PRESSURE1;
			//w2 = Capacity - PRESSURE2;
			//w3 = Capacity - PRESSURE3;
			
			System.out.println("PRESSURE1:"+PRESSURE1);
			System.out.println("PRESSURE2:"+PRESSURE2);
			System.out.println("PRESSURE3:"+PRESSURE3);
			
			// 换最高的
			double latency_case_1 = 
					 Arrival_Rate.get("ZONE1")*ZIPF.get("C"+point)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+point)*L1+ Arrival_Rate.get("ZONE3")*ZIPF.get("C"+point)*L1
					+Arrival_Rate.get("ZONE1")*ZIPF.get("C"+b)*L1 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+b)*L2 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+b)*L2
					+Arrival_Rate.get("ZONE1")*ZIPF.get("C"+c)*L3 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+c)*L3 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+c)*L3;
			
			double pres_case_1_zone_1 = Arrival_Rate.get("ZONE1")*ZIPF.get("C"+b)*L1 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+b)*L2 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+b)*L2;
			double pres_case_1_zone_2 = Arrival_Rate.get("ZONE2")*ZIPF.get("C"+point)*L1 + Arrival_Rate.get("ZONE1")*ZIPF.get("C"+point)*L2 /2;
			double pres_case_1_zone_3 = Arrival_Rate.get("ZONE3")*ZIPF.get("C"+point)*L1 + Arrival_Rate.get("ZONE1")*ZIPF.get("C"+point)*L2 /2;
		
			double delta_latency_1 = before_latency-latency_case_1;
			double pres_case_1 = w1*(pres_case_1_zone_1-pressure_1)+w2*(pres_case_1_zone_2-pressure_2)+w3*(pres_case_1_zone_3-pressure_3);
			
			// 换第2高的
			double latency_case_2 = 
					 Arrival_Rate.get("ZONE1")*ZIPF.get("C"+point)*L1 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+point)*L2+ Arrival_Rate.get("ZONE3")*ZIPF.get("C"+point)*L1
					+Arrival_Rate.get("ZONE1")*ZIPF.get("C"+b)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+b)*L1 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+b)*L2
					+Arrival_Rate.get("ZONE1")*ZIPF.get("C"+c)*L3 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+c)*L3 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+c)*L3;

			double pres_case_2_zone_1 = Arrival_Rate.get("ZONE1")*ZIPF.get("C"+point)*L1 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+point)*L2 /2;
			double pres_case_2_zone_2 = Arrival_Rate.get("ZONE1")*ZIPF.get("C"+b)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+b)*L1 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+b)*L2;
			double pres_case_2_zone_3 = Arrival_Rate.get("ZONE3")*ZIPF.get("C"+point)*L1 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+point)*L2 /2;
			
			double delta_latency_2 = before_latency-latency_case_2;
			double pres_case_2 = w1*(pres_case_2_zone_1-pressure_1)+w2*(pres_case_2_zone_2-pressure_2)+w3*(pres_case_2_zone_3-pressure_3);
			
			// 换第3高的
			

			// 换 1 & 2
			double latency_case_4 =
					 Arrival_Rate.get("ZONE1")*ZIPF.get("C"+point)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+point)*L2+ Arrival_Rate.get("ZONE3")*ZIPF.get("C"+point)*L1
					+Arrival_Rate.get("ZONE1")*ZIPF.get("C"+b)*L1 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+b)*L2 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+b)*L2
					+Arrival_Rate.get("ZONE1")*ZIPF.get("C"+c)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+c)*L1 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+c)*L2;

			
			double pres_case_4_zone_1 = Arrival_Rate.get("ZONE1")*ZIPF.get("C"+b)*L1 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+b)*L2 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+b)*L2;
			double pres_case_4_zone_2 = Arrival_Rate.get("ZONE1")*ZIPF.get("C"+c)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+c)*L1 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+c)*L2;
			double pres_case_4_zone_3 = Arrival_Rate.get("ZONE1")*ZIPF.get("C"+point)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+point)*L2 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+point)*L1;
			
			double delta_latency_4 = before_latency-latency_case_4;
			double pres_case_4 = w1*(pres_case_4_zone_1-pressure_1)+w2*(pres_case_4_zone_2-pressure_2)+w3*(pres_case_4_zone_3-pressure_3);
			
			// 换 1 & 3
			
			
			
			// 换 2 & 3
			double latency_case_6 =
					 Arrival_Rate.get("ZONE1")*ZIPF.get("C"+point)*L1 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+point)*L2+ Arrival_Rate.get("ZONE3")*ZIPF.get("C"+point)*L2
					+Arrival_Rate.get("ZONE1")*ZIPF.get("C"+b)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+b)*L1 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+b)*L2
					+Arrival_Rate.get("ZONE1")*ZIPF.get("C"+c)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+c)*L2 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+c)*L1;
			
			double pres_case_6_zone_1 = Arrival_Rate.get("ZONE1")*ZIPF.get("C"+point)*L1 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+point)*L2 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+point)*L2;
			double pres_case_6_zone_2 = Arrival_Rate.get("ZONE1")*ZIPF.get("C"+b)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+b)*L1 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+b)*L2;
			double pres_case_6_zone_3 = Arrival_Rate.get("ZONE1")*ZIPF.get("C"+c)*L2 + Arrival_Rate.get("ZONE2")*ZIPF.get("C"+c)*L2 + Arrival_Rate.get("ZONE3")*ZIPF.get("C"+c)*L1;

			double delta_latency_6 = before_latency-latency_case_6;
			double pres_case_6 = w1*(pres_case_6_zone_1-pressure_1)+w2*(pres_case_6_zone_2-pressure_2)+w3*(pres_case_6_zone_3-pressure_3);
			
			
			
			//----------------汇聚结果-----------------
			TreeMap<Double,String> rank = new TreeMap<>();
			
			if((pres_case_1_zone_1-pressure_1)>0 && (pres_case_1_zone_2-pressure_2)>0 && (pres_case_1_zone_3-pressure_3)>0 && delta_latency_1>0){
			rank.put(alpha*delta_latency_1+pres_case_1, "case1");}
			
			if((pres_case_2_zone_1-pressure_1)>0 && (pres_case_2_zone_2-pressure_2)>0 && (pres_case_2_zone_3-pressure_3)>0 && delta_latency_2>0){
			rank.put(alpha*delta_latency_2+pres_case_2, "case2");}
			
			if((pres_case_4_zone_1-pressure_1)>0 && (pres_case_4_zone_2-pressure_2)>0 && (pres_case_4_zone_3-pressure_3)>0 && delta_latency_4>0){
			rank.put(alpha*delta_latency_4+pres_case_4, "case4");}
			
			if((pres_case_6_zone_1-pressure_1)>0 && (pres_case_6_zone_2-pressure_2)>0 && (pres_case_6_zone_3-pressure_3)>0 && delta_latency_6>0){
			rank.put(alpha*delta_latency_6+pres_case_6, "case6");}
			
			if(rank.isEmpty()){
				return;
			}
			
			// 如果是case4， 即替换 1 & 2
			if(rank.lastEntry().getValue().equals("case4")){
				Map.get("ZONE1").remove("C"+point);
				Map.get("ZONE2").remove("C"+point);
				
				Map.get("ZONE1").add("C"+b);
				Map.get("ZONE2").add("C"+c);
				
				b+=2;
				c+=2;
				
				PRESSURE1 = PRESSURE1 + pres_case_4_zone_1-pressure_1;
				PRESSURE2 = PRESSURE2 + pres_case_4_zone_2-pressure_2;
				PRESSURE3 = PRESSURE3 + pres_case_4_zone_3-pressure_3;
			}
			
			if(rank.lastEntry().getValue().equals("case6")){
				Map.get("ZONE2").remove("C"+point);
				Map.get("ZONE3").remove("C"+point);
				
				Map.get("ZONE2").add("C"+b);
				Map.get("ZONE3").add("C"+c);
				
				b+=2;
				c+=2;
				
				PRESSURE1 = PRESSURE1 + pres_case_6_zone_1-pressure_1;
				PRESSURE2 = PRESSURE2 + pres_case_6_zone_2-pressure_2;
				PRESSURE3 = PRESSURE3 + pres_case_6_zone_3-pressure_3;
			}
			
			if(rank.lastEntry().getValue().equals("case2")){
				Map.get("ZONE2").remove("C"+point);
				
				Map.get("ZONE2").add("C"+b);
				
				b+=1;
				c+=1;
				
				PRESSURE1 = PRESSURE1 + pres_case_2_zone_1-pressure_1;
				PRESSURE2 = PRESSURE2 + pres_case_2_zone_2-pressure_2;
				PRESSURE3 = PRESSURE3 + pres_case_2_zone_3-pressure_3;
			}
			// 每循环一次结束后，point--
			point--;
		}

	}

	
	static void callMap(){
		generate_zipf();
		pre_map();
		reMap();
		System.out.println("ReMap结束");
	}
	
	public static void main(String[] args) {
		generate_zipf();
		pre_map();
		reMap();
		System.out.println("ReMap结束");

		System.out.println("DEBUG");
	}
}
