package monitor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import base.FileDownload;
import net.sf.json.JSONObject;

@SuppressWarnings("serial")
public class MonitorWindow extends JFrame {
	public static JPanel panel_server, panel_original, panel_user, panel_info;
	public static JButton b_server_set, b_server_hello, b_server_init, b_server_updatamap, b_server_static_start,
			b_server_static_stop, b_server_start, b_server_stop, b_server_init_like, b_original_set, b_original_init,
			b_original_hello, b_original_start, b_original_stop, b_original_remap, b_original_remap_stop, b_user_set,
			b_user_load, b_user_start, b_user_check, b_collectdata;
	public static JLabel task1, task2, task3;
	public static JTextField[] progress = new JTextField[4];

	public MonitorWindow() {
		panel_server = new JPanel();
		panel_original = new JPanel();
		panel_user = new JPanel();
		panel_info = new JPanel();

		// 初始化按钮
		b_server_set = new JButton("设置本地服务器");
		b_server_hello = new JButton("HELLO");
		b_server_updatamap = new JButton("更新Map");
		b_server_init = new JButton("初始化");
		b_server_start = new JButton("启动");
		b_server_stop = new JButton("STOP");
		b_server_static_start = new JButton("启动统计");
		b_server_static_stop = new JButton("关闭统计");
		b_server_init_like = new JButton("init like");

		b_original_set = new JButton("设置原服务器");
		b_original_hello = new JButton("HELLO");
		b_original_init = new JButton("初始化");
		b_original_start = new JButton("启动");
		b_original_stop = new JButton("STOP");
		b_original_remap = new JButton("ReMapStart");
		b_original_remap_stop = new JButton("ReMapStop");

		b_user_set = new JButton("设置用户仿真");
		b_user_load = new JButton("读取TASK");
		b_user_start = new JButton("开始仿真");
		b_user_check = new JButton("Check");
		b_collectdata = new JButton("回收实验数据");

		task1 = new JLabel("User1:");
		task2 = new JLabel("User2:");
		task3 = new JLabel("User3:");
		progress[1] = new JTextField();
		progress[2] = new JTextField();
		progress[3] = new JTextField();
		progress[1].setPreferredSize(new Dimension(60, 25));
		progress[2].setPreferredSize(new Dimension(60, 25));
		progress[3].setPreferredSize(new Dimension(60, 25));
		progress[1].setText(" 0.0% ");
		progress[2].setText(" 0.0% ");
		progress[3].setText(" 0.0% ");

		// 设置布局
		this.setLayout(new GridLayout(4, 1));

		// 添加按钮
		panel_server.add(b_server_set);
		panel_server.add(b_server_hello);
		panel_server.add(b_server_updatamap);
		panel_server.add(b_server_init);
		panel_server.add(b_server_start);
		panel_server.add(b_server_stop);
		panel_server.add(b_server_init_like);
		panel_server.add(b_server_static_start);
		panel_server.add(b_server_static_stop);

		panel_original.add(b_original_set);
		panel_original.add(b_original_hello);
		panel_original.add(b_original_init);
		panel_original.add(b_original_start);
		panel_original.add(b_original_stop);
		panel_original.add(b_original_remap);
		panel_original.add(b_original_remap_stop);

		panel_user.add(b_user_set);
		panel_user.add(b_user_load);
		panel_user.add(b_user_start);
		panel_user.add(b_user_check);
		panel_user.add(b_collectdata);

		panel_info.add(task1);
		panel_info.add(progress[1]);
		panel_info.add(task2);
		panel_info.add(progress[2]);
		panel_info.add(task3);
		panel_info.add(progress[3]);

		// 加入到JFrame
		this.add(panel_server);
		this.add(panel_original);
		this.add(panel_user);
		this.add(panel_info);

		// 添加动作
		addAction();

		// 设置窗体
		this.setTitle("Super Monitor -- WangNing");// 窗体标签
		this.setSize(900, 165);// 窗体大小
		this.setLocationRelativeTo(null);// 在屏幕中间显示(居中显示)
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 退出关闭JFrame
		this.setVisible(true);// 显示窗体

		// 锁定窗体
		this.setResizable(false);
	}

	public static void addAction() {
		b_server_updatamap.setBackground(Color.yellow);
		b_server_start.setBackground(Color.green);
		b_server_stop.setBackground(Color.red);

		b_original_start.setBackground(Color.green);
		b_original_stop.setBackground(Color.red);

		b_user_load.setBackground(Color.orange);
		b_user_start.setBackground(Color.green);
		b_collectdata.setBackground(Color.magenta);

		b_server_set.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.setLocalServer(i);
					} catch (Exception e1) {
						System.out.println("Local Server Set Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_server_hello.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090, "LocalServer",
								"TASK_001");
					} catch (Exception e1) {
						System.out.println("Local Server HELLO Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_server_updatamap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.SetLocalServerMap(i);
					} catch (Exception e1) {
						System.out.println("Local Server UpdataMap Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_server_init.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090, "LocalServer",
								"TASK_004");
					} catch (Exception e1) {
						System.out.println("Local Server Init Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_server_static_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090, "LocalServer",
								"TASK_015");
					} catch (Exception e1) {
						System.out.println("Local Server Static Start Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_server_static_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090, "LocalServer",
								"TASK_016");
					} catch (Exception e1) {
						System.out.println("Local Server Static Stop Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_server_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090, "LocalServer",
								"TASK_005");
					} catch (Exception e1) {
						System.out.println("Local Server Start Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_server_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8090, "LocalServer",
								"TASK_006");
					} catch (Exception e1) {
						System.out.println("Local Server Stop Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_server_init_like.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.SetLocalServerLike(i);
					} catch (Exception e1) {
						System.out.println("Local Server Init Like Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		// ----------------------------------------------------------------
		b_original_set.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String back = null;
				try {
					back = MonitorSetting.setOriginalServer();
				} catch (Exception e1) {
					System.out.println("OriginalServer Set Fail !!!");
				}
				System.out.println(back);
			}
		});

		b_original_hello.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String back = null;
				try {
					back = MonitorSetting.Usually(MonitorSetting.original_server, 8090, "OriginalServer", "TASK_001");
				} catch (Exception e1) {
					System.out.println("OriginalServer Hello Fail !!!");
				}
				System.out.println(back);
			}
		});

		b_original_init.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String back = null;
				try {
					back = MonitorSetting.Usually(MonitorSetting.original_server, 8090, "OriginalServer", "TASK_008");
				} catch (Exception e1) {
					System.out.println("OriginalServer Init Fail !!!");
				}
				System.out.println(back);
			}
		});

		b_original_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String back = null;
				try {
					back = MonitorSetting.Usually(MonitorSetting.original_server, 8090, "OriginalServer", "TASK_009");
				} catch (Exception e1) {
					System.out.println("OriginalServer Start Fail !!!");
				}
				System.out.println(back);
			}
		});

		b_original_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String back = null;
				try {
					back = MonitorSetting.Usually(MonitorSetting.original_server, 8090, "OriginalServer", "TASK_010");
				} catch (Exception e1) {
					System.out.println("OriginalServer Stop Fail !!!");
				}
				System.out.println(back);
			}
		});

		b_original_remap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MonitorSetting.StartReMapServer("Start");
			}
		});

		b_original_remap_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MonitorSetting.StartReMapServer("Stop");
			}
		});

		// ----------------------------------------------------------------
		b_user_set.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.setUser(i);
					} catch (Exception e1) {
						System.out.println("User Set Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_user_load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.UserLoadTask(i);
					} catch (Exception e1) {
						System.out.println("User Load Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_user_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 以后此处可以顺带先启动统计功能
				// ………………………………………………
				
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8091, "USER",
								"TASK_012");
					} catch (Exception e1) {
						System.out.println("User Start Fail !!!");
					}
					System.out.println(back);
				}

				// 以后此处可以顺带启动ReMap服务
				// MonitorSetting.StartReMapServer("Start");
			}
		});

		b_user_check.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8091, "USER",
								"TASK_013");
						double val = Double.parseDouble(JSONObject.fromObject(back).getString("PROGRESS"));
						val = val * 100;
						String pri = "" + val;
						pri = pri.substring(0, 3) + "%";
						progress[i].setText(pri);

					} catch (Exception e1) {
						System.out.println("User Check Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

		b_collectdata.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 1; i <= 3; i++) {
					String back = null;
					try {
						back = MonitorSetting.Usually(MonitorSetting.local_server_info.get(i), 8091, "USER",
								"TASK_013");
						String FileName = JSONObject.fromObject(back).getString("RESULTNAME") + ".csv";
						FileDownload fd = new FileDownload(MonitorSetting.local_server_info.get(i), 8092, FileName, 0);
						fd.download();

					} catch (Exception e1) {
						System.out.println("Download Result Fail !!!");
					}
					System.out.println(back);
				}
			}
		});

	} // end addAction

	public static void main(String[] args) {
		new MonitorWindow();
	}

}
