package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import tg.bot.admin.panel.data.service.MessageResponseTemplateService;
import tg.bot.domain.entity.MessageResponseTemplate;

import javax.persistence.EntityNotFoundException;

public class StringToMessageTemplateConverter implements Converter<String, MessageResponseTemplate> {

    private final MessageResponseTemplateService messageResponseTemplateService;

    public StringToMessageTemplateConverter(MessageResponseTemplateService messageResponseTemplateService) {
        this.messageResponseTemplateService = messageResponseTemplateService;
    }

    @Override
    public Result<MessageResponseTemplate> convertToModel(String s, ValueContext valueContext) {
        return Result.ok(messageResponseTemplateService.get(Long.parseLong(s))
                .orElseThrow(() -> new EntityNotFoundException("No Message template found for the given id " + s)));
    }

    @Override
    public String convertToPresentation(MessageResponseTemplate messageResponseTemplate, ValueContext valueContext) {
        return messageResponseTemplate != null ? messageResponseTemplate.getId().toString() : "";
    }
}
