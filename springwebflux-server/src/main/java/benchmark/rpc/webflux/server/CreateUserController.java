package benchmark.rpc.webflux.server;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import reactor.core.publisher.Mono;

@RestController
public class CreateUserController {
	private final UserService userService = new UserServiceServerImpl();

	@PostMapping("create-user")
	public Mono<Boolean> createUser(User user) {
		return Mono.fromCallable(() -> userService.createUser(user));

	}
}
