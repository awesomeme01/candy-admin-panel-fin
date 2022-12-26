package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import tg.bot.admin.panel.data.service.MessageKeyboardService;
import tg.bot.domain.entity.MessageKeyboard;

public class StringToMessageKeyboardConverter implements Converter<String, MessageKeyboard> {

    private final MessageKeyboardService messageKeyboardService;

    public StringToMessageKeyboardConverter(MessageKeyboardService messageKeyboardService) {
        this.messageKeyboardService = messageKeyboardService;
    }

    @Override
    public Result<MessageKeyboard> convertToModel(String s, ValueContext valueContext) {
        return Result.ok(messageKeyboardService.get(Long.parseLong(s))
                .orElse(null));
    }

    @Override
    public String convertToPresentation(MessageKeyboard messageKeyboard, ValueContext valueContext) {
        return messageKeyboard != null ? messageKeyboard.getId().toString() : "";
    }
}
