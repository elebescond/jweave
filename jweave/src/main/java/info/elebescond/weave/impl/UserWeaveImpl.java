package info.elebescond.weave.impl;

import info.elebescond.weave.UserWeave;
import info.elebescond.weave.exception.WeaveException;
import info.elebescond.weave.exception.WeaveException.Type;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class UserWeaveImpl implements UserWeave {

	private String secret;
	private String serverUrl;

	public UserWeaveImpl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public UserWeaveImpl(String serverUrl, String secret) {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime",
				"true");
		System.setProperty(
				"org.apache.commons.logging.simplelog.log.httpclient.wire",
				"debug");
		System
				.setProperty(
						"org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient",
						"debug");
		this.serverUrl = serverUrl;
		// if (secret.contains("\""))
		// throw new WeaveException(
		// "Weave secret may not contain the quote character");
		this.secret = secret;
	}

	@Override
	public void changeEmail(String userId, String password, String newEmail)
			throws WeaveException {
		// if (userId.contains("\""))
		// throw new WeaveException(
		// "Weave userIds may not contain the quote character");
		// if (newEmail.contains("\""))
		// throw new WeaveException(
		// "Weave userIds may not contain the quote character");
	}

	@Override
	public void changePassword(String userId, String password,
			String newPassword) throws WeaveException {
		// if (userId.contains("\""))
		// throw new WeaveException(
		// "Weave userIds may not contain the quote character");
	}

	@Override
	public boolean checkUserIdAvailable(String userId) throws WeaveException {
		try {
			HttpClient client = new HttpClient();
			String url = serverUrl + USER_API_URL + "/" + userId;
			HttpMethod method = new GetMethod(url);
			int statusCode = client.executeMethod(method);
			byte[] responseBody = method.getResponseBody();
			method.releaseConnection();
			String result = new String(responseBody);
			if (statusCode != HttpStatus.SC_OK)
				throw new WeaveException(
						String
								.format(
										"Unable to communicate with Weave server. StatusCode = %s",
										statusCode), getType(result));

			if (result.compareTo("0") == 0)
				return true;
			else if (result.compareTo("1") == 0)
				return false;
			else
				throw new WeaveException(String.format(
						"Unable to communicate with Weave server: %s", result),
						getType(result));
		} catch (HttpException e) {
			throw new WeaveException(
					"Unable to communicate with Weave server.", e);
		} catch (IOException e) {
			throw new WeaveException(
					"Unable to communicate with Weave server.", e);
		}
	}

	@Override
	public void createUser(String userId, String password, String email)
			throws WeaveException {
		createUser(userId, password, email, null, null);
	}

	@Override
	public void createUser(String userId, String password, String email,
			String captchaChallenge, String captchaResponse)
			throws WeaveException {
		// if (userId.contains("\""))
		// throw new WeaveException(
		// "Weave userIds may not contain the quote character");
		// if (email.contains("\""))
		// throw new WeaveException(
		// "Weave userIds may not contain the quote character");
		try {
			HttpClient client = new HttpClient();
			String url = serverUrl + USER_API_URL + "/" + userId + "/";
			PutMethod method = new PutMethod(url);
			JSONObject body = new JSONObject();
			body.put("password", password);
			body.put("email", email);
			RequestEntity requestEntity = new StringRequestEntity(body
					.toString(), "application/json", "UTF-8");
			method.setRequestEntity(requestEntity);
			if (secret != null)
				method.addRequestHeader("X-Weave-Secret", secret);
			if (captchaChallenge != null && captchaResponse != null) {
				if (secret != null)
					throw new WeaveException(
							"Cannot provide both a secret and a captchaResponse to createUser");
				method.addRequestHeader("captcha-challenge", captchaChallenge);
				method.addRequestHeader("captcha-response", captchaResponse);
			}

			int statusCode = client.executeMethod(method);
			byte[] responseBody = method.getResponseBody();
			method.releaseConnection();
			String result = new String(responseBody);
			if (statusCode != HttpStatus.SC_OK)
				throw new WeaveException(
						String
								.format(
										"Unable to communicate with Weave server. StatusCode = %s",
										statusCode), getType(result));
			if (result.compareTo(userId.toLowerCase()) != 0)
				throw new WeaveException(
						String
								.format(
										"Unable to create new user: got return value '%s' from server",
										result));
		} catch (UnsupportedEncodingException e) {
			throw new WeaveException(
					"Unable to communicate with Weave server.", e);
		} catch (HttpException e) {
			throw new WeaveException(
					"Unable to communicate with Weave server.", e);
		} catch (IOException e) {
			throw new WeaveException(
					"Unable to communicate with Weave server.", e);
		}

	}

	@Override
	public void deleteUser(String userId, String password)
			throws WeaveException {
		// if (userId.contains("\""))
		// throw new WeaveException(
		// "Weave userIds may not contain the quote character");
		try {
			HttpClient client = new HttpClient();
			client.getState().setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(userId, password));
			String url = serverUrl + USER_API_URL + "/" + userId + "/";
			client.getParams().setAuthenticationPreemptive(true);
			DeleteMethod method = new DeleteMethod(url);
			method.setDoAuthentication(true);
			if (secret != null)
				method.addRequestHeader("X-Weave-Secret", secret);
			int statusCode = client.executeMethod(method);
			byte[] responseBody = method.getResponseBody();
			method.releaseConnection();
			String result = new String(responseBody);
			if (statusCode != HttpStatus.SC_OK)
				throw new WeaveException(
						String
								.format(
										"Unable to communicate with Weave server. StatusCode = %s (%s)",
										statusCode, result), getType(result));
		} catch (HttpException e) {
			throw new WeaveException(
					"Unable to communicate with Weave server.", e);
		} catch (IOException e) {
			throw new WeaveException(
					"Unable to communicate with Weave server.", e);
		}
	}

	@Override
	public String getSecret() {
		return this.secret;
	}

	@Override
	public String getServerUrl() {
		return this.serverUrl;
	}

	@Override
	public void setSecret(String secret) {
		this.secret = secret;
	}

	@Override
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@Override
	public String getUserStorageNode(String userId, String password)
			throws WeaveException {
		try {
			HttpClient client = new HttpClient();
			client.getState().setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(userId, password));
			String url = serverUrl + USER_API_URL + "/" + userId
					+ "/node/weave";
			client.getParams().setAuthenticationPreemptive(true);
			HttpMethod method = new GetMethod(url);
			method.setDoAuthentication(true);
			if (secret != null)
				method.addRequestHeader("X-Weave-Secret", secret);
			int statusCode = client.executeMethod(method);
			if (statusCode == HttpStatus.SC_NOT_FOUND)
				return serverUrl;
			byte[] responseBody = method.getResponseBody();
			method.releaseConnection();
			String result = new String(responseBody);
			if (statusCode != HttpStatus.SC_OK)
				throw new WeaveException(
						String
								.format(
										"Unable to communicate with Weave server. StatusCode = %s",
										statusCode), getType(result));
			return result;
		} catch (HttpException e) {
			throw new WeaveException(
					"Unable to communicate with Weave server.", e);
		} catch (IOException e) {
			throw new WeaveException(
					"Unable to communicate with Weave server.", e);
		}
	}

	private Type getType(String result) {
		try {
			return Type.values()[Integer.parseInt(result) - 1];
		} catch (Exception e) {
			return Type.WEAVE_UNKNOWN_ERROR;
		}
	}

}