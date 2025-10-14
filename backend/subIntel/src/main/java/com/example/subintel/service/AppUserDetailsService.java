package com.example.subintel.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//import java.util.ArrayList;

import org.springframework.security.core.userdetails.User;

import com.example.subintel.model.UserModel;
import com.example.subintel.repository.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

@Service
public class AppUserDetailsService implements UserDetailsService {
	private UserRepository userRepository;

	public AppUserDetailsService(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		UserModel appUser = userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()));		
		return new User(appUser.getEmail(), appUser.getPassword(), authorities);//new ArrayList<>());
	}

}
