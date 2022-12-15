package tg.bot.admin.panel.views.booking;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
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
import java.time.Duration;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import tg.bot.admin.panel.data.service.ClientService;
import tg.bot.admin.panel.data.service.SellingItemService;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.admin.panel.views.a.util.DefaultValueProviders;
import tg.bot.admin.panel.views.a.util.converter.StringToClientConverter;
import tg.bot.admin.panel.views.a.util.converter.StringToSellingItemConverter;
import tg.bot.core.domain.Booking;
import tg.bot.admin.panel.data.service.BookingService;
import tg.bot.admin.panel.views.MainLayout;
import tg.bot.core.domain.base.AbstractAuditableEntity;

@PageTitle("Booking")
@Route(value = "booking/:bookingID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class BookingView extends Div implements BeforeEnterObserver {

    private final String BOOKING_ID = "bookingID";
    private final String BOOKING_EDIT_ROUTE_TEMPLATE = "booking/%s/edit";

    private final Grid<Booking> grid = new Grid<>(Booking.class, false);

    private TextField client;
    private TextField sellingItem;
    private DateTimePicker dateCreated;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Booking> binder;

    private Booking booking;

    private final BookingService bookingService;
    private final ClientService clientService;
    private final SellingItemService sellingItemService;

    @Autowired
    public BookingView(BookingService bookingService, ClientService clientService, SellingItemService sellingItemService) {
        this.bookingService = bookingService;
        this.clientService = clientService;
        this.sellingItemService = sellingItemService;
        addClassNames("booking-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(Booking::getId)
                .setAutoWidth(true)
                .setHeader(ColumnNames.ID);
        grid.addColumn(b -> b.getClient().getName())
                .setAutoWidth(true)
                .setHeader(ColumnNames.CLIENT);
        grid.addColumn(b -> DefaultValueProviders.generateSellingItemName(b.getSellingItem()))
                .setAutoWidth(true);
        grid.addColumn(AbstractAuditableEntity::getDateCreated)
                .setAutoWidth(true)
                .setHeader(ColumnNames.DATE_CREATED);
        grid.setItems(query -> bookingService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(BOOKING_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(BookingView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Booking.class);


        // Bind fields. This is where you'd define e.g. validation rules

//        binder.bindInstanceFields(this);
        binder.forField(this.client)
                .withConverter(new StringToClientConverter(this.clientService))
                .bind(Booking::getClient, Booking::setClient);
        binder.forField(this.sellingItem)
                .withConverter(new StringToSellingItemConverter(this.sellingItemService))
                .bind(Booking::getSellingItem, Booking::setSellingItem);


        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.booking == null) {
                    this.booking = new Booking();
                }
                binder.writeBean(this.booking);
                bookingService.update(this.booking);
                clearForm();
                refreshGrid();
                Notification.show("Booking details stored.");
                UI.getCurrent().navigate(BookingView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the booking details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> bookingId = event.getRouteParameters().get(BOOKING_ID).map(Long::parseLong);
        if (bookingId.isPresent()) {
            Optional<Booking> bookingFromBackend = bookingService.get(bookingId.get());
            if (bookingFromBackend.isPresent()) {
                populateForm(bookingFromBackend.get());
            } else {
                Notification.show(String.format("The requested booking was not found, ID = %s", bookingId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(BookingView.class);
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
        client = new TextField(ColumnNames.CLIENT, "Enter client's username");
        sellingItem = new TextField(ColumnNames.SELLING_ITEM, "Enter selling item's id");
        dateCreated = new DateTimePicker(ColumnNames.DATE_CREATED);
        dateCreated.setStep(Duration.ofSeconds(1));
        formLayout.add(client, sellingItem, dateCreated);

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

    private void populateForm(Booking value) {
        this.booking = value;
        binder.readBean(this.booking);

    }
}
