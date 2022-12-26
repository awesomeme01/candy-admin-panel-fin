package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import tg.bot.admin.panel.data.service.CurrencyService;
import tg.bot.core.domain.Currency;


public class StringToCurrencyConverter implements Converter<String, Currency> {

    private final CurrencyService currencyService;

    public StringToCurrencyConverter(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public Result<Currency> convertToModel(String s, ValueContext valueContext) {
        return Result.ok(currencyService.findByCode(s));
    }

    @Override
    public String convertToPresentation(Currency currency, ValueContext valueContext) {
        return currency != null ? currency.getCode() : "";
    }
}
