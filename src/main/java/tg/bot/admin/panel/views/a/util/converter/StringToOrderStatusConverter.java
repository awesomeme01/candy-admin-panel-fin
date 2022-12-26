package tg.bot.admin.panel.views.a.util.converter;


import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import tg.bot.core.domain.enums.OrderStatus;

public class StringToOrderStatusConverter implements Converter<String, OrderStatus> {
    @Override
    public Result<OrderStatus> convertToModel(String s, ValueContext valueContext) {
        return Result.ok(OrderStatus.valueOf(s));
    }

    @Override
    public String convertToPresentation(OrderStatus orderStatus, ValueContext valueContext) {
        return orderStatus != null ? orderStatus.friendlyName() : "";
    }
}
