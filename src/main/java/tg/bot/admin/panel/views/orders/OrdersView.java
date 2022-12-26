package tg.bot.admin.panel.views.orders;

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
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import tg.bot.admin.panel.data.service.ClientService;
import tg.bot.admin.panel.data.service.OrderService;
import tg.bot.admin.panel.data.service.PaymentService;
import tg.bot.admin.panel.data.service.SellingItemService;
import tg.bot.admin.panel.views.MainLayout;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.admin.panel.views.a.util.DefaultValueProviders;
import tg.bot.admin.panel.views.a.util.converter.StringToClientConverter;
import tg.bot.admin.panel.views.a.util.converter.StringToOrderStatusConverter;
import tg.bot.admin.panel.views.a.util.converter.StringToPaymentConverter;
import tg.bot.admin.panel.views.a.util.converter.StringToSellingItemConverter;
import tg.bot.core.domain.Order;
import tg.bot.core.domain.base.AbstractAuditableEntity;

import javax.annotation.security.RolesAllowed;
import java.time.Duration;
import java.util.Optional;

@PageTitle("Orders")
@Route(value = "orders/:orderID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class OrdersView extends Div implements BeforeEnterObserver {

    private final String ORDER_ID = "orderID";
    private final String ORDER_EDIT_ROUTE_TEMPLATE = "orders/%s/edit";

    private final Grid<Order> grid = new Grid<>(Order.class, false);
    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<Order> binder;
    private final OrderService orderService;
    private final SellingItemService sellingItemService;
    private final ClientService clientService;
    private final PaymentService paymentService;
    private TextField sellingItem;
    private TextField orderedBy;
    private TextField status;
    private TextField payment;
    private DateTimePicker dateCreated;
    private Order order;

    @Autowired
    public OrdersView(OrderService orderService, SellingItemService sellingItemService, ClientService clientService, PaymentService paymentService) {
        this.orderService = orderService;
        this.sellingItemService = sellingItemService;
        this.clientService = clientService;
        this.paymentService = paymentService;
        addClassNames("orders-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(c -> DefaultValueProviders.generateSellingItemName(c.getSellingItem()))
                .setHeader(ColumnNames.SELLING_ITEM)
                .setAutoWidth(true);
        grid.addColumn(c -> c.getClient().getName())
                .setHeader(ColumnNames.ORDERED_BY)
                .setAutoWidth(true);
        grid.addColumn(c -> c.getStatus().friendlyName())
                .setHeader(ColumnNames.STATUS)
                .setAutoWidth(true);
        grid.addColumn(c -> DefaultValueProviders.generatePaymentAmount(c.getPayment()))
                .setHeader(ColumnNames.PAYMENT)
                .setAutoWidth(true);
        grid.addColumn(AbstractAuditableEntity::getDateCreated)
                .setHeader(ColumnNames.DATE_CREATED)
                .setAutoWidth(true);
        grid.setItems(query -> orderService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(ORDER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(OrdersView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Order.class);

        //    private TextField sellingItem;
        //    private TextField orderedBy;
        //    private TextField status;
        //    private TextField payment;
        //    private DateTimePicker dateCreated;
        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(payment).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("payment");
        binder.forField(sellingItem)
                .withConverter(new StringToSellingItemConverter(this.sellingItemService))
                .bind(Order::getSellingItem, Order::setSellingItem);
        binder.forField(orderedBy)
                .withConverter(new StringToClientConverter(this.clientService))
                .bind(Order::getClient, Order::setClient);
        binder.forField(status)
                .withConverter(new StringToOrderStatusConverter())
                .bind(Order::getStatus, Order::setStatus);
        binder.forField(payment)
                .withConverter(new StringToPaymentConverter(this.paymentService))
                .bind(Order::getPayment, Order::setPayment);
        binder.bind(this.dateCreated, "dateCreated");
//        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.order == null) {
                    this.order = new Order();
                }
                binder.writeBean(this.order);
                orderService.update(this.order);
                clearForm();
                refreshGrid();
                Notification.show("Order details stored.");
                UI.getCurrent().navigate(OrdersView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the order details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> orderId = event.getRouteParameters().get(ORDER_ID).map(Long::parseLong);
        if (orderId.isPresent()) {
            Optional<Order> orderFromBackend = orderService.get(orderId.get());
            if (orderFromBackend.isPresent()) {
                populateForm(orderFromBackend.get());
            } else {
                Notification.show(String.format("The requested order was not found, ID = %s", orderId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(OrdersView.class);
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
        sellingItem = new TextField(ColumnNames.SELLING_ITEM, "Enter selling item id");
        orderedBy = new TextField(ColumnNames.ORDERED_BY, "Enter client's username");
        status = new TextField(ColumnNames.STATUS, "Enter order status");
        payment = new TextField(ColumnNames.PAYMENT, "Enter payment id");
        dateCreated = new DateTimePicker(ColumnNames.DATE_CREATED);
        dateCreated.setStep(Duration.ofSeconds(1));
        formLayout.add(sellingItem, orderedBy, status, payment, dateCreated);

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

    private void populateForm(Order value) {
        this.order = value;
        binder.readBean(this.order);

    }
}
