package benchmark.rpc.aeron.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.rpc.protocol.Request;
import benchmark.rpc.protocol.Response;
import benchmark.service.ServiceRegister;
import benchmark.service.UserService;

public class UserServiceAeronClientImpl implements UserService, Closeable {

	private final AeronClientConnector connector = new AeronClientConnector();
	private final AtomicLong requestCounter = new AtomicLong();

	public UserServiceAeronClientImpl() {
		connect();
	}

	@Override
	public void close() throws IOException {
		connector.close();
	}

	private void connect() {
		try {
			connector.connect();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T execute(int serviceId, Object... params) {
		long requestId = requestCounter.getAndIncrement();

		Request request = new Request();
		request.setRequestId(requestId);
		request.setServiceId(serviceId);
		request.setParams(params);

		try {
			Response response = connector.execute(request);
			return (T) response.getResult();
		} catch (Exception e) {
			System.err.println("出现异常,requestId:" + requestId);
			return null;
		}

	}

	@Override
	public boolean existUser(String email) {
		Boolean result = execute(ServiceRegister.EXIST_USER, email);
		return result == Boolean.TRUE;
	}

	@Override
	public boolean createUser(User user) {
		Boolean result = execute(ServiceRegister.CREATE_USER, user);
		return result == Boolean.TRUE;
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
