package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import tg.bot.admin.panel.data.service.ClientService;
import tg.bot.core.domain.Client;

import javax.persistence.EntityNotFoundException;

public class StringToClientConverter implements Converter<String, Client> {

    private final ClientService clientService;

    public StringToClientConverter(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public Result<Client> convertToModel(String s, ValueContext valueContext) {
        return Result.ok(this.clientService.findByName(s)
                .orElseThrow(() -> new EntityNotFoundException("Client not found for name " + s)));
    }

    @Override
    public String convertToPresentation(Client client, ValueContext valueContext) {
        return client.getUsername();
    }
}
