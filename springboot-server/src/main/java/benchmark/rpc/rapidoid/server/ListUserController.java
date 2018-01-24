package benchmark.rpc.rapidoid.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

@RestController
public class ListUserController {
	private final UserService userService = new UserServiceServerImpl();

	@GetMapping("list-user")
	public Page<User> getUser(int pageNo) {
		Page<User> userList = userService.listUser(pageNo);
		return userList;
	}
}
