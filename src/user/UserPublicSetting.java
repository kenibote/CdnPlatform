package user;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import base.TaskInfo;

public class UserPublicSetting {
	// 用于记录monitor的socket信息；
	public static BufferedReader MonitorSocketInput = null;
	public static PrintWriter MonitorSocketWrite = null;
	
	// 链接断开后不会消失的数据
	public static final String Key = "USER";
	public static String ID = "NULL";
	public static String LocalServerIP = "NULL";
	public static int LocalServerPort = 0;
	
	public static ArrayList<TaskInfo> TaskList = new ArrayList<>();
	
	// 用于记录仿真状态
	public static boolean SimulationFlag = false;
	public static double SimulationStatus = 0;
	// 注意，这个filename不带csv
	public static String FilePix = "E:\\";
	public static String FileName = null;
	public static String FileResult = null;
}
