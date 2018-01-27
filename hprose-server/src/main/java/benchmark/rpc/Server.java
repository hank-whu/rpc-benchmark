package benchmark.rpc;

import benchmark.service.UserServiceServerImpl;
import hprose.server.HproseTcpServer;

public class Server {

	public static void main(String[] args) throws Exception {
		HproseTcpServer server = new HproseTcpServer("benchmark-server", 8080);
		server.add(new UserServiceServerImpl());
		server.start();
		System.out.println("START");
		Thread.sleep(Long.MAX_VALUE);
	}

}
