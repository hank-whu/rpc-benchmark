package benchmark.rpc.undertow.server;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import benchmark.bean.User;
import benchmark.rpc.util.ByteBufferUtils;
import benchmark.rpc.util.JsonUtils;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class GetUserHandler implements HttpHandler {

	private final ObjectMapper objectMapper = JsonUtils.objectMapper;
	private final UserService userService = new UserServiceServerImpl();

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		Map<String, Deque<String>> params = exchange.getQueryParameters();
		String idStr = params.get("id").getFirst();
		long id = Integer.parseInt(idStr);

		User user = userService.getUser(id);

		byte[] bytes = objectMapper.writeValueAsBytes(user);
		ByteBuffer buffer = ByteBufferUtils.allocate(bytes);

		exchange.getResponseSender().send(buffer);
	}

}
