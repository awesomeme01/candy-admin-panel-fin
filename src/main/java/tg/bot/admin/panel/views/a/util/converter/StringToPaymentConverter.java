package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import tg.bot.admin.panel.data.service.PaymentService;
import tg.bot.core.domain.Payment;

import javax.persistence.EntityNotFoundException;

public class StringToPaymentConverter implements Converter<String, Payment> {

    private final PaymentService paymentService;

    public StringToPaymentConverter(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Result<Payment> convertToModel(String s, ValueContext valueContext) {
        return Result.ok(paymentService.get(Long.parseLong(s))
                .orElseThrow(() -> new EntityNotFoundException("Payment was not found for this id " + s)));
    }

    @Override
    public String convertToPresentation(Payment payment, ValueContext valueContext) {
        if (payment == null) {
            return "";
        }
        return String.format("%s [%s]", payment.getAmount(), payment.getCurrency() != null ? payment.getCurrency().getCode() : "");
    }
}
