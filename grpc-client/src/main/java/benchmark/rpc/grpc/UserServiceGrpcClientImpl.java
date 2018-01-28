package benchmark.rpc.grpc;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.pool.ViberObjectPool;
import benchmark.rpc.grpc.UserServiceOuterClass.CreateUserResponse;
import benchmark.rpc.grpc.UserServiceOuterClass.GetUserRequest;
import benchmark.rpc.grpc.UserServiceOuterClass.ListUserRequest;
import benchmark.rpc.grpc.UserServiceOuterClass.UserExistRequest;
import benchmark.rpc.grpc.UserServiceOuterClass.UserExistResponse;
import benchmark.rpc.grpc.UserServiceOuterClass.UserPage;
import benchmark.service.UserService;

public class UserServiceGrpcClientImpl implements UserService, Closeable {

	private final String host = "benchmark-server";
	private final int port = 8080;

	private static final int NCPU = Runtime.getRuntime().availableProcessors();

	private final ViberObjectPool<GrpcUserServiceClient> clientPool = //
			new ViberObjectPool<>(NCPU, () -> new GrpcUserServiceClient(host, port));

	@Override
	public void close() throws IOException {
		clientPool.close();
	}

	@Override
	public boolean existUser(String email) {
		GrpcUserServiceClient grpcUserServiceClient = clientPool.borrow();
		try {
			UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub = grpcUserServiceClient.userServiceBlockingStub;

			UserExistRequest request = UserExistRequest.newBuilder().setEmail(email).build();
			UserExistResponse response = userServiceBlockingStub.userExist(request);

			return response.getExist();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			clientPool.release(grpcUserServiceClient);
		}

	}

	@Override
	public boolean createUser(User user) {
		benchmark.rpc.grpc.UserServiceOuterClass.User request = benchmark.rpc.grpc.UserServiceOuterClass.User
				.newBuilder()//
				.setId(user.getId())//
				.setName(user.getName())//
				.setSex(user.getSex())//
				.setBirthday((int) (user.getBirthday().toEpochDay()))//
				.setEmail(user.getEmail())//
				.setMobile(user.getMobile())//
				.setAddress(user.getAddress())//
				.setIcon(user.getIcon())//
				.addAllPermissions(user.getPermissions())//
				.setStatus(user.getStatus())//
				.setCreateTime(user.getCreateTime().toEpochSecond(ZoneOffset.UTC))//
				.setUpdateTime(user.getUpdateTime().toEpochSecond(ZoneOffset.UTC))//
				.build();

		GrpcUserServiceClient grpcUserServiceClient = clientPool.borrow();
		try {
			UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub = grpcUserServiceClient.userServiceBlockingStub;

			CreateUserResponse response = userServiceBlockingStub.createUser(request);

			return response.getSuccess();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			clientPool.release(grpcUserServiceClient);
		}

	}

	@Override
	public User getUser(long id) {
		GetUserRequest request = GetUserRequest.newBuilder().setId(id).build();
		benchmark.rpc.grpc.UserServiceOuterClass.User response;

		GrpcUserServiceClient grpcUserServiceClient = clientPool.borrow();
		try {
			UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub = grpcUserServiceClient.userServiceBlockingStub;

			response = userServiceBlockingStub.getUser(request);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			clientPool.release(grpcUserServiceClient);
		}

		User user = new User();
		user.setId(response.getId());
		user.setName(response.getName());
		user.setSex(response.getSex());
		user.setBirthday(LocalDate.ofEpochDay(response.getBirthday()));
		user.setEmail(response.getEmail());
		user.setMobile(response.getMobile());
		user.setAddress(response.getAddress());
		user.setIcon(response.getIcon());
		user.setPermissions(response.getPermissionsList());
		user.setStatus(response.getStatus());
		user.setCreateTime(LocalDateTime.ofEpochSecond(response.getCreateTime(), 0, ZoneOffset.UTC));
		user.setUpdateTime(LocalDateTime.ofEpochSecond(response.getUpdateTime(), 0, ZoneOffset.UTC));

		return user;
	}

	@Override
	public Page<User> listUser(int pageNo) {
		ListUserRequest request = ListUserRequest.newBuilder().setPageNo(pageNo).build();
		UserPage response;

		GrpcUserServiceClient grpcUserServiceClient = clientPool.borrow();
		try {
			UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub = grpcUserServiceClient.userServiceBlockingStub;

			response = userServiceBlockingStub.listUser(request);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			clientPool.release(grpcUserServiceClient);
		}

		Page<User> page = new Page<>();

		page.setPageNo(response.getPageNo());
		page.setTotal(response.getTotal());

		List<User> userList = new ArrayList<>(response.getResultCount());

		for (benchmark.rpc.grpc.UserServiceOuterClass.User u : response.getResultList()) {
			User user = new User();
			user.setId(u.getId());
			user.setName(u.getName());
			user.setSex(u.getSex());
			user.setBirthday(LocalDate.ofEpochDay(u.getBirthday()));
			user.setEmail(u.getEmail());
			user.setMobile(u.getMobile());
			user.setAddress(u.getAddress());
			user.setIcon(u.getIcon());
			user.setPermissions(u.getPermissionsList());
			user.setStatus(u.getStatus());
			user.setCreateTime(LocalDateTime.ofEpochSecond(u.getCreateTime(), 0, ZoneOffset.UTC));
			user.setUpdateTime(LocalDateTime.ofEpochSecond(u.getUpdateTime(), 0, ZoneOffset.UTC));

			userList.add(user);
		}

		page.setResult(userList);

		return page;
	}

}
