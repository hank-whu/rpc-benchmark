package benchmark.rpc.netty.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.rpc.protocol.Request;
import benchmark.rpc.protocol.Response;
import benchmark.service.ServiceRegister;
import benchmark.service.UserService;

public class UserServiceNettyClientImpl implements UserService, Closeable {

	public static final String host = "127.0.0.1";
	public static final int port = 8080;

	private final NettyClientConnector connector = new NettyClientConnector(host, port);
	private final AtomicLong requestCounter = new AtomicLong();

	public UserServiceNettyClientImpl() {
		connect();
	}

	@Override
	public void close() throws IOException {
		connector.close();
	}

	private void connect() {
		try {
			connector.connect();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T execute(int serviceId, Object... params) {
		long requestId = requestCounter.getAndIncrement();

		try {
			Request request = new Request();
			request.setRequestId(requestId);
			request.setServiceId(serviceId);
			request.setParams(params);

			Response response = connector.execute(request);
			return (T) response.getResult();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public boolean existUser(String email) {
		return execute(ServiceRegister.EXIST_USER, email);
	}

	@Override
	public boolean createUser(User user) {
		return execute(ServiceRegister.CREATE_USER, user);
	}

	@Override
	public User getUser(long id) {
		return execute(ServiceRegister.GET_USER, id);
	}

	@Override
	public Page<User> listUser(int pageNo) {
		return execute(ServiceRegister.LIST_USER, pageNo);
	}

}
