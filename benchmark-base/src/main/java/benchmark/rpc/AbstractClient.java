package benchmark.rpc;

import java.util.concurrent.atomic.AtomicInteger;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

public abstract class AbstractClient {
	private final AtomicInteger counter = new AtomicInteger(0);
	private final UserService _serviceUserService = new UserServiceServerImpl();

	protected abstract UserService getUserService();

	public boolean existUser() throws Exception {
		String email = String.valueOf(counter.getAndIncrement());
		return getUserService().existUser(email);
	}

	public boolean createUser() throws Exception {
		int id = counter.getAndIncrement();
		User user = _serviceUserService.getUser(id);
		return getUserService().createUser(user);
	}

	public User getUser() throws Exception {
		int id = counter.getAndIncrement();
		return getUserService().getUser(id);
	}

	public Page<User> listUser() throws Exception {
		int pageNo = counter.getAndIncrement();
		return getUserService().listUser(pageNo);
	}

}
