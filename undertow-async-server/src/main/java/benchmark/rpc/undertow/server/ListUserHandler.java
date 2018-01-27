package benchmark.rpc.undertow.server;

import java.util.Deque;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.rpc.util.JsonUtils;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import io.undertow.async.handler.AsyncHttpHandler;
import io.undertow.async.io.PooledByteBufferInputStream;
import io.undertow.async.io.PooledByteBufferOutputStream;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

public class ListUserHandler extends AsyncHttpHandler {

	private final ObjectMapper objectMapper = JsonUtils.objectMapper;
	private final UserService userService = new UserServiceServerImpl();

	@Override
	protected void handleAsyncRequest(HttpServerExchange exchange, PooledByteBufferInputStream content)
			throws Exception {

		Map<String, Deque<String>> params = exchange.getQueryParameters();
		String pageNoStr = params.get("pageNo").getFirst();
		int pageNo = Integer.parseInt(pageNoStr);

		Page<User> userList = userService.listUser(pageNo);

		ByteBufferPool pool = exchange.getConnection().getByteBufferPool();
		PooledByteBufferOutputStream output = new PooledByteBufferOutputStream(pool);
		objectMapper.writeValue(output, userList);

		send(exchange, StatusCodes.OK, output);
	}
}
