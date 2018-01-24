package benchmark.rpc.thrift;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import benchmark.bean.Page;
import benchmark.service.UserServiceServerImpl;

public class UserServiceThriftServerImpl implements UserService.Iface {

	private final benchmark.service.UserService userService = new UserServiceServerImpl();

	@Override
	public boolean userExist(String email) throws TException {
		return userService.existUser(email);
	}

	@Override
	public boolean createUser(User user) throws TException {
		benchmark.bean.User rawUser = Converter.toRaw(user);
		return userService.createUser(rawUser);
	}

	@Override
	public User getUser(long id) throws TException {
		benchmark.bean.User rawUser = userService.getUser(id);
		User user = Converter.toThrift(rawUser);

		return user;
	}

	@Override
	public UserPage listUser(int pageNo) throws TException {
		Page<benchmark.bean.User> page = userService.listUser(pageNo);

		List<User> userList = new ArrayList<>();
		for (benchmark.bean.User rawUser : page.getResult()) {
			User user = Converter.toThrift(rawUser);
			userList.add(user);
		}

		UserPage userPage = new UserPage();
		userPage.setPageNo(page.getPageNo());
		userPage.setTotal(page.getTotal());
		userPage.setResult(userList);

		return userPage;
	}

}
