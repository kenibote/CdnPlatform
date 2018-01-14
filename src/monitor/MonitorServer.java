package monitor;

import base.FileServerControl;

public class MonitorServer {
	public static void main(String[] args) {
		FileServerControl fc = new FileServerControl();
		new Thread(fc).start();
		
		FileServerControl.setPort(8095, 8096);
		FileServerControl.initFileServer();
		FileServerControl.startFileServer();
	}

}
