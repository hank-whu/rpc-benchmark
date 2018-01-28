package benchmark.rpc.thrift;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.pool.ViberObjectPool;
import benchmark.service.UserServiceServerImpl;

public class UserServiceThriftClientImpl implements benchmark.service.UserService, Closeable {

	private final String host = "benchmark-server";
	private final int port = 8080;

	private static final int NCPU = Runtime.getRuntime().availableProcessors();

	private final ViberObjectPool<ThriftUserServiceClient> clientPool = //
			new ViberObjectPool<>(NCPU, () -> new ThriftUserServiceClient(host, port));

	@Override
	public void close() throws IOException {
		clientPool.close();
	}

	@Override
	public boolean existUser(String email) {
		ThriftUserServiceClient thriftUserServiceClient = clientPool.borrow();
		try {
			return thriftUserServiceClient.client.userExist(email);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			clientPool.release(thriftUserServiceClient);
		}
	}

	@Override
	public boolean createUser(User user) {
		benchmark.rpc.thrift.User thriftUser = Converter.toThrift(user);

		ThriftUserServiceClient thriftUserServiceClient = clientPool.borrow();
		try {
			return thriftUserServiceClient.client.createUser(thriftUser);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			clientPool.release(thriftUserServiceClient);
		}
	}

	@Override
	public User getUser(long id) {

		ThriftUserServiceClient thriftUserServiceClient = clientPool.borrow();
		try {

			benchmark.rpc.thrift.User thriftUser = thriftUserServiceClient.client.getUser(id);
			User user = Converter.toRaw(thriftUser);

			return user;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			clientPool.release(thriftUserServiceClient);
		}
	}

	@Override
	public Page<User> listUser(int pageNo) {

		ThriftUserServiceClient thriftUserServiceClient = clientPool.borrow();
		try {

			UserPage userPage = thriftUserServiceClient.client.listUser(pageNo);
			Page<User> page = new Page<>();

			page.setPageNo(userPage.getPageNo());
			page.setTotal(userPage.getTotal());

			List<User> userList = new ArrayList<>(userPage.getResult().size());

			for (benchmark.rpc.thrift.User thriftUser : userPage.getResult()) {
				User user = Converter.toRaw(thriftUser);

				userList.add(user);
			}

			page.setResult(userList);

			return page;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			clientPool.release(thriftUserServiceClient);
		}

	}

	public static void main(String[] args) throws IOException {
		try (UserServiceThriftClientImpl userService = new UserServiceThriftClientImpl()) {
			System.out.println(userService.existUser("12345"));
			System.out.println(userService.createUser(new UserServiceServerImpl().getUser(1)));
			System.out.println(userService.getUser(5));
			System.out.println(userService.listUser(100));
		}
	}

}
