package benchmark.rpc.rsocket;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.rpc.grpc.UserServiceClient;
import benchmark.rpc.grpc.UserServiceOuterClass;
import benchmark.service.UserService;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class UserServiceRsocketClientImpl implements UserService, Closeable {

    RSocket rSocket;
    UserServiceClient userServiceClient;

    public UserServiceRsocketClientImpl() {
        this.rSocket =
                RSocketFactory.connect().transport(TcpClientTransport.create(8080)).start().block();
        this.userServiceClient = new UserServiceClient(rSocket);
    }

    @Override
    public boolean existUser(String email) {
        UserServiceOuterClass.UserExistRequest request = UserServiceOuterClass.UserExistRequest.newBuilder()
                .setEmail(email)
                .build();
        return userServiceClient.userExist(request).block().getExist();
    }

    @Override
    public boolean createUser(User user) {
        UserServiceOuterClass.User param = UserServiceOuterClass.User.newBuilder()
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
        return userServiceClient.createUser(param).block().getSuccess();
    }

    @Override
    public User getUser(long id) {
        UserServiceOuterClass.GetUserRequest request = UserServiceOuterClass.GetUserRequest.newBuilder()
                .setId(id)
                .build();
        return userServiceClient.getUser(request).map(response -> {

            return from(response);
        }).block();
    }

    private User from(UserServiceOuterClass.User response){
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
        UserServiceOuterClass.ListUserRequest request = UserServiceOuterClass.ListUserRequest.newBuilder()
                .setPageNo(pageNo)
                .build();
        Page<User> page = new Page<>();

        UserServiceOuterClass.UserPage userPage = userServiceClient.listUser(request).block();
        page.setPageNo(userPage.getPageNo());
        page.setTotal(userPage.getTotal());

        List<User> userList = userPage.getResultList()
                .stream()
                .map(response -> from(response))
                .collect(Collectors.toList());

        page.setResult(userList);

        return page;
    }

    @Override
    public void close() throws IOException {

    }
}
