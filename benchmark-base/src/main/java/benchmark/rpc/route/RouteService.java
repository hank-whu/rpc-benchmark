package benchmark.rpc.route;

import static benchmark.service.ServiceRegister.CREATE_USER;
import static benchmark.service.ServiceRegister.EXIST_USER;
import static benchmark.service.ServiceRegister.GET_USER;
import static benchmark.service.ServiceRegister.LIST_USER;

import benchmark.bean.User;
import benchmark.service.UserService;
import benchmark.service.UserServiceServerImpl;

public class RouteService {

	private final UserService userService = new UserServiceServerImpl();

	public Object invoke(int serviceId, Object[] params) {

		switch (serviceId) {
		case EXIST_USER:
			return userService.existUser((String) params[0]);

		case CREATE_USER:
			return userService.createUser((User) params[0]);

		case GET_USER:
			return userService.getUser((Long) params[0]);

		case LIST_USER:
			return userService.listUser((Integer) params[0]);

		default:
			break;
		}

		return null;
	}
}
