// CustomUserDetailsService.java
package com.movienotebook.api.security;

import com.movienotebook.api.entity.User;
import com.movienotebook.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@NullMarked
public class CustomUserDetailsService implements UserDetailsService {
	
	private final UserRepository userRepository;
	
	@Override
	public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
		
		return new CustomUserDetails(
				user.getId(),
				user.getUsername(),
				user.getPasswordHash(),
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
		);
	}
}