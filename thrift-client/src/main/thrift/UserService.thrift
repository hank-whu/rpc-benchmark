namespace java benchmark.rpc.thrift
 
struct User {
	1: i64 id,
	2: string name,
	3: i32 sex,
	4: i32 birthday,
	5: string email,
	6: string mobile,
	7: string address,
	8: string icon,
	9: list<i32> permissions,
	10: i32 status,
	11: i64 createTime,
	12: i64 updateTime,
}

struct UserPage {
  	1: i32 pageNo,
	2: i32 total,
	3: list<User> result,
}
 
service UserService {
    
	bool userExist(1: string email),
  
	bool createUser(1: User user),
  
	User getUser(1: i64 id),
  
	UserPage listUser(1: i32 pageNo)
}