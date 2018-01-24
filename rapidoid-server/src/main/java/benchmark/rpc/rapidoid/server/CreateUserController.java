package benchmark.rpc.rapidoid.server;

import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;

import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

@Controller
public class CreateUserController {
	private final UserService userService = new UserServiceServerImpl();

	@GET("create-user")
	public boolean createUser(User user) {
		return userService.createUser(user);
	}
}
