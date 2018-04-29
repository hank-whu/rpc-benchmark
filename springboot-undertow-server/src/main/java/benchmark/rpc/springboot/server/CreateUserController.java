package benchmark.rpc.springboot.server;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

@RestController
public class CreateUserController {
	private final UserService userService = new UserServiceServerImpl();

	@PostMapping("create-user")
	public boolean createUser(User user) {
		return userService.createUser(user);
	}
}
