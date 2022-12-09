package tg.bot.admin.panel.views.products;

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
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
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
import tg.bot.admin.panel.data.service.BrandService;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.core.domain.Product;
import tg.bot.admin.panel.data.service.ProductService;
import tg.bot.admin.panel.views.MainLayout;
import tg.bot.core.domain.base.AbstractAuditableEntity;
import tg.bot.admin.panel.views.a.util.converter.StringToBrandConverter;

@PageTitle("Products")
@Route(value = "products/:productID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class ProductsView extends Div implements BeforeEnterObserver {

    private final String PRODUCT_ID = "productID";
    private final String PRODUCT_EDIT_ROUTE_TEMPLATE = "products/%s/edit";

    private final Grid<Product> grid = new Grid<>(Product.class, false);

    private TextField name;
    private TextField code;
    private TextField brand;
    private TextField description;
    private TextField price;
    private DateTimePicker dateCreated;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Product> binder;

    private Product product;

    private final ProductService productService;
    private final BrandService brandService;

    @Autowired
    public ProductsView(ProductService productService, BrandService brandService) {
        this.productService = productService;
        this.brandService = brandService;
        addClassNames("products-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(Product::getName)
                .setHeader(ColumnNames.NAME)
                .setAutoWidth(true);
        grid.addColumn(Product::getCode)
                .setHeader(ColumnNames.CODE)
                .setAutoWidth(true);
        grid.addColumn(c -> c.getBrand().getName())
                .setHeader(ColumnNames.BRAND)
                .setAutoWidth(true);
        grid.addColumn(Product::getDescription)
                .setHeader(ColumnNames.DESCRIPTION)
                .setAutoWidth(true);
        grid.addColumn(Product::getPrice)
                .setHeader(ColumnNames.PRICE_PER_GRAM)
                .setAutoWidth(true);
        grid.addColumn(AbstractAuditableEntity::getDateCreated)
                .setHeader(ColumnNames.DATE_CREATED)
                .setAutoWidth(true);

        grid.setItems(query -> productService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PRODUCT_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(ProductsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Product.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(price).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("price");

//        binder.bindInstanceFields(this);
        binder.bind(this.name, "name");
        binder.bind(this.code, "code");
        binder.forField(this.brand)
                .withConverter(new StringToBrandConverter(this.brandService))
                .bind(Product::getBrand, Product::setBrand);
        binder.bind(this.description, "description");
        binder.forField(this.price)
                .withConverter(new StringToDoubleConverter("Price must be double!"))
                .bind(Product::getPrice, Product::setPrice);
        binder.bind(this.dateCreated, "dateCreated");


        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.product == null) {
                    this.product = new Product();
                }
                binder.writeBean(this.product);
                productService.update(this.product);
                clearForm();
                refreshGrid();
                Notification.show("Candy details stored.");
                UI.getCurrent().navigate(ProductsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the product details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> productId = event.getRouteParameters().get(PRODUCT_ID).map(Long::parseLong);
        if (productId.isPresent()) {
            Optional<Product> productFromBackend = productService.get(productId.get());
            if (productFromBackend.isPresent()) {
                populateForm(productFromBackend.get());
            } else {
                Notification.show(String.format("The requested product was not found, ID = %s", productId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(ProductsView.class);
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
        brand = new TextField("Brand");
        description = new TextField("Description");
        price = new TextField("Price");
        dateCreated = new DateTimePicker("Date Created");
        dateCreated.setStep(Duration.ofSeconds(1));
        formLayout.add(name, code, brand, description, price, dateCreated);

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

    private void populateForm(Product value) {
        this.product = value;
        binder.readBean(this.product);

    }
}
