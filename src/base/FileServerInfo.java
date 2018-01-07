package base;

public class FileServerInfo {
	public int port = 0;
	public Thread thread = null;
	public FileServer fileserver = null;
	public FileServerStatus fss = FileServerStatus.OFF;
}
