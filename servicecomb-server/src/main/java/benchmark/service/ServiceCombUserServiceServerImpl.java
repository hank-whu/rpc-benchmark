package benchmark.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.provider.pojo.RpcSchema;

import benchmark.bean.Page;
import benchmark.bean.User;

@RpcSchema(schemaId = "rpc-benchmark")
public class ServiceCombUserServiceServerImpl implements UserService {

	@Override
	public boolean existUser(String email) {
		if (email == null || email.isEmpty()) {
			return true;
		}

		if (email.charAt(email.length() - 1) < '5') {
			return false;
		}

		return true;
	}

	@Override
	public User getUser(long id) {
		User user = new User();

		user.setId(id);
		user.setName(new String("Doug Lea"));
		user.setSex(1);
		user.setBirthday(LocalDate.of(1968, 12, 8));
		user.setEmail(new String("dong.lea@gmail.com"));
		user.setMobile(new String("18612345678"));
		user.setAddress(new String("北京市 中关村 中关村大街1号 鼎好大厦 1605"));
		user.setIcon(new String("https://www.baidu.com/img/bd_logo1.png"));
		user.setStatus(1);
		user.setCreateTime(LocalDateTime.now());
		user.setUpdateTime(user.getCreateTime());

		List<Integer> permissions = new ArrayList<Integer>(
				Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 19, 88, 86, 89, 90, 91, 92));

		user.setPermissions(permissions);

		return user;
	}

	@Override
	public Page<User> listUser(int pageNo) {
		List<User> userList = new ArrayList<>(15);

		for (int i = 0; i < 15; i++) {
			User user = new User();

			user.setId(i);
			user.setName("Doug Lea" + i);
			user.setSex(1);
			user.setBirthday(LocalDate.of(1968, 12, 8));
			user.setEmail("dong.lea@gmail.com" + i);
			user.setMobile("18612345678" + i);
			user.setAddress("北京市 中关村 中关村大街1号 鼎好大厦 1605" + i);
			user.setIcon("https://www.baidu.com/img/bd_logo1.png" + i);
			user.setStatus(1);
			user.setCreateTime(LocalDateTime.now());
			user.setUpdateTime(user.getCreateTime());

			List<Integer> permissions = new ArrayList<Integer>(
					Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 19, 88, 86, 89, 90, 91, 92));
			user.setPermissions(permissions);

			userList.add(user);
		}

		Page<User> page = new Page<>();
		page.setPageNo(pageNo);
		page.setTotal(1000);
		page.setResult(userList);

		return page;
	}

	@Override
	public boolean createUser(User user) {
		if (user == null) {
			return false;
		}

		return true;
	}

}
