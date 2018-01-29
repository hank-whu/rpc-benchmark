package benchmark.service;

import benchmark.bean.MotanUser;
import benchmark.bean.Page;

public interface MotanUserService {
	public boolean existUser(String email);

	public boolean createUser(MotanUser user);

	public MotanUser getUser(long id);

	public Page<MotanUser> listUser(int pageNo);

}
