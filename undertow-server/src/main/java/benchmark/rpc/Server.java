package benchmark.rpc;

import benchmark.rpc.undertow.server.CreateUserHandler;
import benchmark.rpc.undertow.server.UserExistHandler;
import benchmark.rpc.undertow.server.GetUserHandler;
import benchmark.rpc.undertow.server.ListUserHandler;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;

public class Server {

	public static final String host = "benchmark-server";
	public static final int port = 8080;

	public static void main(String[] args) {
		Undertow.builder()//
				.addHttpListener(port, host)//
				.setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)//
				.setServerOption(UndertowOptions.ALWAYS_SET_DATE, false)//
				.setHandler(paths())//
				.build()//
				.start();
	}

	private static HttpHandler paths() {
		return new PathHandler()//
				.addExactPath("/user-exist", new UserExistHandler())//
				.addExactPath("/create-user", new CreateUserHandler())//
				.addExactPath("/get-user", new GetUserHandler())//
				.addExactPath("/list-user", new ListUserHandler())//
		;
	}

}
