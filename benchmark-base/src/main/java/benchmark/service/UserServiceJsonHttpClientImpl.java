package benchmark.service;

import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.rpc.util.HttpClientUtils;
import benchmark.rpc.util.JsonUtils;

/**
 * only for client
 * 
 * @author Hank
 *
 */
public class UserServiceJsonHttpClientImpl implements UserService {

	private static final String URL_EXIST_USER = "http://benchmark-server:8080/user-exist?email=";
	private static final String URL_CREATE_USER = "http://benchmark-server:8080/create-user";
	private static final String URL_GET_USER = "http://benchmark-server:8080/get-user?id=";
	private static final String URL_LIST_USER = "http://benchmark-server:8080/list-user?pageNo=";

	private final CloseableHttpClient client;
	private final ObjectMapper objectMapper = JsonUtils.objectMapper;
	private final JavaType userPageType = objectMapper.getTypeFactory()//
			.constructParametricType(Page.class, User.class);

	public UserServiceJsonHttpClientImpl(int concurrency) {
		client = HttpClientUtils.createHttpClient(concurrency);
	}

	@Override
	public boolean existUser(String email) {
		try {
			String url = URL_EXIST_USER + email;

			HttpGet request = new HttpGet(url);
			CloseableHttpResponse response = client.execute(request);

			String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

			return "true".equals(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean createUser(User user) {
		try {
			byte[] bytes = objectMapper.writeValueAsBytes(user);

			HttpPost request = new HttpPost(URL_CREATE_USER);
			HttpEntity entity = EntityBuilder.create().setBinary(bytes).build();
			request.setEntity(entity);

			CloseableHttpResponse response = client.execute(request);

			String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

			return "true".equals(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public User getUser(long id) {
		try {
			String url = URL_GET_USER + id;

			HttpGet request = new HttpGet(url);
			CloseableHttpResponse response = client.execute(request);

			byte[] bytes = EntityUtils.toByteArray(response.getEntity());

			return objectMapper.readValue(bytes, User.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Page<User> listUser(int pageNo) {
		try {
			String url = URL_LIST_USER + pageNo;

			HttpGet request = new HttpGet(url);
			CloseableHttpResponse response = client.execute(request);

			byte[] bytes = EntityUtils.toByteArray(response.getEntity());

			return objectMapper.readValue(bytes, userPageType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Exception {
		UserService userService = new UserServiceJsonHttpClientImpl(256);

		System.out.println(userService.existUser("1236"));
		System.out.println(userService.getUser(123));
		System.out.println(userService.listUser(123));
	}

}
