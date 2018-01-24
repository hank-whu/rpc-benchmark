package benchmark.rpc.tars;

import java.util.ArrayList;
import java.util.List;

import benchmark.bean.Page;
import benchmark.service.UserServiceServerImpl;

public class UserServiceTarsServerImpl implements TarsUserServiceServant {

	private final benchmark.service.UserService userService = new UserServiceServerImpl();

	@Override
	public boolean existUser(String email) {
		return userService.existUser(email);
	}

	@Override
	public boolean createUser(TarsUser user) {
		benchmark.bean.User rawUser = Converter.toRaw(user);
		return userService.createUser(rawUser);
	}

	@Override
	public TarsUser getUser(long id) {
		benchmark.bean.User rawUser = userService.getUser(id);
		TarsUser user = Converter.toTars(rawUser);

		return user;
	}

	@Override
	public TarsPage listUser(int pageNo) {
		Page<benchmark.bean.User> page = userService.listUser(pageNo);

		List<TarsUser> userList = new ArrayList<>();
		for (benchmark.bean.User rawUser : page.getResult()) {
			TarsUser user = Converter.toTars(rawUser);
			userList.add(user);
		}

		TarsPage userPage = new TarsPage();
		userPage.setPageNo(page.getPageNo());
		userPage.setTotal(page.getTotal());
		userPage.setResult(userList);

		return userPage;
	}

}
