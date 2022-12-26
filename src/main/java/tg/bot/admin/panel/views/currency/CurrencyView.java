package tg.bot.admin.panel.views.currency;

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
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import tg.bot.admin.panel.data.service.CurrencyService;
import tg.bot.admin.panel.views.MainLayout;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.admin.panel.views.a.util.converter.StringToMoneyTypeConverter;
import tg.bot.core.domain.Currency;
import tg.bot.core.domain.base.AbstractAuditableEntity;

import javax.annotation.security.RolesAllowed;
import java.util.Optional;

@PageTitle("Currency")
@Route(value = "currency/:currencyID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class CurrencyView extends Div implements BeforeEnterObserver {

    private final String CURRENCY_ID = "currencyID";
    private final String CURRENCY_EDIT_ROUTE_TEMPLATE = "currency/%s/edit";

    private final Grid<Currency> grid = new Grid<>(Currency.class, false);
    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<Currency> binder;
    private final CurrencyService currencyService;
    private TextField name;
    private TextField code;
    private TextField type;
    private TextField amountInUsd;
    private Currency currency;

    @Autowired
    public CurrencyView(CurrencyService currencyService) {
        this.currencyService = currencyService;
        addClassNames("currency-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(AbstractAuditableEntity::getId)
                .setHeader(ColumnNames.ID)
                .setAutoWidth(true);
        grid.addColumn(Currency::getName)
                .setHeader(ColumnNames.NAME)
                .setAutoWidth(true);
        grid.addColumn(Currency::getCode)
                .setHeader(ColumnNames.CODE)
                .setAutoWidth(true);
        grid.addColumn(Currency::getType)
                .setHeader(ColumnNames.TYPE)
                .setAutoWidth(true);
        grid.addColumn(Currency::getAmountInUsd)
                .setHeader(ColumnNames.AMOUNT_IN_USD)
                .setAutoWidth(true);
        grid.addColumn(AbstractAuditableEntity::getDateCreated).setAutoWidth(true);
        grid.setItems(query -> currencyService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(CURRENCY_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CurrencyView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Currency.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(amountInUsd).withConverter(new StringToDoubleConverter("Only Double numbers are allowed"))
                .bind("amountInUsd");
//        private TextField name;
//        private TextField code;
//        private TextField type;
//        private TextField amountInUsd;
        binder.bind(name, "name");
        binder.bind(code, "code");
        binder.forField(type).withConverter(new StringToMoneyTypeConverter())
                .bind(Currency::getType, Currency::setType);
//        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.currency == null) {
                    this.currency = new Currency();
                }
                binder.writeBean(this.currency);
                currencyService.update(this.currency);
                clearForm();
                refreshGrid();
                Notification.show("Currency details stored.");
                UI.getCurrent().navigate(CurrencyView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the currency details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> currencyId = event.getRouteParameters().get(CURRENCY_ID).map(Long::parseLong);
        if (currencyId.isPresent()) {
            Optional<Currency> currencyFromBackend = currencyService.get(currencyId.get());
            if (currencyFromBackend.isPresent()) {
                populateForm(currencyFromBackend.get());
            } else {
                Notification.show(String.format("The requested currency was not found, ID = %s", currencyId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(CurrencyView.class);
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
        name = new TextField("Name");
        code = new TextField("Code");
        type = new TextField("Type");
        amountInUsd = new TextField("Amount In Usd");
        formLayout.add(name, code, type, amountInUsd);

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

    private void populateForm(Currency value) {
        this.currency = value;
        binder.readBean(this.currency);

    }
}
