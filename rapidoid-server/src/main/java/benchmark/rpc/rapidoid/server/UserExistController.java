package benchmark.rpc.rapidoid.server;

import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;

import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

@Controller
public class UserExistController {

	private final UserService userService = new UserServiceServerImpl();

	@GET("user-exist")
	public boolean emailExist(String email) {
		return userService.existUser(email);
	}
}
