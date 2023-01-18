package tg.bot.admin.panel.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tg.bot.admin.panel.data.service.PrincipalService;
import tg.bot.core.domain.Principal;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PrincipalService principalService;

    @Autowired
    public UserDetailsServiceImpl(PrincipalService principalService) {
        this.principalService = principalService;
    }

    private static List<GrantedAuthority> getAuthorities(Principal principal) {
        return principal.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Principal principal = principalService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user present with username: " + username));


        return new org.springframework.security.core.userdetails.User(principal.getUsername(), principal.getPassword(), principal.getIsActive(), true, true, principal.getIsActive(), getAuthorities(principal));
    }

}
