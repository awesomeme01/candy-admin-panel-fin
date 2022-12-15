package tg.bot.admin.panel.views.messagekeyboardbutton;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.domain.entity.MessageKeyboardButton;
import tg.bot.admin.panel.data.service.MessageKeyboardButtonService;
import tg.bot.admin.panel.views.MainLayout;

@PageTitle("Message Keyboard Button")
@Route(value = "messageKeyboardButton/:messageKeyboardButtonID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class MessageKeyboardButtonView extends Div implements BeforeEnterObserver {

    private final String MESSAGEKEYBOARDBUTTON_ID = "messageKeyboardButtonID";
    private final String MESSAGEKEYBOARDBUTTON_EDIT_ROUTE_TEMPLATE = "messageKeyboardButton/%s/edit";

    private final Grid<MessageKeyboardButton> grid = new Grid<>(MessageKeyboardButton.class, false);

    private TextField keyboardId;
    private TextField label;
    private TextField url;
    private Checkbox isActive;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<MessageKeyboardButton> binder;

    private MessageKeyboardButton messageKeyboardButton;

    private final MessageKeyboardButtonService messageKeyboardButtonService;

    @Autowired
    public MessageKeyboardButtonView(MessageKeyboardButtonService messageKeyboardButtonService) {
        this.messageKeyboardButtonService = messageKeyboardButtonService;
        addClassNames("message-keyboard-button-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(MessageKeyboardButton::getId)
                .setHeader(ColumnNames.ID)
                .setAutoWidth(true);
        grid.addColumn(c -> c.getKeyboard().getId())
                .setHeader("Keyboard Id")
                .setAutoWidth(true);
        grid.addColumn(MessageKeyboardButton::getLabel)
                .setHeader(ColumnNames.LABEL)
                .setAutoWidth(true);
        grid.addColumn(MessageKeyboardButton::getUrl)
                .setHeader(ColumnNames.URL)
                .setAutoWidth(true);
        LitRenderer<MessageKeyboardButton> isActiveRenderer = LitRenderer.<MessageKeyboardButton>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", isActive -> isActive.getIsActive() ? "check" : "minus").withProperty("color",
                        isActive -> isActive.getIsActive()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(isActiveRenderer).setHeader("Is Active").setAutoWidth(true);

        grid.setItems(query -> messageKeyboardButtonService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent()
                        .navigate(String.format(MESSAGEKEYBOARDBUTTON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(MessageKeyboardButtonView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(MessageKeyboardButton.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(keyboardId).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("keyboardId");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.messageKeyboardButton == null) {
                    this.messageKeyboardButton = new MessageKeyboardButton();
                }
                binder.writeBean(this.messageKeyboardButton);
                messageKeyboardButtonService.update(this.messageKeyboardButton);
                clearForm();
                refreshGrid();
                Notification.show("MessageKeyboardButton details stored.");
                UI.getCurrent().navigate(MessageKeyboardButtonView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the messageKeyboardButton details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> messageKeyboardButtonId = event.getRouteParameters().get(MESSAGEKEYBOARDBUTTON_ID)
                .map(Long::parseLong);
        if (messageKeyboardButtonId.isPresent()) {
            Optional<MessageKeyboardButton> messageKeyboardButtonFromBackend = messageKeyboardButtonService
                    .get(messageKeyboardButtonId.get());
            if (messageKeyboardButtonFromBackend.isPresent()) {
                populateForm(messageKeyboardButtonFromBackend.get());
            } else {
                Notification.show(String.format("The requested messageKeyboardButton was not found, ID = %s",
                        messageKeyboardButtonId.get()), 3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(MessageKeyboardButtonView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        keyboardId = new TextField("Keyboard Id");
        label = new TextField("Label");
        url = new TextField("Url");
        isActive = new Checkbox("Is Active");
        formLayout.add(keyboardId, label, url, isActive);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(MessageKeyboardButton value) {
        this.messageKeyboardButton = value;
        binder.readBean(this.messageKeyboardButton);

    }
}
