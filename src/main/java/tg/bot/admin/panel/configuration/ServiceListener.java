package tg.bot.admin.panel.configuration;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceListener implements VaadinServiceInitListener {

    private final GlobalExceptionHandler globalExceptionHandler;

    @Override
    public void serviceInit(ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.getSource().addSessionInitListener(
                initEvent -> {
                    VaadinSession.getCurrent().setErrorHandler(globalExceptionHandler);
                });
    }
}
