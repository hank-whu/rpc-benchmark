package benchmark.rpc.webflux.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import reactor.core.publisher.Mono;

@RestController
public class ListUserController {
	private final UserService userService = new UserServiceServerImpl();

	@GetMapping("list-user")
	public Mono<Page<User>> getUser(int pageNo) {
		return Mono.fromCallable(() -> userService.listUser(pageNo));
	}

}
