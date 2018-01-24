package benchmark.rpc.webflux.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import reactor.core.publisher.Mono;

@RestController
public class UserExistController {

	private final UserService userService = new UserServiceServerImpl();

	@GetMapping("user-exist")
	public Mono<Boolean> emailExist(String email) {
		return Mono.fromCallable(() -> userService.existUser(email));
	}
}
