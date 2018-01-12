package originalserver;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class OriginalServerPublicSetting {
	// 用于记录monitor的socket信息；
	public static BufferedReader MonitorSocketInput = null;
	public static PrintWriter MonitorSocketWrite = null;

	// 链接断开后不会消失的数据
	public static final String Key = "OriginalServer";
	public static String ID = "NULL";
	public static String OriginalServerIP = "NULL";

	public static int RemoteBalancePort = 0;

	// 自身服务器服务能力
	public static int Start_port = 0;
	public static int End_port = 0;
	// 同级服务器信息 (ID,IP)
	public static HashMap<String, String> Neighbor = new HashMap<>();
	// TODO 考虑一下存储分布表是不是也要放在这里
	private static HashMap<String, ArrayList<String>> ContentMap = new HashMap<>();
}
