package benchmark.rpc.undertow.server;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.rpc.util.ByteBufferUtils;
import benchmark.rpc.util.JsonUtils;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class ListUserHandler implements HttpHandler {

	private final ObjectMapper objectMapper = JsonUtils.objectMapper;
	private final UserService userService = new UserServiceServerImpl();

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		Map<String, Deque<String>> params = exchange.getQueryParameters();
		String pageNoStr = params.get("pageNo").getFirst();
		int pageNo = Integer.parseInt(pageNoStr);

		Page<User> userList = userService.listUser(pageNo);

		byte[] bytes = objectMapper.writeValueAsBytes(userList);
		ByteBuffer buffer = ByteBufferUtils.allocate(bytes);

		exchange.getResponseSender().send(buffer);
	}

}
