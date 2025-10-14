package com.example.subintel.config;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;

@Configuration
public class PlaidConfig {
	@Value("${plaid.client.id}")
	private String clientId;
	@Value("${plaid.secret.sandbox}")
	private String sandboxSecret;
	
	@Bean
	public PlaidApi plaidApi() {
		HashMap<String, String> apiKeys=new HashMap<String, String>();
		apiKeys.put("clientId", clientId);
		apiKeys.put("secret", sandboxSecret);
		
		ApiClient apiClient=new ApiClient(apiKeys);
		apiClient.setPlaidAdapter(ApiClient.Sandbox);
		return apiClient.createService(PlaidApi.class);
	}
}
