package benchmark.rpc;

import java.net.InetSocketAddress;

import com.linecorp.armeria.server.ServerBuilder;

import benchmark.service.ArmeriaUserServiceServerImpl;

public class Server {

	public static void main(String[] args) throws Exception {
		ServerBuilder sb = new ServerBuilder();

		// Configure an HTTP port.
		sb.http(new InetSocketAddress("benchmark-server", 8080));

		// Using an annotated service object:
		sb.annotatedService(new ArmeriaUserServiceServerImpl());

		sb.build().start().join();

	}

}
