package benchmark.rpc.rapidoid.server;

import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

@Controller
public class ListUserController {
	private final UserService userService = new UserServiceServerImpl();

	@GET("list-user")
	public Page<User> getUser(int pageNo) {
		Page<User> userList = userService.listUser(pageNo);
		return userList;
	}
}
