package benchmark.rpc.rsocket.server;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.rpc.grpc.UserService;
import benchmark.rpc.grpc.UserServiceOuterClass;
import benchmark.service.UserServiceServerImpl;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class UserServiceRsocketServerImpl implements UserService {

    private final benchmark.service.UserService userService = new UserServiceServerImpl();

    @Override
    public Mono<UserServiceOuterClass.UserExistResponse> userExist(UserServiceOuterClass.UserExistRequest message, ByteBuf metadata) {
        String email = message.getEmail();
        boolean isExist = userService.existUser(email);
        return Mono.just(UserServiceOuterClass.UserExistResponse.newBuilder().setExist(isExist).build());
    }

    @Override
    public Mono<UserServiceOuterClass.CreateUserResponse> createUser(UserServiceOuterClass.User message, ByteBuf metadata) {
        benchmark.bean.User user = new benchmark.bean.User();

        user.setId(message.getId());
        user.setName(message.getName());
        user.setSex(message.getSex());
        user.setBirthday(LocalDate.ofEpochDay(message.getBirthday()));
        user.setEmail(message.getEmail());
        user.setMobile(message.getMobile());
        user.setAddress(message.getAddress());
        user.setIcon(message.getIcon());
        user.setPermissions(message.getPermissionsList());
        user.setStatus(message.getStatus());
        user.setCreateTime(LocalDateTime.ofEpochSecond(message.getCreateTime(), 0, ZoneOffset.UTC));
        user.setUpdateTime(LocalDateTime.ofEpochSecond(message.getUpdateTime(), 0, ZoneOffset.UTC));

        boolean success = userService.createUser(user);

        UserServiceOuterClass.CreateUserResponse reply = UserServiceOuterClass.CreateUserResponse.newBuilder().setSuccess(success).build();
        return Mono.just(reply);
    }

    @Override
    public Mono<UserServiceOuterClass.User> getUser(UserServiceOuterClass.GetUserRequest message, ByteBuf metadata) {
        long id = message.getId();
        benchmark.bean.User user = userService.getUser(id);

        UserServiceOuterClass.User reply = UserServiceOuterClass.User.newBuilder()//
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
        return Mono.just(reply);
    }

    @Override
    public Mono<UserServiceOuterClass.UserPage> listUser(UserServiceOuterClass.ListUserRequest message, ByteBuf metadata) {
        int pageNo = message.getPageNo();

        Page<User> page = userService.listUser(pageNo);

        List<UserServiceOuterClass.User> userList = new ArrayList<>(page.getResult().size());

        for (benchmark.bean.User user : page.getResult()) {
            UserServiceOuterClass.User u = UserServiceOuterClass.User.newBuilder()//
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

        UserServiceOuterClass.UserPage reply = UserServiceOuterClass.UserPage.newBuilder()//
                .setPageNo(page.getPageNo())//
                .setTotal(page.getTotal())//
                .addAllResult(userList)//
                .build();
        return Mono.just(reply);
    }
}
