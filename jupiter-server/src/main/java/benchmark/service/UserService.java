package benchmark.service;

import org.jupiter.rpc.ServiceProvider;

import benchmark.bean.Page;
import benchmark.bean.User;

@ServiceProvider(group = "test", name = "userService")
public interface UserService {
	public boolean existUser(String email);

	public boolean createUser(User user);

	public User getUser(long id);

	public Page<User> listUser(int pageNo);

}
