package benchmark.rpc;

import benchmark.rpc.grpc.server.UserServiceGrpcServerImpl;
import io.grpc.ServerBuilder;

public class Server {

	public static final String host = "benchmark-server";
	public static final int port = 8080;

	public static void main(String[] args) throws Exception {
		ServerBuilder//
				.forPort(port)//
				.addService(new UserServiceGrpcServerImpl())//
				.build()//
				.start()//
				.awaitTermination();
	}

}
