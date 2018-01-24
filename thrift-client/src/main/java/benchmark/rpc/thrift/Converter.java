package benchmark.rpc.thrift;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Converter {

	public static benchmark.rpc.thrift.User toThrift(benchmark.bean.User user) {
		benchmark.rpc.thrift.User thriftUser = new benchmark.rpc.thrift.User();

		thriftUser.setId(user.getId());
		thriftUser.setName(user.getName());
		thriftUser.setSex(user.getSex());
		thriftUser.setBirthday((int) (user.getBirthday().toEpochDay()));
		thriftUser.setEmail(user.getEmail());
		thriftUser.setMobile(user.getMobile());
		thriftUser.setAddress(user.getAddress());
		thriftUser.setIcon(user.getIcon());
		thriftUser.setPermissions(user.getPermissions());
		thriftUser.setStatus(user.getStatus());
		thriftUser.setCreateTime(user.getCreateTime().toEpochSecond(ZoneOffset.UTC));
		thriftUser.setUpdateTime(user.getUpdateTime().toEpochSecond(ZoneOffset.UTC));

		return thriftUser;
	}

	public static benchmark.bean.User toRaw(benchmark.rpc.thrift.User user) {
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
