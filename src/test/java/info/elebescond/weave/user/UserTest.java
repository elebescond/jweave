package info.elebescond.weave.user;

import info.elebescond.weave.UserWeave;
import info.elebescond.weave.exception.WeaveException;
import info.elebescond.weave.exception.WeaveException.Type;
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
	private static String EMAIL2 = "raoul@yahoo.com";

	private String userId;
	private UserWeave userWeave;

	private final static List<Character> RANDOM_STRING_CHARACTERS;
	static {
		RANDOM_STRING_CHARACTERS = new ArrayList<Character>();
		for (char c = '\u0030'; c <= '\u0039'; c++) {
			RANDOM_STRING_CHARACTERS.add(c);
		}
		for (char c = '\u0061'; c <= '\u007A'; c++) {
			RANDOM_STRING_CHARACTERS.add(c);
		}
	}

	private String generateRandomString(int length) {
		Random r = new Random(System.currentTimeMillis());
		StringBuilder randomString = new StringBuilder();
		for (int i = 0; i < length; i++) {
			randomString.append(new Character(RANDOM_STRING_CHARACTERS.get(r
					.nextInt(RANDOM_STRING_CHARACTERS.size()))));
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

	@Test(dependsOnMethods = { "createUser" })
	public void checkUserIdAvailable2() throws WeaveException {
		boolean result = userWeave.checkUserIdAvailable(userId);
		Assert.assertEquals(result, false);
	}

	@Test(dependsOnMethods = { "deleteUser2" })
	public void checkUserIdAvailable3() throws WeaveException {
		boolean result = userWeave.checkUserIdAvailable(userId);
		Assert.assertEquals(result, true);
	}

	@Test(dependsOnMethods = { "checkUserIdAvailable1" })
	public void createUserBadPasswordStrength() {
		try {
			userWeave.createUser(userId, userId, EMAIL1);
		} catch (WeaveException e) {
			Assert.assertEquals(e.getType(),
					Type.WEAVE_ERROR_BAD_PASSWORD_STRENGTH);
		}
	}

	@Test(dependsOnMethods = { "createUserBadPasswordStrength" })
	public void createUserBadUserName() {
		try {
			userWeave.createUser("'" + userId, userId, EMAIL1);
		} catch (WeaveException e) {
			Assert.assertEquals(e.getType(), Type.WEAVE_ERROR_INVALID_USERNAME);
		}
	}

	@Test(dependsOnMethods = { "createUserBadUserName" })
	public void createUser() throws WeaveException {
		boolean result = userWeave.createUser(userId, PASSWORD1, EMAIL1);
		Assert.assertEquals(result, true);
	}

	@Test(dependsOnMethods = { "checkUserIdAvailable2" })
	public void createUser2() {
		try {
			userWeave.createUser(userId, PASSWORD1, EMAIL1);
		} catch (WeaveException e) {
			Assert.assertEquals(e.getType(), Type.WEAVE_ERROR_NO_OVERWRITE);
		}
	}

	@Test(dependsOnMethods = { "createUser2" })
	public void changePassword1() {
		try {
			userWeave.changePassword(userId, PASSWORD1, userId);
		} catch (WeaveException e) {
			Assert.assertEquals(e.getType(),
					Type.WEAVE_ERROR_BAD_PASSWORD_STRENGTH);
		}
	}

	@Test(dependsOnMethods = { "changePassword1" }, expectedExceptions = { WeaveException.class })
	public void changePassword2() throws WeaveException {
		userWeave.changePassword(userId, PASSWORD2, PASSWORD2);
	}

	@Test(dependsOnMethods = { "changePassword2" })
	public void changePassword3() throws WeaveException {
		boolean result = userWeave.changePassword(userId, PASSWORD1, PASSWORD2);
		Assert.assertEquals(result, true);
	}

	@Test(dependsOnMethods = { "changePassword3" }, expectedExceptions = { WeaveException.class })
	public void changeEmail1() throws WeaveException {
		userWeave.changeEmail(userId, PASSWORD1, EMAIL2);
	}

	@Test(dependsOnMethods = { "changeEmail1" })
	public void changeEmail2() throws WeaveException {
		boolean result = userWeave.changeEmail(userId, PASSWORD2, EMAIL2);
		Assert.assertEquals(result, true);
	}

	@Test(dependsOnMethods = { "changeEmail2" })
	public void getUserStorageNode() throws WeaveException {
		UserWeave userWeave = new UserWeaveImpl(SERVER);
		String result = userWeave.getUserStorageNode(userId, PASSWORD1);
		Assert.assertNotNull(result);
	}

	@Test(dependsOnMethods = { "getUserStorageNode" }, expectedExceptions = { WeaveException.class })
	public void deleteUser1() throws WeaveException {
		UserWeave userWeave = new UserWeaveImpl(SERVER);
		userWeave.deleteUser(userId, PASSWORD1);
	}

	@Test(dependsOnMethods = { "deleteUser1" })
	public void deleteUser2() throws WeaveException {
		UserWeave userWeave = new UserWeaveImpl(SERVER);
		boolean result = userWeave.deleteUser(userId, PASSWORD2);
		Assert.assertEquals(result, true);
	}

}