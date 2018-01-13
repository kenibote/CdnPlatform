package user;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import base.FileDownload;
import base.TaskInfo;
import net.sf.json.JSONObject;

public class UserControl {
	/**
	 * user端控制类
	 */
	private static Logger logger = LogManager.getLogger(UserControl.class.getName());
	// 用于暂存路由信息
	private static String gowhere;
	// 用于生成回复信息
	private static HashMap<String, String> result = new HashMap<>();
	private static JSONObject json = null;

	/**
	 * 路由函数，用于将不同的命令路由到不同的处理函数；
	 */
	@SuppressWarnings("unchecked")
	public static void Router(String commandin) {
		// 获取命令类型
		gowhere = (String) JSONObject.fromObject(commandin).getOrDefault("TYPE", "NULL");
		// 命令路由表
		switch (gowhere) {
		// hello 包
		case "TASK_001":
			HELLO();
			break;
		// 基础设置函数
		case "TASK_002":
			UsuallySet(commandin);
			break;

		case "TASK_011":
			loadTask(commandin);
			break;

		case "TASK_012":
			startSimulation();
			break;

		case "TASK_013":
			checkTaskProcess();
			break;

		// 如果没有匹配到任何task
		default:
			Default();
		}

	}

	@SuppressWarnings({ "unchecked" })
	public static void loadTask(String commandin) {
		logger.info("Loading Task...");

		// 提取文件信息
		json = JSONObject.fromObject(commandin);
		UserPublicSetting.FileName = (String) json.getOrDefault("FileName", "NULL");
		String IP = (String) json.getOrDefault("MonitorIP", "NULL");
		int port = Integer.parseInt((String) json.getOrDefault("MonitorPort", "NULL"));

		// 下载指定文件
		new FileDownload(IP, port, UserPublicSetting.FileName + ".csv", 0).download();

		// 载入文件
		UserPublicSetting.TaskList.clear();
		try {
			// 打开文件
			BufferedReader fin = new BufferedReader(
					new FileReader(UserPublicSetting.FilePix + UserPublicSetting.FileName + ".csv"));
			String readin = null;
			while ((readin = fin.readLine()) != null) {
				String[] info = readin.split(",");

				TaskInfo task = new TaskInfo();
				task.wait_time = Integer.parseInt(info[0]);
				task.task_from = info[1];
				task.load_balance_port = Integer.parseInt(info[2]);
				task.task_id = Integer.parseInt(info[3]);
				task.file_name = info[4];

				UserPublicSetting.TaskList.add(task);
			}

			// 关闭文件
			fin.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		initResult("INIT");
		result.put("CODE", "TASK_011");
		result.put("STATE", "SUCCESS");
		initResult("SEND");
	}

	public static void checkTaskProcess() {
		logger.info("Check Task Process...");

		initResult("INIT");
		result.put("CODE", "TASK_013");
		result.put("STATE", "SUCCESS");
		result.put("TASKFLAG", UserPublicSetting.SimulationFlag + "");
		result.put("PROGRESS", UserPublicSetting.SimulationStatus + "");
		initResult("SEND");
	}

	public static void startSimulation() {
		logger.info("Start simulation...");

		// 启动任务执行线程
		new Thread(new TaskProcess()).start();

		initResult("INIT");
		result.put("CODE", "TASK_012");
		result.put("STATE", "SUCCESS");
		initResult("SEND");
	}

	/**
	 * 基础设置函数; 目前设置的内容有：userID，LocalServer ip & port
	 */
	@SuppressWarnings("unchecked")
	public static void UsuallySet(String commandin) {
		json = JSONObject.fromObject(commandin);
		// user端的ID
		UserPublicSetting.ID = (String) json.getOrDefault("ID", "NULL");
		// user优先联系的本地服务器信息
		UserPublicSetting.LocalServerIP = (String) json.getOrDefault("LocalServerIP", "NULL");
		UserPublicSetting.LocalServerPort = Integer.parseInt((String) json.getOrDefault("LocalServerPort", "0"));

		initResult("INIT");
		result.put("CODE", "TASK_002");
		result.put("STATE", "SUCCESS");
		initResult("SEND");
	}

	/**
	 * HELLO 包
	 */
	public static void HELLO() {
		logger.info("HELLO package.");

		initResult("INIT");
		result.put("CODE", "TASK_001");
		result.put("STATE", "SUCCESS");
		result.put("LocalServerIP", UserPublicSetting.LocalServerIP);
		result.put("LocalServerPort", "" + UserPublicSetting.LocalServerPort);
		initResult("SEND");
	}

	/**
	 * 默认回复内容
	 */
	public static void Default() {
		logger.info("Task Unknown.");

		initResult("INIT");
		result.put("CODE", "ERROR_002");
		initResult("SEND");
	}

	/**
	 * 初始化结果函数，用于减少重复代码
	 */
	public static void initResult(String dosomething) {
		if ("INIT".equals(dosomething)) {
			UserControl.result.clear();
			// 默认情况下发送设备的类型以及ID
			result.put("DEVICE", "USER");
			result.put("ID", UserPublicSetting.ID);
		}

		if ("SEND".equals(dosomething)) {
			json = JSONObject.fromObject(result);
			Send(json.toString());
		}

	}

	/**
	 * 通用发送函数
	 */
	public static void Send(String output) {
		// 安全性检查
		if (UserPublicSetting.MonitorSocketWrite != null) {
			UserPublicSetting.MonitorSocketWrite.println(output);
			UserPublicSetting.MonitorSocketWrite.flush();
		} else {
			// TODO 可能需要补充发送失败处理函数
		}
	}

}
