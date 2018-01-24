package benchmark.rpc.tars;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Converter {

	public static TarsUser toTars(benchmark.bean.User user) {
		TarsUser tarsUser = new TarsUser();

		tarsUser.setId(user.getId());
		tarsUser.setName(user.getName());
		tarsUser.setSex(user.getSex());
		tarsUser.setBirthday((int) (user.getBirthday().toEpochDay()));
		tarsUser.setEmail(user.getEmail());
		tarsUser.setMobile(user.getMobile());
		tarsUser.setAddress(user.getAddress());
		tarsUser.setIcon(user.getIcon());
		tarsUser.setPermissions(user.getPermissions());
		tarsUser.setStatus(user.getStatus());
		tarsUser.setCreateTime(user.getCreateTime().toEpochSecond(ZoneOffset.UTC));
		tarsUser.setUpdateTime(user.getUpdateTime().toEpochSecond(ZoneOffset.UTC));

		return tarsUser;
	}

	public static benchmark.bean.User toRaw(TarsUser user) {
		benchmark.bean.User rawUser = new benchmark.bean.User();

		rawUser.setId(user.getId());
		rawUser.setName(user.getName());
		rawUser.setSex(user.getSex());
		rawUser.setBirthday(LocalDate.ofEpochDay(user.getBirthday()));
		rawUser.setEmail(user.getEmail());
		rawUser.setMobile(user.getMobile());
		rawUser.setAddress(user.getAddress());
		rawUser.setIcon(user.getIcon());
		rawUser.setPermissions(user.getPermissions());
		rawUser.setStatus(user.getStatus());
		rawUser.setCreateTime(LocalDateTime.ofEpochSecond(user.getCreateTime(), 0, ZoneOffset.UTC));
		rawUser.setUpdateTime(LocalDateTime.ofEpochSecond(user.getUpdateTime(), 0, ZoneOffset.UTC));

		return rawUser;
	}
}
