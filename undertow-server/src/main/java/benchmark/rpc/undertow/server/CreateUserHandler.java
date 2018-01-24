package benchmark.rpc.undertow.server;

import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.ObjectMapper;

import benchmark.bean.User;
import benchmark.rpc.util.ByteBufferUtils;
import benchmark.rpc.util.JsonUtils;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class CreateUserHandler implements HttpHandler {

	private final ByteBuffer trueResult = ByteBufferUtils.allocateDirect("true");
	private final ByteBuffer falseResult = ByteBufferUtils.allocateDirect("false");

	private final ObjectMapper objectMapper = JsonUtils.objectMapper;
	private final UserService userService = new UserServiceServerImpl();

	@Override
	public void handleRequest(HttpServerExchange _exchange) throws Exception {
		_exchange.getRequestReceiver().receiveFullBytes(//
				(exchange, data) -> {// do stuff with the data
					try {
						User user = objectMapper.readValue(data, User.class);
						userService.createUser(user);

						exchange.getResponseSender().send(trueResult.duplicate());
					} catch (Exception e) {
						e.printStackTrace();
						exchange.setStatusCode(500);
						exchange.getResponseSender().send(falseResult.duplicate());
					}
				}, //
				(exchange, exception) -> {// optional error handler
					exchange.setStatusCode(500);
					exchange.getResponseSender().send(falseResult.duplicate());
				});
	}

}
