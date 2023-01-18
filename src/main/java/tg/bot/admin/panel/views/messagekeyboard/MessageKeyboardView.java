package tg.bot.admin.panel.views.messagekeyboard;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tg.bot.admin.panel.data.service.MessageKeyboardButtonService;
import tg.bot.admin.panel.data.service.MessageResponseTemplateService;
import tg.bot.admin.panel.views.a.util.ButtonUtil;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.admin.panel.views.a.util.DefaultValueProviders;
import tg.bot.admin.panel.views.a.util.converter.StringToButtonsConverter;
import tg.bot.admin.panel.views.a.util.converter.StringToMessageTemplateConverter;
import tg.bot.domain.entity.MessageKeyboard;
import tg.bot.admin.panel.data.service.MessageKeyboardService;
import tg.bot.admin.panel.views.MainLayout;
import tg.bot.domain.entity.MessageResponseTemplate;

@PageTitle("Message Keyboard")
@Route(value = "messageKeyboard/:messageKeyboardID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class MessageKeyboardView extends Div implements BeforeEnterObserver {

    private final String MESSAGEKEYBOARD_ID = "messageKeyboardID";
    private final String MESSAGEKEYBOARD_EDIT_ROUTE_TEMPLATE = "messageKeyboard/%s/edit";

    private final Grid<MessageKeyboard> grid = new Grid<>(MessageKeyboard.class, false);
    private final MessageResponseTemplateService messageTemplateService;
    private final MessageKeyboardButtonService messageKeyBoardButtonService;

    private ComboBox<MessageResponseTemplate> template;
    private TextField version;
    private TextField buttons;
    private Checkbox isActive;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<MessageKeyboard> binder;

    private MessageKeyboard messageKeyboard;

    private final MessageKeyboardService messageKeyboardService;

    public MessageKeyboardView(MessageResponseTemplateService messageTemplateService, MessageKeyboardButtonService messageKeyBoardButtonService, MessageKeyboardService messageKeyboardService) {
        this.messageTemplateService = messageTemplateService;
        this.messageKeyBoardButtonService = messageKeyBoardButtonService;
        this.messageKeyboardService = messageKeyboardService;
        addClassNames("message-keyboard-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(m -> m.getMessageResponseTemplate().getMessage())
                .setHeader("Template Name")
                .setAutoWidth(true);
        grid.addColumn(MessageKeyboard::getVersion)
                .setHeader(ColumnNames.VERSION)
                .setAutoWidth(true);
        grid.addColumn(keyboard -> DefaultValueProviders.buttonNamesCombine(keyboard.getButtons()))
                .setHeader(ColumnNames.BUTTONS)
                .setAutoWidth(true);

        LitRenderer<MessageKeyboard> isActiveRenderer = LitRenderer.<MessageKeyboard>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", isActive -> isActive.getIsActive() ? "check" : "minus").withProperty("color",
                        isActive -> isActive.getIsActive()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(isActiveRenderer).setHeader("Is Active").setAutoWidth(true);
        grid.addComponentColumn(item -> ButtonUtil.defaultDeleteFromGrid(click -> {
            this.messageKeyboardService.delete(item.getId());
            refreshGrid();
        })).setWidth("140px").setFlexGrow(0).setHeader("Actions");
        grid.setItems(query -> messageKeyboardService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(MESSAGEKEYBOARD_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(MessageKeyboardView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(MessageKeyboard.class);

        // Bind fields. This is where you'd define e.g. validation rules
        //    private TextField template;
        //    private TextField version;
        //    private TextField buttons;
        //    private Checkbox isActive;
        binder.bind(this.template, "messageResponseTemplate");
        binder.bind(version,"version");
        binder.forField(buttons).withConverter(new StringToButtonsConverter(this.messageKeyBoardButtonService))
                .bind(MessageKeyboard::getButtons, MessageKeyboard::setButtons);
        binder.bind(isActive, "isActive");

//        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.messageKeyboard == null) {
                    this.messageKeyboard = new MessageKeyboard();
                }
                binder.writeBean(this.messageKeyboard);
                messageKeyboardService.update(this.messageKeyboard);
                clearForm();
                refreshGrid();
                Notification.show("MessageKeyboard details stored.");
                UI.getCurrent().navigate(MessageKeyboardView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the messageKeyboard details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> messageKeyboardId = event.getRouteParameters().get(MESSAGEKEYBOARD_ID).map(Long::parseLong);
        if (messageKeyboardId.isPresent()) {
            Optional<MessageKeyboard> messageKeyboardFromBackend = messageKeyboardService.get(messageKeyboardId.get());
            if (messageKeyboardFromBackend.isPresent()) {
                populateForm(messageKeyboardFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested messageKeyboard was not found, ID = %s", messageKeyboardId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(MessageKeyboardView.class);
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
        template = new ComboBox<>("Template Name");
        template.setItems(this.messageTemplateService.list(Pageable.unpaged()).toList());
        template.setItemLabelGenerator(MessageResponseTemplate::getMessage);
        version = new TextField("Version");
        buttons = new TextField("Buttons");
        isActive = new Checkbox("Is Active");
        formLayout.add(template, version, buttons, isActive);

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

    private void populateForm(MessageKeyboard value) {
        this.messageKeyboard = value;
        binder.readBean(this.messageKeyboard);

    }
}
