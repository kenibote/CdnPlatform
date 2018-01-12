package originalserver;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

		// 如果没有匹配到任何task
		default:
			Default();
		}

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
