package newSimulation;

import java.util.*;
import java.util.Map.Entry;

public class MCTL {
	public static HashMap<String, HashSet<String>> Map = new HashMap<>();
	public static HashMap<String, Double> Pressure = new HashMap<>();
	
	public static void MainFunction() {
		int first_a = 0, first_b = 0, first_c = 0;
		double pre = Double.MAX_VALUE;
		
		while (true) {

			TreeMap<Double, String> evalution = new TreeMap<>();
			evalution.put(subMap(first_a + 1, first_b, first_c), "case1");
			evalution.put(subMap(first_a + 1, first_b + 1, first_c), "case2");
			evalution.put(subMap(first_a + 1, first_b + 1, first_c + 1), "case3");

			if(Double.compare(pre, evalution.firstKey())<=0){
				System.out.println("系统无增益！");
				break;
			}else{
				pre = evalution.firstKey();				
			}
			
			if (evalution.firstEntry().getValue().equals("case1")) {
				System.out.println("CASE1-"+evalution.firstKey());
				first_a++;
				//break;
			}

			if (evalution.firstEntry().getValue().equals("case2")) {
				System.out.println("CASE2-"+evalution.firstKey());
				first_a++;
				first_b++;
			}

			if (evalution.firstEntry().getValue().equals("case3")) {
				System.out.println("CASE3-"+evalution.firstKey());
				first_a++;
				first_b++;
				first_c++;
			}
			
			if(first_a==PublicSetting.StorageSpace || first_b==PublicSetting.StorageSpace || first_c==PublicSetting.StorageSpace){
				System.out.println("超出容量限制！");
				break;
			}

		}
		
		subMap(first_a, first_b, first_c);
		for(int i=1;i<=3;i++){
			System.out.println("P:"+Pressure.get("Zone"+i));
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
		}// end for

		// 预先计算压力
		PreCalPressure(mark);
		
		// 之后放置第二部分
		// 当还有空间的时候
		while(hasStorageSpace()){
			// 先找到下一个要放的内容
			int point = GetNextGlobalPoint(mark);
			String content_id = PublicSetting.GlobalRank.get(point);
			
			TreeMap<Double, String> candidate = new TreeMap<>(Collections.reverseOrder());
			// 先按照权重生成候选人名单
			for (int i = 1; i <= 3; i++) {
				if (Map.get("Zone" + i).size() < PublicSetting.StorageSpace) {
					double val = PublicSetting.ArrivalRate.get("Zone" + i)
							* PublicSetting.Popularity.get("Zone" + i).get(content_id);

					candidate.put(val, "Zone" + i);
				}
			}
			
			boolean if_success = false;
			String target = null;
			Iterator<Entry<Double, String>> it = candidate.entrySet().iterator();
			while(it.hasNext()){
				target = it.next().getValue();
				
				//如果该目标服务器负载小于预设阈值
				if(Double.compare(Pressure.get(target),PublicSetting.Processing*PublicSetting.rate)<0){
					// 标记为可选项
					if_success = true;
					break;
				}
				
				//否则继续查找下一个
			}
			
			// 如果没有成功找到，即代表所有服务器都超过了负载
			if(!if_success){
				candidate.clear();
				
				for(int i=1;i<=3;i++){
					if(Map.get("Zone" + i).size() < PublicSetting.StorageSpace)
						candidate.put(Pressure.get("Zone"+i),"Zone"+i);						
				}
				
				// 则选择负载最低的一个作为目标 （注意此处为倒叙）
				target = candidate.lastEntry().getValue();
			}
			
			
			// 存入该内容，更新mark，更新Pressure
			Map.get(target).add(content_id);
			
			mark.add(content_id);
			
			for(int i=1;i<=3;i++){
				if(Map.get("Zone"+i).contains(content_id)){
					double pre = Pressure.get("Zone"+i);
					pre = pre + PublicSetting.ArrivalRate.get("Zone"+i)
								*PublicSetting.Popularity.get("Zone"+i).get(content_id)
								*PublicSetting.L1;
					
					Pressure.put("Zone"+i, pre);
				}else{
					double bal = Pressure.get(target);
					bal = bal + PublicSetting.ArrivalRate.get("Zone"+i)
								*PublicSetting.Popularity.get("Zone"+i).get(content_id)
								*PublicSetting.L2;
		
					Pressure.put(target, bal);
				}
			}// end for
			
			
		}
		
		// 计算可能的时延
		return evaluateLatency(mark);
	}
	
	
	
	/**
	 * 压力预先计算
	 * */
	private static void PreCalPressure(HashSet<String> mark){
		Pressure.clear();
		
		Pressure.put("Zone1", 0.0);
		Pressure.put("Zone2", 0.0);
		Pressure.put("Zone3", 0.0);
		
		// 考察每一个内容
		for(int c=1;c<=PublicSetting.ContentNumber;c++){
			// 只有该内容在Zone中有存储，才会产生压力
			if(mark.contains("C"+c)){
				
				//考察每一个服务器
				for(int i=1;i<=3;i++){
					if(Map.get("Zone"+i).contains("C"+c)){
						double pre = Pressure.get("Zone"+i);
						pre = pre + PublicSetting.ArrivalRate.get("Zone"+i)
									*PublicSetting.Popularity.get("Zone"+i).get("C"+c)
									*PublicSetting.L1;
						
						Pressure.put("Zone"+i, pre);
					}else{
						//如果本地没有该内容，先计算整个区域有多少服务器有该内容
						double number = 0;
						for(int j=1;j<=3;j++){
							if(Map.get("Zone"+j).contains("C"+c))
								number = number + 1;
						}
						
						//负载均衡
						for(int j=1;j<=3;j++){
							if(Map.get("Zone"+j).contains("C"+c)){
								double bal = Pressure.get("Zone"+j);
								bal = bal + PublicSetting.ArrivalRate.get("Zone"+i)
											*PublicSetting.Popularity.get("Zone"+i).get("C"+c)
											*PublicSetting.L2 / number;
								
								Pressure.put("Zone"+j, bal);
							}
						}

					}//end if
				}// end for server
				
				
			} // end if mark
		}// end for content
		
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
	
	/**
	 * 判断是否还有可用存储空间
	 * */
	private static boolean hasStorageSpace() {
		for (int i = 1; i <= 3; i++) {
			if (Map.get("Zone" + i).size() < PublicSetting.StorageSpace)
				return true;
		}

		return false;
	}

	/**
	 * 根据全局喜好程度，查找下一个需要存储的内容
	 * */
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
		//PublicSetting.ChangePopularity(2);
		//PublicSetting.ChangePopularity(3);
		//-----------------------------
		PublicSetting.CalculateLikePoint();
		PublicSetting.CalculateLocalRank();
		PublicSetting.CalculateGoRank();
		
		MainFunction();
		PublicSetting.ShowMap(Map);
	}
	
	
}
