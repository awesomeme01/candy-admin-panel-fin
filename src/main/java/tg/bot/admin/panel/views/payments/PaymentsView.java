package tg.bot.admin.panel.views.payments;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tg.bot.admin.panel.data.service.ClientService;
import tg.bot.admin.panel.data.service.CurrencyService;
import tg.bot.admin.panel.views.a.util.ButtonUtil;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.admin.panel.views.a.util.converter.StringToClientConverter;
import tg.bot.admin.panel.views.a.util.converter.StringToCurrencyConverter;
import tg.bot.core.domain.Currency;
import tg.bot.core.domain.Payment;
import tg.bot.admin.panel.data.service.PaymentService;
import tg.bot.admin.panel.views.MainLayout;

@PageTitle("Payments")
@Route(value = "payments/:paymentID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class PaymentsView extends Div implements BeforeEnterObserver {

    private final String PAYMENT_ID = "paymentID";
    private final String PAYMENT_EDIT_ROUTE_TEMPLATE = "payments/%s/edit";

    private final Grid<Payment> grid = new Grid<>(Payment.class, false);

    private TextField user;
    private TextField orderId;
    private TextField amount;
    private ComboBox<Currency> currency;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Payment> binder;

    private Payment payment;

    private final PaymentService paymentService;
    private final ClientService clientService;
    private final CurrencyService currencyService;

    @Autowired
    public PaymentsView(PaymentService paymentService, ClientService clientService, CurrencyService currencyService) {
        this.paymentService = paymentService;
        this.clientService = clientService;
        this.currencyService = currencyService;
        addClassNames("payments-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(c -> c.getClient().getName())
                .setHeader(ColumnNames.CLIENT)
                .setAutoWidth(true);
        grid.addColumn(c -> c.getOrder().getId())
                .setHeader(ColumnNames.ORDER_ID)
                .setAutoWidth(true);
        grid.addColumn(Payment::getAmount)
                .setHeader(ColumnNames.AMOUNT)
                .setAutoWidth(true);
        grid.addColumn(c -> c.getCurrency().getCode())
                .setHeader(ColumnNames.CURRENCY)
                .setAutoWidth(true);
        grid.addComponentColumn(item -> ButtonUtil.defaultDeleteFromGrid(click -> {
            this.paymentService.delete(item.getId());
            refreshGrid();
        })).setWidth("140px").setFlexGrow(0).setHeader("Actions");
        grid.setItems(query -> paymentService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PAYMENT_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(PaymentsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Payment.class);
        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(orderId)
                .withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("order.id");
        binder.forField(amount)
                .withConverter(new StringToDoubleConverter("Only numbers are allowed")).bind("amount");
        binder.forField(user)
                .withConverter(new StringToClientConverter(this.clientService))
                .bind(Payment::getClient, Payment::setClient);
        binder.bind(this.currency, "currency");

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.payment == null) {
                    this.payment = new Payment();
                }
                binder.writeBean(this.payment);
                paymentService.update(this.payment);
                clearForm();
                refreshGrid();
                Notification.show("Payment details stored.");
                UI.getCurrent().navigate(PaymentsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the payment details.");
            }
        });

    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> paymentId = event.getRouteParameters().get(PAYMENT_ID).map(Long::parseLong);
        if (paymentId.isPresent()) {
            Optional<Payment> paymentFromBackend = paymentService.get(paymentId.get());
            if (paymentFromBackend.isPresent()) {
                populateForm(paymentFromBackend.get());
            } else {
                Notification.show(String.format("The requested payment was not found, ID = %s", paymentId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(PaymentsView.class);
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
        user = new TextField(ColumnNames.CLIENT, "Enter username of client");
        orderId = new TextField(ColumnNames.ORDER_ID, "Enter order id");
        amount = new TextField(ColumnNames.AMOUNT, "Enter amount");
        currency = new ComboBox<>(ColumnNames.CURRENCY);
        currency.setItems(this.currencyService.list(Pageable.unpaged()).toList());
        currency.setItemLabelGenerator(Currency::getCode);
        formLayout.add(user, orderId, amount, currency);

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

    private void populateForm(Payment value) {
        this.payment = value;
        binder.readBean(this.payment);

    }
}
