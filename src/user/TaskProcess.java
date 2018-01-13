package user;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import base.FileDownload;
import base.TaskInfo;

public class TaskProcess implements Runnable {
	private static Logger logger = LogManager.getLogger(TaskProcess.class.getName());

	@Override
	public void run() {
		logger.info("Task Process Start...");
		// 标记任务启动
		UserPublicSetting.SimulationFlag = true;

		double total_task = UserPublicSetting.TaskList.size();
		double point = 0.0;
		TaskInfo task = null;
		Iterator<TaskInfo> it = UserPublicSetting.TaskList.iterator();
		while (it.hasNext()) {
			task = it.next();

			// 先等待一段时间
			try {
				Thread.sleep(task.wait_time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// 启动任务
			logger.debug("start task - " + task.task_id);
			new Thread(new FileDownload(task)).start();

			// 标记任务执行进度
			point = point + 1.0;
			UserPublicSetting.SimulationStatus = point / total_task;
		}

		// 标记任务结束
		UserPublicSetting.SimulationFlag = false;

		// 等待下载任务结束
		try {
			Thread.sleep(1000*5);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		// 之后调用数据统计服务,写入到文件中
		UserPublicSetting.FileResult = UserPublicSetting.FileName + "-" + System.currentTimeMillis();
		String saveFile = UserPublicSetting.FilePix + UserPublicSetting.FileResult + ".csv";

		try {
			FileWriter fout = new FileWriter(saveFile);

			it = UserPublicSetting.TaskList.iterator();
			while (it.hasNext()) {
				task = it.next();
				// 按照TaskInfo中的顺序
				String output = task.wait_time + "," + task.task_from + "," + task.load_balance_port + ","
						+ task.task_id + "," + task.file_name + "," + task.task_server_ip + "," + task.task_server_port
						+ "," + task.redirect_time + "," + task.download_time + ",\r\n";

				fout.write(output);
			}

			// 关闭文件
			fout.flush();
			fout.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 测试代码
	public static void main(String[] args) throws Exception {
		TaskInfo task1 = new TaskInfo();
		task1.wait_time = 100;
		task1.task_from = "192.168.1.122";
		task1.load_balance_port = 8070;
		task1.task_id = 1;
		task1.file_name = "test11.JPG";

		TaskInfo task2 = new TaskInfo();
		task2.wait_time = 150;
		task2.task_from = "192.168.1.122";
		task2.load_balance_port = 8070;
		task2.task_id = 2;
		task2.file_name = "test12.JPG";

		TaskInfo task3 = new TaskInfo();
		task3.wait_time = 200;
		task3.task_from = "192.168.1.122";
		task3.load_balance_port = 8070;
		task3.task_id = 3;
		task3.file_name = "test21.JPG";

		TaskInfo task4 = new TaskInfo();
		task4.wait_time = 250;
		task4.task_from = "192.168.1.122";
		task4.load_balance_port = 8070;
		task4.task_id = 4;
		task4.file_name = "test22.JPG";

		UserPublicSetting.TaskList.add(task1);
		UserPublicSetting.TaskList.add(task2);
		UserPublicSetting.TaskList.add(task3);
		UserPublicSetting.TaskList.add(task4);

		new Thread(new TaskProcess()).start();

		Thread.sleep(1000 * 5);

		Iterator<TaskInfo> it = UserPublicSetting.TaskList.iterator();
		while (it.hasNext()) {
			TaskInfo task = it.next();
			System.out.println(
					"TaskID:" + task.task_id + ", RedTime:" + task.redirect_time + ", DowTime:" + task.download_time);
		}
	}

}
