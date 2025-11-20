package com.example.subintel.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.subintel.dto.SubscriptionDTO;
import com.example.subintel.model.Frequency;
import com.example.subintel.model.SubscriptionModel;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.UserRepository;
import com.example.subintel.service.AppUserDetailsService;
import com.example.subintel.service.JWTService;
import com.example.subintel.service.SubscriptionService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SubscriptionControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SubscriptionService subscriptionService;

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private JWTService jwtService;

	@MockitoBean
	private AppUserDetailsService appUserDetailsService;

	@Test
	void whenGetSubscriptions_thenReturnJsonList() throws Exception {
		UserModel user = new UserModel();
		user.setId(1L);
		user.setEmail("test@example.com");
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(),
				"pass", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));

		SubscriptionModel subModel = new SubscriptionModel();
		subModel.setMerchantName("Netflix");
		subModel.setEstimatedAmount(-15.0);
		subModel.setFrequency(Frequency.MONTHLY);
		SubscriptionDTO subDTO = new SubscriptionDTO(subModel);

		when(subscriptionService.getSubscriptionsForUser(user.getId())).thenReturn(List.of(subDTO));

		mockMvc.perform(get("/api/subscriptions")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].merchantName").value("Netflix"))
				.andExpect(jsonPath("$[0].frequency").value("MONTHLY"));
	}
}
