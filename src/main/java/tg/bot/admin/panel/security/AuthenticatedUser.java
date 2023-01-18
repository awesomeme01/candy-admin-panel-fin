package tg.bot.admin.panel.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import tg.bot.admin.panel.configuration.SecurityConfiguration;
import tg.bot.admin.panel.data.service.PrincipalService;
import tg.bot.core.domain.Principal;

@Component
public class AuthenticatedUser {

    private final PrincipalService principalService;

    @Autowired
    public AuthenticatedUser(PrincipalService principalService) {
        this.principalService = principalService;
    }

    private Optional<Authentication> getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        return Optional.ofNullable(context.getAuthentication())
                .filter(authentication -> !(authentication instanceof AnonymousAuthenticationToken));
    }

    public Optional<Principal> get() {
        return getAuthentication().flatMap(authentication -> principalService.findByUsername(authentication.getName()));
    }

    public void logout() {
        UI.getCurrent().getPage().setLocation(SecurityConfiguration.LOGOUT_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }

}
