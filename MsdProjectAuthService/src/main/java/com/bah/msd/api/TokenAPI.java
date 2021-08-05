package com.bah.msd.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bah.msd.domain.Customer;
import com.bah.msd.domain.Token;
import com.bah.msd.util.CustomerFactory;
import com.bah.msd.util.JWTHelper;

@RestController
@RequestMapping("/token")
public class TokenAPI {

	private static final String DATA_API_HOST = "localhost:8080";

	// private static Key key = AuthFilter.key;
	public static Token appUserToken;

	@GetMapping
	public String getAll() {
		return "jwt-fake-token-asdfasdfasfa".toString();
	}

	@PostMapping
	// public ResponseEntity<?> createTokenForCustomer(@RequestBody Customer
	// customer, HttpRequest request, UriComponentsBuilder uri) {
	public ResponseEntity<?> createTokenForCustomer(@RequestBody Customer customer) {

		String username = customer.getName();
		String password = customer.getPassword();

		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) && checkPassword(username, password)) {
			Token token = createToken(username);
			return ResponseEntity.ok(token);
		}
		// bad request
		return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

	}

	private boolean checkPassword(String username, String password) {
		// special case for application user
		if (username.equals("ApiClientApp") && password.equals("secret")) {
			return true;
		}
		// make call to customer service
		Customer cust = getCustomerByNameFromCustomerAPI(username);

		// compare name and password
		if (cust != null && cust.getName().equals(username) && cust.getPassword().equals(password)) {
			return true;
		}
		return false;
	}

	public static Token getAppUserToken() {
		if (appUserToken == null || StringUtils.isBlank(appUserToken.getToken())) {
			appUserToken = createToken("ApiClientApp");
		}
		return appUserToken;
	}

	private static Token createToken(String username) {
		String scopes = "com.bah.msd.data.apis";
		// special case for application user
		if (username.equalsIgnoreCase("ApiClientApp")) {
			scopes = "com.bah.msd.auth.apis";
		}
		String tokenString = JWTHelper.createToken(scopes);
		return new Token(tokenString);
	}

	private Customer getCustomerByNameFromCustomerAPI(String username) {
		try {

			String apiHost = System.getenv("API_HOST");
			if (apiHost == null) {
				apiHost = DATA_API_HOST;
			}
			URL url = new URL("http://" + apiHost + "/api/customers/byname/" + username);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(RequestMethod.GET.name());
			conn.setRequestProperty("Accept", "application/json");
			
			Token token = getAppUserToken();
			conn.setRequestProperty("authorization", "Bearer " + token.getToken());

			if (conn.getResponseCode() != 200) {
				return null;
			} else {
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				String output = StringUtils.EMPTY;
				String out = StringUtils.EMPTY;
				while ((out = br.readLine()) != null) {
					output += out;
				}
				conn.disconnect();
				return CustomerFactory.getCustomer(output);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;

		} catch (java.io.IOException e) {
			e.printStackTrace();
			return null;
		}

	}

}
