package benchmark.rpc.rapidoid.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

@RestController
public class UserExistController {

	private final UserService userService = new UserServiceServerImpl();

	@GetMapping("user-exist")
	public boolean emailExist(String email) {
		return userService.existUser(email);
	}
}
