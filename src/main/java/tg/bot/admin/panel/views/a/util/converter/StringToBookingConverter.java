package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import org.springframework.util.ObjectUtils;
import tg.bot.admin.panel.data.service.BookingService;
import tg.bot.core.domain.Booking;

import javax.persistence.EntityNotFoundException;

public class StringToBookingConverter implements Converter<String, Booking> {

    private final BookingService bookingService;

    public StringToBookingConverter(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public Result<Booking> convertToModel(String s, ValueContext valueContext) {
        if (ObjectUtils.isEmpty(s)) {
            return Result.ok(null);
        }
        return Result.ok(bookingService.get(Long.parseLong(s))
                .orElseThrow(() -> new EntityNotFoundException("Entity Not found for id = " + s)));
    }

    @Override
    public String convertToPresentation(Booking booking, ValueContext valueContext) {
        return booking.getId().toString();
    }
}
