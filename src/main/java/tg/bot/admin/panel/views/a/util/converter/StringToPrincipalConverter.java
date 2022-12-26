package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import tg.bot.admin.panel.data.service.PrincipalService;
import tg.bot.core.domain.Principal;

import javax.persistence.EntityNotFoundException;


public class StringToPrincipalConverter implements Converter<String, Principal> {

    private final PrincipalService principalService;

    public StringToPrincipalConverter(PrincipalService principalService) {
        this.principalService = principalService;
    }

    @Override
    public Result<Principal> convertToModel(String s, ValueContext valueContext) {
        return Result.ok(principalService.findByUsername(s)
                .orElseThrow(() -> new EntityNotFoundException("Principal was not found by username = " + s)));
    }

    @Override
    public String convertToPresentation(Principal principal, ValueContext valueContext) {
        return principal != null ? principal.getUsername() : "";
    }
}
