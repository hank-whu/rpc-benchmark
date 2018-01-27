package benchmark.service;

import org.jupiter.rpc.ServiceProvider;

import benchmark.bean.Page;
import benchmark.bean.User;

@ServiceProvider(group = "test", name = "jupiterUserService")
public interface JupiterUserService {
	public boolean existUser(String email);

	public boolean createUser(User user);

	public User getUser(long id);

	public Page<User> listUser(int pageNo);

}
