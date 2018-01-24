package benchmark.rpc.undertow.server;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.Map;

import benchmark.rpc.util.ByteBufferUtils;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class UserExistHandler implements HttpHandler {

	private final ByteBuffer trueResult = ByteBufferUtils.allocateDirect("true");
	private final ByteBuffer falseResult = ByteBufferUtils.allocateDirect("false");
	private final UserService userService = new UserServiceServerImpl();

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		Map<String, Deque<String>> params = exchange.getQueryParameters();
		String email = params.get("email").getFirst();

		if (userService.existUser(email)) {
			exchange.getResponseSender().send(trueResult.duplicate());
		} else {
			exchange.getResponseSender().send(falseResult.duplicate());
		}
	}

}
