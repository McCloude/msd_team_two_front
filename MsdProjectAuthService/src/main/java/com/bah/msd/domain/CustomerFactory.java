package com.bah.msd.domain;

import org.json.JSONObject;

public class CustomerFactory {
	
	public static Customer getCustomer(String jsonString){
        JSONObject jsonObject = new org.json.JSONObject(jsonString); 
          
        int id = (int) jsonObject.get("id");
        String name = (String) jsonObject.get("name"); 
        String email = (String) jsonObject.get("email"); 
        String password = (String) jsonObject.get("password"); 
		
		return new Customer(id, name, password, email);
	}
	
	public static String getCustomerAsJSONString(Customer customer) {
        JSONObject jsonObject = new JSONObject(); 
        
        jsonObject.put("name", customer.getName()); 
        jsonObject.put("email", customer.getEmail());
        jsonObject.put("password", customer.getPassword());
        jsonObject.put("id", customer.getId());
        
        return jsonObject.toString();
	}

}
