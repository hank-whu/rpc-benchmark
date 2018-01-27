package benchmark.rpc.undertow.server;

import java.util.Deque;
import java.util.Map;

import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import io.undertow.async.handler.AsyncHttpHandler;
import io.undertow.async.io.PooledByteBufferInputStream;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

public class UserExistHandler extends AsyncHttpHandler {

	private final UserService userService = new UserServiceServerImpl();

	@Override
	protected void handleAsyncRequest(HttpServerExchange exchange, PooledByteBufferInputStream content)
			throws Exception {

		Map<String, Deque<String>> params = exchange.getQueryParameters();
		String email = params.get("email").getFirst();

		if (userService.existUser(email)) {
			send(exchange, StatusCodes.OK, "true");
		} else {
			send(exchange, StatusCodes.OK, "false");
		}
	}
}
