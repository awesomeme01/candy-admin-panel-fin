package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import tg.bot.core.domain.enums.MoneyType;

public class StringToMoneyTypeConverter implements Converter<String, MoneyType> {
    @Override
    public Result<MoneyType> convertToModel(String s, ValueContext valueContext) {
        return Result.ok(MoneyType.valueOf(s));
    }

    @Override
    public String convertToPresentation(MoneyType moneyType, ValueContext valueContext) {
        return moneyType.name();
    }
}
