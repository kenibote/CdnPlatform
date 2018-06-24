package newSimulation;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import base.FileDownload;
import monitor.MonitorSetting;
import net.sf.json.JSONObject;
import simulation.SimulationTask;

public class OneDay {           //1     2    3    4    5    6     7     8     9     10    11    12   13    14    15  16  17  18  19  20  21  22  23  24
	static double[] zone1 = { 0, 3.0,  3.0, 3.0, 3.0, 5.0, 10.0, 20.0, 25.0, 35.0, 40.0, 43.0, 45.0, 40.0, 35.0, 33, 34, 37, 45, 37, 28, 15, 10, 8,  5 };
	static double[] zone2 = { 0, 10.0, 5.0, 4.0, 4.0, 6.0, 7.0,  7.0,  9.0,  9.0,  10.0, 11.0, 12.0, 15.0, 13.0, 13, 14, 15, 15, 20, 25, 31, 31, 20, 15 };
	static double[] zone3 = { 0, 5.0,  4.0, 3.0, 3.0, 5.0, 5.0,  6.0,  6.0,  7.0,  7.0,  8.0,  8.0,  10.0, 9.0,  9,  10, 10, 10, 15, 20, 28, 25, 15, 10 };

	static String pixPath = "D:\\Content\\24basefile\\";

	public static void main(String[] args) throws Exception {
		//generateTask();
		//generateMixCoMap(9);
		runTest();
	}
	
	
	static public void generateTask() throws Exception {
		// 先准备分布数据，由于这里假设分布一样，所以只用计算一次即可
		PublicSetting.GenerateZipf(0.88);
		PublicSetting.PutPopularitySame();
		PublicSetting.CalculateLikePoint();
		PublicSetting.CalculateLocalRank();
		PublicSetting.CalculateGoRank();

		// 以小时为单位，生成TASK
		for (int hour = 1; hour <= 24; hour++) {
			// 先设置到达率信息
			PublicSetting.ArrivalRate.put("Zone1", zone1[hour] / 8);
			PublicSetting.ArrivalRate.put("Zone2", zone2[hour] / 8);
			PublicSetting.ArrivalRate.put("Zone3", zone3[hour] / 8);
			// 清空任务时间轴
			PublicSetting.time_line.clear();
			// 生成任务，300s
			PublicSetting.Generate_Task(300);

			// 生成保存文件的路径
			String path1 = pixPath + hour + "\\loadtask-" + hour + "-" + 1 + ".csv";
			String path2 = pixPath + hour + "\\loadtask-" + hour + "-" + 2 + ".csv";
			String path3 = pixPath + hour + "\\loadtask-" + hour + "-" + 3 + ".csv";
			FileWriter fout1 = new FileWriter(path1);
			FileWriter fout2 = new FileWriter(path2);
			FileWriter fout3 = new FileWriter(path3);

			double time1 = 0, time2 = 0, time3 = 0;

			Iterator<Entry<Double, SimulationTask>> it = PublicSetting.time_line.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Double, SimulationTask> entry = it.next();
				int timesp = 0;

				if ("Zone1".equals(entry.getValue().TaskFrom)) {
					timesp = (int) ((entry.getKey() - time1) * 1000);
					time1 = entry.getKey();
					fout1.write(timesp + "," + "192.168.1.101,8070," + entry.getValue().TaskID + ","
							+ entry.getValue().TaskContnet + ",\r\n");
				}

				if ("Zone2".equals(entry.getValue().TaskFrom)) {
					timesp = (int) ((entry.getKey() - time2) * 1000);
					time2 = entry.getKey();
					fout2.write(timesp + "," + "192.168.1.101,8070," + entry.getValue().TaskID + ","
							+ entry.getValue().TaskContnet + ",\r\n");
				}

				if ("Zone3".equals(entry.getValue().TaskFrom)) {
					timesp = (int) ((entry.getKey() - time3) * 1000);
					time3 = entry.getKey();
					fout3.write(timesp + "," + "192.168.1.101,8070," + entry.getValue().TaskID + ","
							+ entry.getValue().TaskContnet + ",\r\n");
				}

			}

			fout1.close();
			fout2.close();
			fout3.close();

		}
	}

	
	static public void generateMixCoMap(int hour) throws Exception{
		// 先进行准备工作
		PublicSetting.GenerateZipf(0.88);
		PublicSetting.PutPopularitySame();
		PublicSetting.CalculateLikePoint();
		PublicSetting.CalculateLocalRank();
		PublicSetting.CalculateGoRank();
		PublicSetting.Processing = 5.0;
		PublicSetting.rate = 0.4;
		
		// 重新设置到达率
		ArrayList<Double> rank = new ArrayList<>();
		rank.add(zone1[hour]); rank.add(zone2[hour]); rank.add(zone3[hour]);
		Collections.sort(rank, Collections.reverseOrder());
		
		PublicSetting.ArrivalRate.put("Zone1", rank.get(0)/8);
		PublicSetting.ArrivalRate.put("Zone2", rank.get(1)/8);
		PublicSetting.ArrivalRate.put("Zone3", rank.get(2)/8);
		
		// 生成MixCoMap
		MCTL.MainFunction();
		PublicSetting.ShowMap(MCTL.Map);
		// 保存文件
		String Path = pixPath+hour+"\\"+hour+"-MixCo.txt";
		PublicSetting.WriteMap(MCTL.Map,Path);
		
	}
	
	
	static public void runTest() throws Exception {
		Thread.sleep(2000);

		for (int hour = 16; hour <= 24; hour++) {
			// 下发任务和Map
			for(int server=1;server<=3;server++){
				MonitorSetting.UserLoadTaskFor24(hour,server);
				MonitorSetting.SetLocalServerMapFor24(server, "D:\\Content\\"+hour+"-MostTopMap.txt");
			}
			
			// 等待任务下载
			Thread.sleep(10*1000);
			
			// 启动任务
			for(int server=1;server<=3;server++){
				MonitorSetting.Usually(MonitorSetting.local_server_info.get(server), 8091, "USER","TASK_012");
			}
			
			// 等待任务执行
			Thread.sleep(5*60*1000+30*1000);
			
			// 回收结果
			for (int i = 1; i <= 3; i++) {
				String back = null;
				try {
					back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8091, "USER",
							"TASK_013");
					String FileName = JSONObject.fromObject(back).getString("RESULTNAME") + ".csv";
					FileDownload fd = new FileDownload(MonitorSetting.local_server_info.get(i), 8092, FileName, 0);
					fd.download();

				} catch (Exception e1) {
					System.out.println("Download Result Fail !!!");
				}
				System.out.println(back);
			}
			
			// 稍作休息，继续下一次
			Thread.sleep(10*1000);
		}

	}
	
	
	

	

}
