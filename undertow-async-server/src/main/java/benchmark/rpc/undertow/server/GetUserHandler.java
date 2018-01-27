package benchmark.rpc.undertow.server;

import java.util.Deque;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

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

public class GetUserHandler extends AsyncHttpHandler {

	private final ObjectMapper objectMapper = JsonUtils.objectMapper;
	private final UserService userService = new UserServiceServerImpl();

	@Override
	protected void handleAsyncRequest(HttpServerExchange exchange, PooledByteBufferInputStream content)
			throws Exception {

		Map<String, Deque<String>> params = exchange.getQueryParameters();
		String idStr = params.get("id").getFirst();
		long id = Integer.parseInt(idStr);

		User user = userService.getUser(id);

		ByteBufferPool pool = exchange.getConnection().getByteBufferPool();
		PooledByteBufferOutputStream output = new PooledByteBufferOutputStream(pool);
		objectMapper.writeValue(output, user);

		send(exchange, StatusCodes.OK, output);
	}
}
