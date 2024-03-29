package tg.bot.admin.panel.views.principalrole;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tg.bot.admin.panel.data.Role;
import tg.bot.admin.panel.data.service.PrincipalRoleService;
import tg.bot.admin.panel.data.service.PrincipalService;
import tg.bot.admin.panel.views.MainLayout;
import tg.bot.admin.panel.views.a.util.ButtonUtil;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.core.domain.Principal;
import tg.bot.core.domain.PrincipalRole;

import javax.annotation.security.RolesAllowed;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Principal Role")
@Route(value = "principalRole/:principalRoleID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class PrincipalRoleView extends Div implements BeforeEnterObserver {

    private final String PRINCIPALROLE_ID = "principalRoleID";
    private final String PRINCIPALROLE_EDIT_ROUTE_TEMPLATE = "principalRole/%s/edit";

    private final Grid<PrincipalRole> grid = new Grid<>(PrincipalRole.class, false);
    private final PrincipalService principalService;
    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final BeanValidationBinder<PrincipalRole> binder;
    private final PrincipalRoleService principalRoleService;
    private Select<String> role;
    private ComboBox<Principal> principal;
    private PrincipalRole principalRole;

    @Autowired
    public PrincipalRoleView(PrincipalService principalService, PrincipalRoleService principalRoleService) {
        this.principalService = principalService;
        this.principalRoleService = principalRoleService;
        addClassNames("principal-role-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn(PrincipalRole::getRole)
                .setHeader(ColumnNames.NAME)
                .setAutoWidth(true);
        grid.addColumn(pr -> pr.getPrincipal().getUsername())
                .setHeader(ColumnNames.PRINCIPAL)
                .setAutoWidth(true);
        grid.addComponentColumn(item -> ButtonUtil.defaultDeleteFromGrid(click -> {
            this.principalRoleService.delete(item.getId());
            refreshGrid();
        })).setWidth("140px").setFlexGrow(0).setHeader("Actions");
        grid.setItems(query -> principalRoleService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PRINCIPALROLE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(PrincipalRoleView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(PrincipalRole.class);

        // Bind fields. This is where you'd define e.g. validation rules

//        binder.bindInstanceFields(this);
        binder.bind(principal, "principal");
        binder.bind(role, "role");

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.principalRole == null) {
                    this.principalRole = new PrincipalRole();
                }
                binder.writeBean(this.principalRole);
                principalRoleService.update(this.principalRole);
                clearForm();
                refreshGrid();
                Notification.show("PrincipalRole details stored.");
                UI.getCurrent().navigate(PrincipalRoleView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the principalRole details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> principalRoleId = event.getRouteParameters().get(PRINCIPALROLE_ID).map(Long::parseLong);
        if (principalRoleId.isPresent()) {
            Optional<PrincipalRole> principalRoleFromBackend = principalRoleService.get(principalRoleId.get());
            if (principalRoleFromBackend.isPresent()) {
                populateForm(principalRoleFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested principalRole was not found, ID = %s", principalRoleId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(PrincipalRoleView.class);
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
        role = new Select<>();
        role.setItems(Arrays.stream(Role.values()).map(Enum::name).collect(Collectors.toList()));
        role.setItemLabelGenerator(Object::toString);
        role.setLabel("Role");
        principal = new ComboBox<>("Principal");
        principal.setItems(this.principalService.list(Pageable.unpaged()).toList());
        principal.setItemLabelGenerator(Principal::getUsername);
        formLayout.add(role, principal);

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

    private void populateForm(PrincipalRole value) {
        this.principalRole = value;
        binder.readBean(this.principalRole);

    }
}
