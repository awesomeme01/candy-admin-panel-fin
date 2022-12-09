package tg.bot.admin.panel.views.brands;

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
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.time.Duration;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.core.domain.Brand;
import tg.bot.admin.panel.data.service.BrandService;
import tg.bot.admin.panel.views.MainLayout;
import tg.bot.core.domain.base.AbstractAuditableEntity;

@PageTitle("Brands")
@Route(value = "brands/:brandID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class BrandsView extends Div implements BeforeEnterObserver {

    private final String BRAND_ID = "brandID";
    private final String BRAND_EDIT_ROUTE_TEMPLATE = "brands/%s/edit";

    private final Grid<Brand> grid = new Grid<>(Brand.class, false);

    private TextField name;
    private TextField description;
    private DateTimePicker dateCreated;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Brand> binder;

    private Brand brand;

    private final BrandService brandService;

    @Autowired
    public BrandsView(BrandService brandService) {
        this.brandService = brandService;
        addClassNames("brands-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(AbstractAuditableEntity::getId)
                .setAutoWidth(true)
                .setHeader(ColumnNames.ID);
        grid.addColumn(Brand::getName)
                .setHeader(ColumnNames.NAME)
                .setAutoWidth(true);
        grid.addColumn(Brand::getDescription)
                .setHeader(ColumnNames.DESCRIPTION)
                .setAutoWidth(true);
        grid.addColumn(AbstractAuditableEntity::getDateCreated)
                .setHeader(ColumnNames.DATE_CREATED)
                .setAutoWidth(true);
        grid.setItems(query -> brandService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(BRAND_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(BrandsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Brand.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.brand == null) {
                    this.brand = new Brand();
                }
                binder.writeBean(this.brand);
                brandService.update(this.brand);
                clearForm();
                refreshGrid();
                Notification.show("Brand details stored.");
                UI.getCurrent().navigate(BrandsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the brand details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> brandId = event.getRouteParameters().get(BRAND_ID).map(Long::parseLong);
        if (brandId.isPresent()) {
            Optional<Brand> brandFromBackend = brandService.get(brandId.get());
            if (brandFromBackend.isPresent()) {
                populateForm(brandFromBackend.get());
            } else {
                Notification.show(String.format("The requested brand was not found, ID = %s", brandId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(BrandsView.class);
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
        description = new TextField("Description");
        dateCreated = new DateTimePicker("Date Created");
        dateCreated.setStep(Duration.ofSeconds(1));
        formLayout.add(name, description, dateCreated);

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

    private void populateForm(Brand value) {
        this.brand = value;
        binder.readBean(this.brand);

    }
}
