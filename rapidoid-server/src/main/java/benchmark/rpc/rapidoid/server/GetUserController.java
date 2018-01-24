package benchmark.rpc.rapidoid.server;

import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;

import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

@Controller
public class GetUserController {
	private final UserService userService = new UserServiceServerImpl();

	@GET("get-user")
	public User getUser(int id) {
		User user = userService.getUser(id);
		return user;
	}
}
