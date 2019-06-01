package benchmark.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.RequestObject;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.rpc.util.JsonUtils;

/**
 * only for server
 * 
 * @author Hank
 *
 */
public class ArmeriaUserServiceServerImpl {

	private final ObjectMapper objectMapper = JsonUtils.objectMapper;

	@Get("/user-exist")
	public HttpResponse existUser(@Param("email") String email) {

		if (email == null || email.isEmpty()) {
			return HttpResponse.of(String.valueOf(Boolean.TRUE));
		}

		if (email.charAt(email.length() - 1) < '5') {
			return HttpResponse.of(String.valueOf(Boolean.FALSE));
		}

		return HttpResponse.of(String.valueOf(Boolean.TRUE));

	}

	@Get("/get-user")
	public HttpResponse getUser(@Param("id") long id) throws JsonProcessingException {
		User user = new User();

		user.setId(id);
		user.setName("Doug Lea");
		user.setSex(1);
		user.setBirthday(LocalDate.of(1968, 12, 8));
		user.setEmail("dong.lea@gmail.com");
		user.setMobile("18612345678");
		user.setAddress("北京市 中关村 中关村大街1号 鼎好大厦 1605");
		user.setIcon("https://www.baidu.com/img/bd_logo1.png");
		user.setStatus(1);
		user.setCreateTime(LocalDateTime.now());
		user.setUpdateTime(user.getCreateTime());

		List<Integer> permissions = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 19, 88, 86, 89, 90, 91, 92));
		user.setPermissions(permissions);

		return HttpResponse.of(objectMapper.writeValueAsString(user));
	}

	@Get("/list-user")
	public HttpResponse listUser(@Param("pageNo") int pageNo) throws JsonProcessingException {
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

			List<Integer> permissions = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 19, 88, 86, 89, 90, 91, 92));
			user.setPermissions(permissions);

			userList.add(user);
		}

		Page<User> page = new Page<>();
		page.setPageNo(pageNo);
		page.setTotal(1000);
		page.setResult(userList);

		return HttpResponse.of(objectMapper.writeValueAsString(page));
	}

	@Post("/create-user")
	public HttpResponse createUser(@RequestObject User user) {
		if (user == null) {
			return HttpResponse.of(String.valueOf(Boolean.FALSE));
		}

		return HttpResponse.of(String.valueOf(Boolean.TRUE));
	}

}
