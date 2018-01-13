package base;

public class TaskInfo {
	// 任务启动等待时间
	public int wait_time = 0;
	// 任务归属
	public String task_from = null;
	public int load_balance_port = 8070;
	// 任务id
	public int task_id = 0;
	// 任务需要下载的文件
	public String file_name = null;

	// ---------------------------------------------

	// 任务最重由谁服务
	public String task_server_ip = null;
	public int task_server_port = 0;
	// 任务状态
	public boolean task_flag = false;

	// 任务重定向时间
	public long redirect_time = 0;
	// 任务下载时间
	public long download_time = 0;
}
