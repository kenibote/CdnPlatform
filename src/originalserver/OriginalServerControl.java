package originalserver;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import base.FileServerControl;
import net.sf.json.JSONObject;

public class OriginalServerControl {
	/**
	 * OriginalServerControl端控制类
	 */
	private static Logger logger = LogManager.getLogger(OriginalServerControl.class.getName());
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
		case "TASK_001":
			HELLO();
			break;

		case "TASK_007":
			OriginalServerSet(commandin);
			break;

		case "TASK_008":
			initOriginalServer();
			break;

		case "TASK_009":
			startOriginalServer();
			break;

		case "TASK_010":
			stopOriginalServer();
			break;

		// 如果没有匹配到任何task
		default:
			Default();
		}

	}

	public static void initOriginalServer() {
		// 设置并启动Remote-balance服务
		// Remote-balance 服务会使用到LocalServerPublicSetting中设置的端口号
		OriginalServerPublicSetting.originalremotebalanceserver = new OriginalRemoteBalanceServer();
		OriginalServerPublicSetting.t_remote = new Thread(OriginalServerPublicSetting.originalremotebalanceserver);
		OriginalServerPublicSetting.t_remote.start();

		// 设置并启动FileServerControl服务
		// 这个类启动之后并没有什么副作用
		OriginalServerPublicSetting.fileservercontrol = new FileServerControl();
		OriginalServerPublicSetting.t_file = new Thread(OriginalServerPublicSetting.fileservercontrol);
		OriginalServerPublicSetting.t_file.start();

		initResult("INIT");
		result.put("CODE", "TASK_008");
		result.put("STATE", "SUCCESS");
		initResult("SEND");
	}

	public static void startOriginalServer() {
		initResult("INIT");

		if (!OriginalServerPublicSetting.originalserverflag) {
			// 启动文件下载服务
			FileServerControl.setPort(OriginalServerPublicSetting.Start_port, OriginalServerPublicSetting.End_port);
			FileServerControl.initFileServer();
			FileServerControl.startFileServer();

			result.put("CODE", "TASK_009");
			result.put("STATE", "SUCCESS");

			OriginalServerPublicSetting.originalserverflag = true;
		} else {
			result.put("CODE", "ERROR_5");
		}

		initResult("SEND");
	}

	public static void stopOriginalServer() {
		initResult("INIT");
		if (OriginalServerPublicSetting.originalserverflag) {
			// 关闭FileServerControl服务
			FileServerControl.stopFileServer();

			result.put("CODE", "TASK_010");
			result.put("STATE", "SUCCESS");

			OriginalServerPublicSetting.originalserverflag = false;
		} else {
			result.put("CODE", "ERROR_6");
		}

		initResult("SEND");
	}

	@SuppressWarnings("unchecked")
	public static void OriginalServerSet(String commandin) {
		json = JSONObject.fromObject(commandin);

		// OriginalServer端的ID
		OriginalServerPublicSetting.ID = (String) json.getOrDefault("ID", "NULL");
		// 原服务器信息
		OriginalServerPublicSetting.OriginalServerIP = (String) json.getOrDefault("OriginalServerIP", "NULL");
		OriginalServerPublicSetting.RemoteBalancePort = Integer
				.parseInt((String) json.getOrDefault("RemoteBalancePort", "0"));
		// 服务能力（起始与终止端口）
		OriginalServerPublicSetting.Start_port = Integer.parseInt((String) json.getOrDefault("StartPort", "0"));
		OriginalServerPublicSetting.End_port = Integer.parseInt((String) json.getOrDefault("EndPort", "0"));

		// 记录邻居信息
		int neighbor_number = Integer.parseInt((String) json.getOrDefault("LocalServerNumber", "0"));
		OriginalServerPublicSetting.Neighbor.clear();
		for (int i = 1; i <= neighbor_number; i++) {
			// 注意这里json传递的信息和实际记录在OriginalServerPublicSetting中的信息并不一样
			String KEY = "Neighbor" + i;
			String NeighborIP = (String) json.getOrDefault(KEY, "NULL");
			OriginalServerPublicSetting.Neighbor.put("LocalServer" + i, NeighborIP);
		}

		logger.info("Setting Updated!");
		initResult("INIT");
		result.put("CODE", "TASK_007");
		result.put("STATE", "SUCCESS");
		initResult("SEND");

	}

	// HELLO包
	public static void HELLO() {
		logger.info("HELLO package");

		initResult("INIT");
		result.put("CODE", "TASK_001");
		result.put("STATE", "SUCCESS");
		
		result.put("OriginalServerIP", OriginalServerPublicSetting.OriginalServerIP);
		result.put("RemoteBalancePort", "" + OriginalServerPublicSetting.RemoteBalancePort);

		result.put("StartPort", "" + OriginalServerPublicSetting.Start_port);
		result.put("EndPort", "" + OriginalServerPublicSetting.End_port);

		result.put("LocalServerNumber", "" + OriginalServerPublicSetting.Neighbor.size());
		Iterator<String> it = OriginalServerPublicSetting.Neighbor.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			result.put(key, OriginalServerPublicSetting.Neighbor.get(key));
		}
		result.put("OriginalServerStatus", OriginalServerPublicSetting.originalserverflag + "");

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
			OriginalServerControl.result.clear();
			// 默认情况下发送设备的类型以及ID
			result.put("DEVICE", "OriginalServer");
			result.put("ID", OriginalServerPublicSetting.ID);
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
		if (OriginalServerPublicSetting.MonitorSocketWrite != null) {
			OriginalServerPublicSetting.MonitorSocketWrite.println(output);
			OriginalServerPublicSetting.MonitorSocketWrite.flush();
		} else {
			// TODO 可能需要补充发送失败处理函数
		}
	}

}
