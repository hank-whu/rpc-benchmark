package benchmark.rpc.rapidoid.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

@RestController
public class GetUserController {
	private final UserService userService = new UserServiceServerImpl();

	@GetMapping("get-user")
	public User getUser(int id) {
		User user = userService.getUser(id);
		return user;
	}
}
