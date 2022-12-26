package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import org.apache.commons.lang3.ObjectUtils;
import tg.bot.admin.panel.data.service.MessageKeyboardButtonService;
import tg.bot.domain.entity.MessageKeyboardButton;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringToButtonsConverter implements Converter<String, List<MessageKeyboardButton>> {
    private final MessageKeyboardButtonService messageKeyboardButtonService;

    public StringToButtonsConverter(MessageKeyboardButtonService messageKeyboardButtonService) {
        this.messageKeyboardButtonService = messageKeyboardButtonService;
    }

    @Override
    public Result<List<MessageKeyboardButton>> convertToModel(String s, ValueContext valueContext) {
        return Result.ok(Arrays.stream(s.split(",")).map(messageKeyboardButtonService::findByLabel)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toList()));
    }

    @Override
    public String convertToPresentation(List<MessageKeyboardButton> messageKeyboardButtons, ValueContext valueContext) {
        return messageKeyboardButtons.stream()
                .map(MessageKeyboardButton::getLabel)
                .reduce((a, c) -> a + "," + c)
                .orElse("");
    }
}
