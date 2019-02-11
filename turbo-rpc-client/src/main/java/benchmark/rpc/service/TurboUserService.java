package benchmark.rpc.service;

import java.util.concurrent.CompletableFuture;

import benchmark.bean.Page;
import benchmark.bean.User;
import rpc.turbo.annotation.TurboService;

@TurboService(version = "1.0.0", rest = "user")
public interface TurboUserService {

	@TurboService(version = "2.1.2", rest = "exist")
	public CompletableFuture<Boolean> existUser(String email);

	@TurboService(version = "2.1.2", rest = "create")
	public CompletableFuture<Boolean> createUser(User user);

	@TurboService(version = "2.1.2", rest = "get")
	public CompletableFuture<User> getUser(long id);

	@TurboService(version = "1.2.1", rest = "list")
	public CompletableFuture<Page<User>> listUser(int pageNo);

}
