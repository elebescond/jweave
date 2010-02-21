package info.elebescond.weave.user;

import info.elebescond.weave.UserWeave;
import info.elebescond.weave.exception.WeaveException;
import info.elebescond.weave.impl.UserWeaveImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class UserTest {

	private static String SERVER = "http://weave.localhost";
	private static String SECRET = "toctoctoc";
	private static String PASSWORD1 = "0987654321";
	private static String PASSWORD2 = "1234567890";
	private static String EMAIL1 = "raoul@gmail.com";
	private static String EMAIL2 = "raoul@gmail";

	private String userId;
	private UserWeave userWeave;
	
	private final static List<Character> RANDOM_STRING_CHARACTERS;
	static {
		RANDOM_STRING_CHARACTERS = new ArrayList<Character>();
		for(char c='\u0030';c<='\u0039';c++) {
			RANDOM_STRING_CHARACTERS.add(c);
		}
		for(char c='\u0061';c<='\u007A';c++) {
			RANDOM_STRING_CHARACTERS.add(c);
		}
	}
	
	private String generateRandomString(int length) {
		Random r = new Random(System.currentTimeMillis());
		StringBuilder randomString = new StringBuilder();
		for(int i=0;i<length;i++) {
			randomString.append(new Character(RANDOM_STRING_CHARACTERS.get(r.nextInt(RANDOM_STRING_CHARACTERS.size()))));
		}
		return randomString.toString();
	}
	
	private String generateUserId() {
		return generateRandomString(8);
	}
	
	@BeforeTest
	private void init() {
		userId = generateUserId();
		userWeave = new UserWeaveImpl(SERVER, SECRET);
	}
	
	@Test
	public void checkUserIdAvailable1() throws WeaveException {
		boolean result = userWeave.checkUserIdAvailable(userId);
		Assert.assertEquals(result, true);
	}
	
	@Test(dependsOnMethods = {"createUser"})
	public void checkUserIdAvailable2() throws WeaveException {
		boolean result = userWeave.checkUserIdAvailable(userId);
		Assert.assertEquals(result, false);
	}
	
	@Test(dependsOnMethods = {"deleteUser2"})
	public void checkUserIdAvailable3() throws WeaveException {
		boolean result = userWeave.checkUserIdAvailable(userId);
		Assert.assertEquals(result, true);
	}

	@Test(dependsOnMethods = {"checkUserIdAvailable1"}, expectedExceptions = {WeaveException.class})
	public void createUserBadPassword() throws WeaveException {
		userWeave.createUser(userId, userId, EMAIL1);		
	}
	
	@Test(dependsOnMethods = {"checkUserIdAvailable1"}, expectedExceptions = {WeaveException.class})
	public void createUserBadEmail() throws WeaveException {
		userWeave.createUser(userId, PASSWORD1, null);		
	}
	
	@Test(dependsOnMethods = {"createUserBadPassword"})
	public void createUser() throws WeaveException {
		userWeave.createUser(userId, PASSWORD1, EMAIL1);		
	}
	
	@Test(dependsOnMethods = {"checkUserIdAvailable2"})
	public void getUserStorageNode() throws WeaveException {
		UserWeave userWeave = new UserWeaveImpl(SERVER);
		String result = userWeave.getUserStorageNode(userId, PASSWORD1);
		Assert.assertNotNull(result);
	}
	
	@Test(dependsOnMethods = {"getUserStorageNode"}, expectedExceptions = {WeaveException.class})
	public void deleteUser1() throws WeaveException {
		UserWeave userWeave = new UserWeaveImpl(SERVER);
		userWeave.deleteUser(userId, PASSWORD2);
	}
	
	@Test(dependsOnMethods = {"deleteUser1"})
	public void deleteUser2() throws WeaveException {
		UserWeave userWeave = new UserWeaveImpl(SERVER);
		userWeave.deleteUser(userId, PASSWORD1);
	}
	
}