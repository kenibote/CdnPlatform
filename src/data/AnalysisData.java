package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import base.TaskInfo;

public class AnalysisData {
	/**
	 * 读取任务执行结果文件，经过降噪处理后，分析SELF,NEIG,ORIG这3种情况下，重定向时间以及文件下载的平均时间
	 */

	public static String Go = "R";
	public static String Original = "10.10.12.136";
	public static String path = "D:\\Content\\5-1\\";

	public static void analy_1() throws Exception {
		ArrayList<TaskInfo> data = new ArrayList<>();

		File file = new File(path);
		File[] filelist = file.listFiles();

		// 载入文件
		for (File f : filelist) {
			BufferedReader fin = new BufferedReader(new FileReader(f));

			String readin = null;
			while ((readin = fin.readLine()) != null) {
				String[] r = readin.split(",");

				TaskInfo t = new TaskInfo();
				t.wait_time = Integer.parseInt(r[0]);
				t.task_from = r[1];
				t.load_balance_port = Integer.parseInt(r[2]);
				t.task_id = Integer.parseInt(r[3]);
				t.file_name = r[4];
				t.task_server_ip = r[5];
				t.task_server_port = Integer.parseInt(r[6]);
				t.redirect_time = Integer.parseInt(r[7]);
				t.download_time = Integer.parseInt(r[8]);

				data.add(t);
			} // end while

			fin.close();
		} // end for

		// -----------------------读取文件结束----------------------------------
		for (int jiang_zao = 1; jiang_zao <= 1; jiang_zao++) {

			int i = 0;
			double average = 0;
			double biao_zhun_cha = 0;

			// ----------第1次计算平均值----------------
			for (TaskInfo e : data) {
				i++;
				if ("R".equals(Go))
					average = average + e.redirect_time;
				if ("D".equals(Go))
					average = average + e.download_time;
			}
			average = average / (double) i;
			System.out.println("降噪前，平均值：" + average);

			// ----------第1次计算标准差----------------
			for (TaskInfo e : data) {
				if ("R".equals(Go))
					biao_zhun_cha = biao_zhun_cha + (e.redirect_time - average) * (e.redirect_time - average);
				if ("D".equals(Go))
					biao_zhun_cha = biao_zhun_cha + (e.download_time - average) * (e.download_time - average);
			}
			biao_zhun_cha = biao_zhun_cha / (double) i;
			biao_zhun_cha = Math.sqrt(biao_zhun_cha);

			// ----------第1次降噪----------------
			ArrayList<TaskInfo> del = new ArrayList<>();
			for (TaskInfo e : data) {
				double val = 0;

				if ("R".equals(Go))
					val = Math.abs(e.redirect_time - average) - 3 * biao_zhun_cha;
				if ("D".equals(Go))
					val = Math.abs(e.download_time - average) - 3 * biao_zhun_cha;

				if (val > 0) {
					del.add(e);
				}
			}
			System.out.println("第" + jiang_zao + "次降噪，去除噪音：" + del.size());
			data.removeAll(del);

		}

		double average = 0;
		for (TaskInfo e : data) {
			if ("R".equals(Go))
				average = average + e.redirect_time;
			if ("D".equals(Go))
				average = average + e.download_time;
		}
		average = average / (double) data.size();
		System.out.println("最终平均值：" + average);

		// ---------------------------分析操作-------------------------------------
		double average_self = 0;
		int i_self = 0;
		double average_ori = 0;
		int i_ori = 0;
		double average_neig = 0;
		int i_neig = 0;

		for (TaskInfo e : data) {
			if ("R".equals(Go)) {
				if (e.task_server_ip.equals("192.168.1.101")) {
					average_self = average_self + e.redirect_time;
					i_self++;
					continue;
				}

				if (e.task_server_ip.equals(Original)) {
					average_ori = average_ori + e.redirect_time;
					i_ori++;
					continue;
				}

				average_neig = average_neig + e.redirect_time;
				i_neig++;
			} // end if

			if ("D".equals(Go)) {
				if (e.task_server_ip.equals("192.168.1.101")) {
					average_self = average_self + e.download_time;
					i_self++;
					continue;
				}

				if (e.task_server_ip.equals(Original)) {
					average_ori = average_ori + e.download_time;
					i_ori++;
					continue;
				}

				average_neig = average_neig + e.download_time;
				i_neig++;
			} // end if

		} // end for
		average_self = average_self / i_self;
		average_neig = average_neig / i_neig;
		average_ori = average_ori / i_ori;

		System.out.println("Average_self:" + average_self + ", count:" + i_self);
		System.out.println("Average_neig:" + average_neig + ", count:" + i_neig);
		System.out.println("Average_ori:" + average_ori + ", count:" + i_ori);

	}

	public static void main(String[] args) throws Exception {
		Go = "D";
		Original = "10.10.12.136";
		path = "D:\\Content\\20-2\\";
		analy_1();

	}
}
