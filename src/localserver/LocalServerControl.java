package localserver;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import base.FileServerControl;
import net.sf.json.JSONObject;

public class LocalServerControl {
	/**
	 * LocalServerControl端控制类
	 */
	private static Logger logger = LogManager.getLogger(LocalServerControl.class.getName());
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

		case "TASK_003":
			localserverSet(commandin);
			break;

		case "TASK_014":
			UpdataContentMap(commandin);
			break;

		case "TASK_004":
			initLocalServer();
			break;

		case "TASK_005":
			StartLocalServer();
			break;

		case "TASK_017":
			initLike(commandin);
			break;

		case "TASK_015":
			StartStaticFunction();
			break;

		case "TASK_016":
			StopStaticFunction();
			break;

		case "TASK_006":
			StopLocalServer();
			break;

		case "TASK_018":
			requestCount();
			break;

		// 如果没有匹配到任何task
		default:
			Default();
		}

	}

	public static void requestCount() {
		initResult("INIT");

		// TODO 补充计算到达率的方法
		double rate = LocalServerStatic.Average_ArrivalRate();
		result.put("ArrivalRate", rate + "");
		result.put("ContentNumber", LocalServerPublicSetting.Content_N + "");
		LocalServerPublicSetting.DoContentCount("RESULT", null, result);

		result.put("CODE", "TASK_018");
		result.put("STATE", "SUCCESS");
		initResult("SEND");
	}

	public static void initLike(String commandin) {
		initResult("INIT");
		
		json = JSONObject.fromObject(commandin);
		
		int ContentNumber = json.getInt("ContentNumber");
		LocalServerPublicSetting.Content_N = (double)ContentNumber;
		
		// 先清理所有数据，之后重新赋值
		LocalServerPublicSetting.content_live_like.clear();
		for(int i=1;i<=ContentNumber;i++){
			double like = json.getDouble("C"+i);
			LocalServerPublicSetting.content_live_like.put("C"+i, like);
		}
		
		result.put("CODE", "TASK_017");
		result.put("STATE", "SUCCESS");
		initResult("SEND");
	}

	public static void StartStaticFunction() {
		initResult("INIT");

		if (!LocalServerPublicSetting.localstaticfunction) {
			LocalServerPublicSetting.localstaticfunction = true;
			// 最好在本地服务启动之后，再启动统计服务
			// 在启动统计服务之前，需要进行初始化设置
			LocalServerPublicSetting.total_arrival = 0;
			LocalServerPublicSetting.total_arrival_rate.clear();
			LocalServerPublicSetting.content_count.clear();

			for (int i = 1; i <= (int) LocalServerPublicSetting.Content_N; i++) {
				LocalServerPublicSetting.content_count.put("C" + i, 0);
			}

			// 最后启动服务
			new Thread(new LocalServerStatic()).start();

			result.put("CODE", "TASK_015");
			result.put("STATE", "SUCCESS");
		} else {
			result.put("CODE", "ERROR_007");
		}

		initResult("SEND");
	}

	public static void StopStaticFunction() {
		initResult("INIT");

		if (LocalServerPublicSetting.localstaticfunction) {

			result.put("CODE", "TASK_016");
			result.put("STATE", "SUCCESS");

			LocalServerPublicSetting.localstaticfunction = false;
		} else {
			result.put("CODE", "ERROR_008");
		}

		initResult("SEND");
	}

	public static void UpdataContentMap(String commandin) {
		LocalServerPublicSetting.DoContentMap("INIT", commandin, "");

		initResult("INIT");
		result.put("CODE", "TASK_014");
		result.put("STATE", "SUCCESS");
		initResult("SEND");
	}

	public static void initLocalServer() {

		// 设置并启动load-balance服务
		// load-balance 服务会使用到LocalServerPublicSetting中设置的端口号
		LocalServerPublicSetting.loadbalanceserver = new LoadBalanceServer();
		LocalServerPublicSetting.t_load = new Thread(LocalServerPublicSetting.loadbalanceserver);
		LocalServerPublicSetting.t_load.start();

		// 设置并启动Remote-balance服务
		// Remote-balance 服务会使用到LocalServerPublicSetting中设置的端口号
		LocalServerPublicSetting.remotebalanceserver = new RemoteBalanceServer();
		LocalServerPublicSetting.t_remote = new Thread(LocalServerPublicSetting.remotebalanceserver);
		LocalServerPublicSetting.t_remote.start();

		// 设置并启动FileServerControl服务
		// 这个类启动之后并没有什么副作用
		LocalServerPublicSetting.fileservercontrol = new FileServerControl();
		LocalServerPublicSetting.t_file = new Thread(LocalServerPublicSetting.fileservercontrol);
		LocalServerPublicSetting.t_file.start();

		initResult("INIT");
		result.put("CODE", "TASK_004");
		result.put("STATE", "SUCCESS");
		initResult("SEND");
	}

	/**
	 * 启动本地服务器
	 */
	public static void StartLocalServer() {
		initResult("INIT");

		if (!LocalServerPublicSetting.localserverflag) {
			// 启动文件下载服务
			FileServerControl.setPort(LocalServerPublicSetting.Start_port, LocalServerPublicSetting.End_port);
			FileServerControl.initFileServer();
			FileServerControl.startFileServer();

			result.put("CODE", "TASK_005");
			result.put("STATE", "SUCCESS");

			LocalServerPublicSetting.localserverflag = true;
		} else {
			result.put("CODE", "ERROR_3");
		}

		initResult("SEND");
	}

	public static void StopLocalServer() {
		initResult("INIT");
		if (LocalServerPublicSetting.localserverflag) {
			// 关闭FileServerControl服务
			FileServerControl.stopFileServer();

			result.put("CODE", "TASK_006");
			result.put("STATE", "SUCCESS");

			LocalServerPublicSetting.localserverflag = false;
		} else {
			result.put("CODE", "ERROR_4");
		}

		initResult("SEND");
	}

	// HELLO包
	public static void HELLO() {
		logger.info("HELLO package");

		initResult("INIT");
		result.put("CODE", "TASK_001");
		result.put("STATE", "SUCCESS");

		result.put("OriginalServerIP", LocalServerPublicSetting.OriginalServerIP);
		result.put("LoadBalancePort", "" + LocalServerPublicSetting.LoadBalancePort);

		result.put("StartPort", "" + LocalServerPublicSetting.Start_port);
		result.put("EndPort", "" + LocalServerPublicSetting.End_port);

		result.put("LocalServerNumber", "" + LocalServerPublicSetting.Neighbor.size());
		Iterator<String> it = LocalServerPublicSetting.Neighbor.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			result.put(key, LocalServerPublicSetting.Neighbor.get(key));
		}
		result.put("LocalServerStatus", LocalServerPublicSetting.localserverflag + "");

		initResult("SEND");
	}

	/**
	 * TASK_003 : 设置ID,原服务器IP:PORT, 服务能力(端口号范围)， 邻居地址等...
	 * (MONITOR-->LOCALSERVER)
	 */
	@SuppressWarnings("unchecked")
	public static void localserverSet(String commandin) {
		json = JSONObject.fromObject(commandin);
		// LocalServer端的ID
		LocalServerPublicSetting.ID = (String) json.getOrDefault("ID", "NULL");
		// 原服务器信息
		LocalServerPublicSetting.OriginalServerIP = (String) json.getOrDefault("OriginalServerIP", "NULL");
		LocalServerPublicSetting.LoadBalancePort = Integer.parseInt((String) json.getOrDefault("LoadBalancePort", "0"));
		// 本地服务能力（起始与终止端口）
		LocalServerPublicSetting.Start_port = Integer.parseInt((String) json.getOrDefault("StartPort", "0"));
		LocalServerPublicSetting.End_port = Integer.parseInt((String) json.getOrDefault("EndPort", "0"));

		// 记录邻居信息
		int neighbor_number = Integer.parseInt((String) json.getOrDefault("LocalServerNumber", "0"));
		LocalServerPublicSetting.Neighbor.clear();
		for (int i = 1; i <= neighbor_number; i++) {
			// 注意这里json传递的信息和实际记录在LocalServerPublicSetting中的信息并不一样
			String KEY = "Neighbor" + i;
			String NeighborIP = (String) json.getOrDefault(KEY, "NULL");
			LocalServerPublicSetting.Neighbor.put("LocalServer" + i, NeighborIP);
		}

		logger.info("Setting Updated!");
		initResult("INIT");
		result.put("CODE", "TASK_003");
		result.put("STATE", "SUCCESS");
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
			LocalServerControl.result.clear();
			// 默认情况下发送设备的类型以及ID
			result.put("DEVICE", "LocalServer");
			result.put("ID", LocalServerPublicSetting.ID);
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
		if (LocalServerPublicSetting.MonitorSocketWrite != null) {
			LocalServerPublicSetting.MonitorSocketWrite.println(output);
			LocalServerPublicSetting.MonitorSocketWrite.flush();
		} else {
			// TODO 可能需要补充发送失败处理函数
		}
	}

}
