package localserver;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import base.FileServerControl;
import net.sf.json.JSONObject;

public class LocalServerPublicSetting {
	// 用于记录monitor的socket信息；
	public static BufferedReader MonitorSocketInput = null;
	public static PrintWriter MonitorSocketWrite = null;

	// 链接断开后不会消失的数据
	public static final String Key = "LocalServer";
	public static String ID = "NULL";
	public static String OriginalServerIP = "NULL";

	public static int LoadBalancePort = 0;

	// 自身服务器服务能力
	public static int Start_port = 0;
	public static int End_port = 0;
	// 同级服务器信息 (ID,IP)
	public static HashMap<String, String> Neighbor = new HashMap<>();
	// TODO 考虑一下存储分布表是不是也要放在这里
	private static HashMap<String, ArrayList<String>> ContentMap = new HashMap<>();

	// --------------------------
	public static LoadBalanceServer loadbalanceserver = null;
	public static Thread t_load = null;
	public static RemoteBalanceServer remotebalanceserver = null;
	public static Thread t_remote = null;
	public static FileServerControl fileservercontrol = null;
	public static Thread t_file = null;
	// 记录本地服务的启动状态
	public static boolean localserverflag = false;

	// ------------到达率统计相关---------------
	public static int time_period = 1000;
	// 用于记录总请求数
	public static int total_arrival = 0;
	// 用于检测各个时间段的到达率
	public static HashMap<Integer, Integer> total_arrival_rate = new HashMap<>();
	// 用于记录总内容数目
	public static double Content_N = 0.0;
	// 用于记录每个content被请求了多少次
	public static HashMap<String, Integer> content_count = new HashMap<>();
	// Sanjay教授法，用于统计每个内容的实时喜好程度
	public static HashMap<String, Double> content_live_like = new HashMap<>();

	/**
	 * 带锁的方法，负责查找一些内容；更新一些内容
	 */
	public synchronized static boolean DoContentMap(String gowhere, String server, String id) {
		if ("FIND".equals(gowhere)) {
			return ContentMap.get(server).contains(id);
		}

		// 初始化操作
		if ("INIT".equals(gowhere)) {
			// 先清除所有内容
			ContentMap.clear();

			JSONObject json = JSONObject.fromObject(server);

			// 按照Neighbor中的信息读取内容
			for (String e : Neighbor.keySet()) {
				String a = json.getString(e);
				String[] c = a.split("-");

				// 创建新的列表
				ArrayList<String> lis = new ArrayList<>();
				// 载入列表
				for (String ee : c)
					lis.add(ee);

				// 将这些信息加入在Map中
				ContentMap.put(e, lis);
			}

		}

		// TODO 更新内容需要完善

		return false;
	}
}
