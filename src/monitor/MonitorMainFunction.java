package monitor;

public class MonitorMainFunction {

	public static void main(String[] args) throws Exception {
		String back = null;

		// ------------LocalServer--------------------
		for (int i = 1; i <= 3; i++) {
			// SET
			back = MonitorSetting.setLocalServer(i);
			System.out.println(back);

			// HELLO
			back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090, "LocalServer", "TASK_001");
			System.out.println(back);

			// INIT
			back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090, "LocalServer", "TASK_004");
			System.out.println(back);

			// START
			back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090, "LocalServer", "TASK_005");
			System.out.println(back);
		}

		// stop
		// for (int i = 1; i <= 3; i++) {
		// back =
		// MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090,
		// "LocalServer", "TASK_006");
		// System.out.println(back);
		// }

		// ------------OriginalServer--------------------
		back = MonitorSetting.setOriginalServer();
		System.out.println(back);
		back = MonitorSetting.Usually(MonitorSetting.original_server, 8090, "OriginalServer", "TASK_001");
		System.out.println(back);
		back = MonitorSetting.Usually(MonitorSetting.original_server, 8090, "OriginalServer", "TASK_008");
		System.out.println(back);
		back = MonitorSetting.Usually(MonitorSetting.original_server, 8090, "OriginalServer", "TASK_009");
		System.out.println(back);
		// stop
		// back = MonitorSetting.Usually(MonitorSetting.original_server, 8090,
		// "OriginalServer", "TASK_010");
		// System.out.println(back);

		// ------------User-------------------
		for (int i = 1; i <= 3; i++) {
			// SET
			back = MonitorSetting.setUser(i);
			System.out.println(back);

			// LOAD
			back = MonitorSetting.UserLoadTask(i);
			System.out.println(back);
		}

		// START
		for (int i = 1; i <= 3; i++) {
			back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8091, "USER", "TASK_012");
			System.out.println(back);
		}

	}
}
