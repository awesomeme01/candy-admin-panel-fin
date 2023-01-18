package tg.bot.admin.panel.views.a.util;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ButtonUtil {

    public static Button defaultDeleteFromGrid(ComponentEventListener<ClickEvent<Button>> eventListener) {
        Button button = new Button("Delete", eventListener);
        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        button.getStyle().set("margin-inline-end", "auto");
        button.setIcon(VaadinIcon.TRASH.create());
        return button;
    }

}
