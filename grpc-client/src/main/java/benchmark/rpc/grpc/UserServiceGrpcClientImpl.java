package benchmark.rpc.grpc;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.rpc.grpc.UserServiceOuterClass.CreateUserResponse;
import benchmark.rpc.grpc.UserServiceOuterClass.GetUserRequest;
import benchmark.rpc.grpc.UserServiceOuterClass.ListUserRequest;
import benchmark.rpc.grpc.UserServiceOuterClass.UserExistRequest;
import benchmark.rpc.grpc.UserServiceOuterClass.UserExistResponse;
import benchmark.rpc.grpc.UserServiceOuterClass.UserPage;
import benchmark.service.UserService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class UserServiceGrpcClientImpl implements UserService, Closeable {

    private final String host = "benchmark-server";
    private final int port = 8080;

    private final List<ManagedChannel> channels = new ArrayList<>();
    private final List<UserServiceGrpc.UserServiceFutureStub> userServiceFutureStubs = new ArrayList<>();
    private final int channelCount = 6;
    // care thread safe?
    private int currentIndex = 0;

    public UserServiceGrpcClientImpl() {
        for (int i = 0; i < channelCount; i++) {
            ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                    .forAddress(host, port)
                    .directExecutor()
                    .usePlaintext();
            ManagedChannel channel = channelBuilder.build();
            channels.add(channel);
            userServiceFutureStubs.add(UserServiceGrpc.newFutureStub(channel));
        }
    }

    @Override
    public void close() throws IOException {
        channels.forEach(channel -> {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private UserServiceGrpc.UserServiceFutureStub userServiceFutureStub() {
        int index = (currentIndex + 1) % channelCount;
        currentIndex = index;
        return userServiceFutureStubs.get(index);
    }

    @Override
    public boolean existUser(String email) {
        UserExistRequest request = UserExistRequest.newBuilder().setEmail(email).build();
        UserExistResponse response = null;
        try {
            response = userServiceFutureStub().userExist(request).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return response.getExist();
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

        CreateUserResponse response = null;
        try {
            response = userServiceFutureStub().createUser(request).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return response.getSuccess();
    }

    @Override
    public User getUser(long id) {
        GetUserRequest request = GetUserRequest.newBuilder().setId(id).build();
        UserServiceOuterClass.User response = null;
        try {
            response = userServiceFutureStub().getUser(request).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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
        UserPage response = null;
        try {
            response = userServiceFutureStub().listUser(request).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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
