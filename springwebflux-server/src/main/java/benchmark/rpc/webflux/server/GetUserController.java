package benchmark.rpc.webflux.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import reactor.core.publisher.Mono;

@RestController
public class GetUserController {
	private final UserService userService = new UserServiceServerImpl();

	@GetMapping("get-user")
	public Mono<User> getUser(int id) {
		return Mono.fromCallable(() -> userService.getUser(id));
	}
}
