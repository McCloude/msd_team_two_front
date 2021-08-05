package com.bah.msd.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.bah.msd.domain.Customer;
import com.bah.msd.domain.Token;
import com.bah.msd.util.CustomerFactory;

@RestController
@RequestMapping("/register")
public class RegisterAPI {

	String dataApiHost = "localhost:8080";

	@PostMapping
	public ResponseEntity<?> registerCustomer(@RequestBody Customer newCustomer, UriComponentsBuilder uri) {
		if (newCustomer.getId() != 0 || newCustomer.getName() == null || newCustomer.getEmail() == null) {
			return ResponseEntity.badRequest().build();
		}

		String json_string = CustomerFactory.getCustomerAsJSONString(newCustomer);
		postNewCustomerToCustomerAPI(json_string);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newCustomer.getId()).toUri();
		return ResponseEntity.created(location).build();
	}

	private void postNewCustomerToCustomerAPI(String jsonString) {
		try {

			String apiHost = System.getenv("API_HOST");
			if (apiHost == null) {
				apiHost = this.dataApiHost;
			}
			URL url = new URL("http://" + apiHost + "/api/customers");

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod(RequestMethod.POST.name());
			conn.setRequestProperty("Content-Type", "application/json");

			Token token = TokenAPI.getAppUserToken();
			conn.setRequestProperty("authorization", "Bearer " + token.getToken());

			OutputStream os = conn.getOutputStream();
			os.write(jsonString.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
