package tg.bot.admin.panel.views.a.util;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import org.apache.commons.lang3.ObjectUtils;
import tg.bot.core.domain.Order;
import tg.bot.core.domain.Payment;
import tg.bot.core.domain.SellingItem;
import tg.bot.core.domain.enums.OrderStatus;
import tg.bot.domain.entity.MessageKeyboardButton;

import java.util.List;

public class DefaultValueProviders {
    public static String generatePaymentAmount(Payment payment) {
        return String.format("%s%s", payment.getAmount(), payment.getCurrency().getCode());
    }

    public static String generateSellingItemName(SellingItem sellingItem) {
        if (ObjectUtils.isEmpty(sellingItem) || ObjectUtils.isEmpty(sellingItem.getProduct())) {
            return "N/A";
        }
        return String.format("%s - %sg.", sellingItem.getProduct().getName(), sellingItem.getWeight());
    }

    public static String buttonNamesCombine(List<MessageKeyboardButton> messageKeyboardButtons) {
        return messageKeyboardButtons.stream().map(MessageKeyboardButton::getLabel)
                .reduce((a, b) -> "" + a + "," + b)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't parse MessageKeyboardButtons.labels"));
    }

    public static ComponentRenderer<Span, Order> createStatusComponentRenderer() {
        return new ComponentRenderer<>(Span::new, statusComponentUpdater);
    }

    public static final SerializableBiConsumer<Span, Order> statusComponentUpdater = (
            span, order) -> {
        OrderStatus status = order.getStatus();
        span.getElement().setAttribute("theme", status.vaadinTheme());
        span.setText(status.friendlyName());
    };


}
