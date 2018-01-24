package benchmark.rpc.grpc.server;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import benchmark.bean.Page;
import benchmark.rpc.grpc.UserServiceGrpc.UserServiceImplBase;
import benchmark.rpc.grpc.UserServiceOuterClass.CreateUserResponse;
import benchmark.rpc.grpc.UserServiceOuterClass.GetUserRequest;
import benchmark.rpc.grpc.UserServiceOuterClass.ListUserRequest;
import benchmark.rpc.grpc.UserServiceOuterClass.User;
import benchmark.rpc.grpc.UserServiceOuterClass.UserExistRequest;
import benchmark.rpc.grpc.UserServiceOuterClass.UserExistResponse;
import benchmark.rpc.grpc.UserServiceOuterClass.UserPage;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;
import io.grpc.stub.StreamObserver;

public class UserServiceGrpcServerImpl extends UserServiceImplBase {
	private final UserService userService = new UserServiceServerImpl();

	@Override
	public void userExist(UserExistRequest request, StreamObserver<UserExistResponse> responseObserver) {
		String email = request.getEmail();
		boolean isExist = userService.existUser(email);

		UserExistResponse reply = UserExistResponse.newBuilder().setExist(isExist).build();

		responseObserver.onNext(reply);
		responseObserver.onCompleted();
	}

	@Override
	public void getUser(GetUserRequest request, StreamObserver<User> responseObserver) {
		long id = request.getId();
		benchmark.bean.User user = userService.getUser(id);

		User reply = User.newBuilder()//
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

		responseObserver.onNext(reply);
		responseObserver.onCompleted();
	}

	@Override
	public void createUser(User request, StreamObserver<CreateUserResponse> responseObserver) {
		benchmark.bean.User user = new benchmark.bean.User();

		user.setId(request.getId());
		user.setName(request.getName());
		user.setSex(request.getSex());
		user.setBirthday(LocalDate.ofEpochDay(request.getBirthday()));
		user.setEmail(request.getEmail());
		user.setMobile(request.getMobile());
		user.setAddress(request.getAddress());
		user.setIcon(request.getIcon());
		user.setPermissions(request.getPermissionsList());
		user.setStatus(request.getStatus());
		user.setCreateTime(LocalDateTime.ofEpochSecond(request.getCreateTime(), 0, ZoneOffset.UTC));
		user.setUpdateTime(LocalDateTime.ofEpochSecond(request.getUpdateTime(), 0, ZoneOffset.UTC));

		boolean success = userService.createUser(user);

		CreateUserResponse reply = CreateUserResponse.newBuilder().setSuccess(success).build();

		responseObserver.onNext(reply);
		responseObserver.onCompleted();
	}

	@Override
	public void listUser(ListUserRequest request, StreamObserver<UserPage> responseObserver) {
		int pageNo = request.getPageNo();

		Page<benchmark.bean.User> page = userService.listUser(pageNo);

		List<User> userList = new ArrayList<>(page.getResult().size());

		for (benchmark.bean.User user : page.getResult()) {
			User u = User.newBuilder()//
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

			userList.add(u);
		}

		UserPage reply = UserPage.newBuilder()//
				.setPageNo(page.getPageNo())//
				.setTotal(page.getTotal())//
				.addAllResult(userList)//
				.build();

		responseObserver.onNext(reply);
		responseObserver.onCompleted();
	}

}
