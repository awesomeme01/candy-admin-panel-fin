package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import tg.bot.admin.panel.data.service.SellingItemService;
import tg.bot.core.domain.SellingItem;

import javax.persistence.EntityNotFoundException;

public class StringToSellingItemConverter implements Converter<String, SellingItem> {

    private final SellingItemService sellingItemService;

    public StringToSellingItemConverter(SellingItemService sellingItemService) {
        this.sellingItemService = sellingItemService;
    }

    @Override
    public Result<SellingItem> convertToModel(String s, ValueContext valueContext) {
        return Result.ok(this.sellingItemService.get(Long.parseLong(s)).orElseThrow(() -> new EntityNotFoundException("Selling Item not found for id = " + s)));
    }

    @Override
    public String convertToPresentation(SellingItem sellingItem, ValueContext valueContext) {
        return sellingItem.getId().toString();
    }
}
