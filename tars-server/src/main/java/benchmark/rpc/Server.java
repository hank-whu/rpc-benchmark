package benchmark.rpc;

import java.io.File;

import com.qq.tars.server.startup.Main;

public class Server {

	public static void main(String[] args) throws InterruptedException {
		new File("/tmp/data").mkdirs();
		new File("/tmp/log").mkdirs();

		Main.main(args);
	}

}
