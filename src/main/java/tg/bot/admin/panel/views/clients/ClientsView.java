package tg.bot.admin.panel.views.clients;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
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
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.converter.StringToBooleanConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import tg.bot.admin.panel.data.service.ClientService;
import tg.bot.admin.panel.views.MainLayout;
import tg.bot.admin.panel.views.a.util.ButtonUtil;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.core.domain.Client;
import tg.bot.core.domain.base.AbstractAuditableEntity;

@PageTitle("Clients")
@Route(value = "clients/:samplePersonID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class ClientsView extends Div implements BeforeEnterObserver {

    private final String SAMPLEPERSON_ID = "samplePersonID";
    private final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "clients/%s/edit";

    private final Grid<Client> grid = new Grid<>(Client.class, false);

    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField phone;
    private TextField username;
    private DateTimePicker dateCreated;
    private Checkbox isActive;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Client> binder;

    private Client samplePerson;

    private final ClientService clientService;

    @Autowired
    public ClientsView(ClientService clientService) {
        this.clientService = clientService;
        addClassNames("clients-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(AbstractAuditableEntity::getId)
                .setHeader(ColumnNames.ID)
                .setAutoWidth(true);
        grid.addColumn(Client::getName)
                .setHeader(ColumnNames.NAME)
                .setAutoWidth(true);
        grid.addColumn(Client::getSurname)
                .setHeader(ColumnNames.SURNAME)
                .setAutoWidth(true);
        grid.addColumn(Client::getUsername)
                .setHeader(ColumnNames.USERNAME)
                .setAutoWidth(true);
        grid.addColumn(Client::getPhoneNumber)
                .setHeader(ColumnNames.PHONE_NUMBER)
                .setAutoWidth(true);
        grid.addColumn(Client::getDateCreated)
                .setHeader(ColumnNames.DATE_CREATED)
                .setAutoWidth(true);

        LitRenderer<Client> importantRenderer = LitRenderer.<Client>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", important -> important.getIsActive() ? "check" : "minus").withProperty("color",
                        important -> important.getIsActive()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(importantRenderer).setHeader("Important").setAutoWidth(true);
        grid.addComponentColumn(item -> ButtonUtil.defaultDeleteFromGrid(click -> {
            this.clientService.delete(item.getId());
            refreshGrid();
        })).setWidth("140px").setFlexGrow(0).setHeader("Actions");

        grid.setItems(query -> clientService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SAMPLEPERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(ClientsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Client.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bind(this.firstName, "name");
        binder.bind(this.lastName, "surname");
        binder.bind(this.username, "username");
        binder.bind(this.phone, "phoneNumber");
        binder.bind(this.dateCreated, "dateCreated");
        binder.bind(this.isActive, "isActive");

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.samplePerson == null) {
                    this.samplePerson = new Client();
                }
                binder.writeBean(this.samplePerson);
                clientService.update(this.samplePerson);
                clearForm();
                refreshGrid();
                Notification.show("Client details stored.");
                UI.getCurrent().navigate(ClientsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the samplePerson details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> samplePersonId = event.getRouteParameters().get(SAMPLEPERSON_ID).map(Long::parseLong);
        if (samplePersonId.isPresent()) {
            Optional<Client> samplePersonFromBackend = clientService.get(samplePersonId.get());
            if (samplePersonFromBackend.isPresent()) {
                populateForm(samplePersonFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested samplePerson was not found, ID = %s", samplePersonId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(ClientsView.class);
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
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        email = new TextField("Email");
        phone = new TextField("Phone");
        dateCreated = new DateTimePicker("Date Created");
        username = new TextField("Username");
        isActive = new Checkbox("Is Active");
        formLayout.add(firstName, lastName, email, username, phone, dateCreated, isActive);

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

    private void populateForm(Client value) {
        this.samplePerson = value;
        binder.readBean(this.samplePerson);

    }
}
