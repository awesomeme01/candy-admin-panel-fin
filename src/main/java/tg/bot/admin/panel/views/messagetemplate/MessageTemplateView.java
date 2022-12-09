package tg.bot.admin.panel.views.messagetemplate;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
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
import tg.bot.domain.entity.MessageResponseTemplate;
import tg.bot.admin.panel.data.service.MessageResponseTemplateService;
import tg.bot.admin.panel.views.MainLayout;

@PageTitle("Message Template")
@Route(value = "messageResponseTemplate/:messageResponseTemplateID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class MessageTemplateView extends Div implements BeforeEnterObserver {

    private final String MESSAGERESPONSETEMPLATE_ID = "messageResponseTemplateID";
    private final String MESSAGERESPONSETEMPLATE_EDIT_ROUTE_TEMPLATE = "messageResponseTemplate/%s/edit";

    private final Grid<MessageResponseTemplate> grid = new Grid<>(MessageResponseTemplate.class, false);

    private TextField message;
    private TextField botRequestUrl;
    private TextField response;
    private TextField lang;
    private TextField pattern;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<MessageResponseTemplate> binder;

    private MessageResponseTemplate messageResponseTemplate;

    private final MessageResponseTemplateService messageResponseTemplateService;

    @Autowired
    public MessageTemplateView(MessageResponseTemplateService messageResponseTemplateService) {
        this.messageResponseTemplateService = messageResponseTemplateService;
        addClassNames("message-template-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(MessageResponseTemplate::getMessage)
                .setHeader(ColumnNames.MESSAGE)
                .setAutoWidth(true);
        grid.addColumn(MessageResponseTemplate::getBotRequestUrl)
                .setHeader(ColumnNames.BOT_REQUEST_URL)
                .setAutoWidth(true);
        grid.addColumn(MessageResponseTemplate::getResponse)
                .setHeader(ColumnNames.RESPONSE)
                .setAutoWidth(true);
        grid.addColumn(MessageResponseTemplate::getLang)
                .setHeader(ColumnNames.LANG)
                .setAutoWidth(true);
        grid.addColumn(MessageResponseTemplate::getPattern)
                .setHeader(ColumnNames.PATERN)
                .setAutoWidth(true);
        grid.setItems(query -> messageResponseTemplateService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent()
                        .navigate(String.format(MESSAGERESPONSETEMPLATE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(MessageTemplateView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(MessageResponseTemplate.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.messageResponseTemplate == null) {
                    this.messageResponseTemplate = new MessageResponseTemplate();
                }
                binder.writeBean(this.messageResponseTemplate);
                messageResponseTemplateService.update(this.messageResponseTemplate);
                clearForm();
                refreshGrid();
                Notification.show("MessageResponseTemplate details stored.");
                UI.getCurrent().navigate(MessageTemplateView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the messageResponseTemplate details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> messageResponseTemplateId = event.getRouteParameters().get(MESSAGERESPONSETEMPLATE_ID)
                .map(Long::parseLong);
        if (messageResponseTemplateId.isPresent()) {
            Optional<MessageResponseTemplate> messageResponseTemplateFromBackend = messageResponseTemplateService
                    .get(messageResponseTemplateId.get());
            if (messageResponseTemplateFromBackend.isPresent()) {
                populateForm(messageResponseTemplateFromBackend.get());
            } else {
                Notification.show(String.format("The requested messageResponseTemplate was not found, ID = %s",
                        messageResponseTemplateId.get()), 3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(MessageTemplateView.class);
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
        message = new TextField("Message");
        botRequestUrl = new TextField("Bot Request Url");
        response = new TextField("Response");
        lang = new TextField("Lang");
        pattern = new TextField("Pattern");
        formLayout.add(message, botRequestUrl, response, lang, pattern);

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

    private void populateForm(MessageResponseTemplate value) {
        this.messageResponseTemplate = value;
        binder.readBean(this.messageResponseTemplate);

    }
}
