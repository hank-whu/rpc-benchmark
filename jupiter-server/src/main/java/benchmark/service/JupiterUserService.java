package benchmark.service;

import java.util.concurrent.CompletableFuture;

import org.jupiter.rpc.ServiceProvider;

import benchmark.bean.Page;
import benchmark.bean.User;

@ServiceProvider(group = "test", name = "jupiterUserService")
public interface JupiterUserService {
	CompletableFuture<Boolean> existUser(String email);

	CompletableFuture<Boolean> createUser(User user);

	CompletableFuture<User> getUser(long id);

	CompletableFuture<Page<User>> listUser(int pageNo);

}
