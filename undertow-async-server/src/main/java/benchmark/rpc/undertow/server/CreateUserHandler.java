package benchmark.rpc.undertow.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import benchmark.bean.User;
import benchmark.rpc.util.JsonUtils;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import io.undertow.async.handler.AsyncHttpHandler;
import io.undertow.async.io.PooledByteBufferInputStream;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

public class CreateUserHandler extends AsyncHttpHandler {

	private final ObjectMapper objectMapper = JsonUtils.objectMapper;
	private final UserService userService = new UserServiceServerImpl();

	@Override
	protected void handleAsyncRequest(HttpServerExchange exchange, PooledByteBufferInputStream content)
			throws Exception {

		byte[] bytes = readBytesAndClose(content);
		User user = objectMapper.readValue(bytes, User.class);
		userService.createUser(user);

		send(exchange, StatusCodes.OK, "true");
	}

}
