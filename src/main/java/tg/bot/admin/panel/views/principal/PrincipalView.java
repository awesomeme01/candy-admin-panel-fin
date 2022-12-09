package tg.bot.admin.panel.views.principal;

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
import tg.bot.core.domain.Principal;
import tg.bot.admin.panel.data.service.PrincipalService;
import tg.bot.admin.panel.views.MainLayout;

@PageTitle("Principal")
@Route(value = "principal/:principalID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class PrincipalView extends Div implements BeforeEnterObserver {

    private final String PRINCIPAL_ID = "principalID";
    private final String PRINCIPAL_EDIT_ROUTE_TEMPLATE = "principal/%s/edit";

    private final Grid<Principal> grid = new Grid<>(Principal.class, false);

    private TextField username;
    private TextField password;
    private Checkbox isActive;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Principal> binder;

    private Principal principal;

    private final PrincipalService principalService;

    @Autowired
    public PrincipalView(PrincipalService principalService) {
        this.principalService = principalService;
        addClassNames("principal-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(Principal::getId)
                .setHeader(ColumnNames.ID)
                .setAutoWidth(true);
        grid.addColumn(Principal::getUsername)
                .setHeader(ColumnNames.USERNAME)
                .setAutoWidth(true);
        grid.addColumn(Principal::getPassword)
                .setHeader(ColumnNames.PASSWORD)
                .setAutoWidth(true);
        LitRenderer<Principal> isActiveRenderer = LitRenderer.<Principal>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", isActive -> isActive.getIsActive() ? "check" : "minus").withProperty("color",
                        isActive -> isActive.getIsActive()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(isActiveRenderer).setHeader("Is Active").setAutoWidth(true);

        grid.setItems(query -> principalService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PRINCIPAL_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(PrincipalView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Principal.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.principal == null) {
                    this.principal = new Principal();
                }
                binder.writeBean(this.principal);
                principalService.update(this.principal);
                clearForm();
                refreshGrid();
                Notification.show("Principal details stored.");
                UI.getCurrent().navigate(PrincipalView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the principal details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> principalId = event.getRouteParameters().get(PRINCIPAL_ID).map(Long::parseLong);
        if (principalId.isPresent()) {
            Optional<Principal> principalFromBackend = principalService.get(principalId.get());
            if (principalFromBackend.isPresent()) {
                populateForm(principalFromBackend.get());
            } else {
                Notification.show(String.format("The requested principal was not found, ID = %s", principalId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(PrincipalView.class);
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
        username = new TextField("Username");
        password = new TextField("Password");
        isActive = new Checkbox("Is Active");
        formLayout.add(username, password, isActive);

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

    private void populateForm(Principal value) {
        this.principal = value;
        binder.readBean(this.principal);

    }
}
