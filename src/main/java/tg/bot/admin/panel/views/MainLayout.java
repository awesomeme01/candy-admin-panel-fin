package tg.bot.admin.panel.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import tg.bot.admin.panel.components.appnav.AppNav;
import tg.bot.admin.panel.components.appnav.AppNavItem;
import tg.bot.admin.panel.data.entity.User;
import tg.bot.admin.panel.security.AuthenticatedUser;
import tg.bot.admin.panel.views.booking.BookingView;
import tg.bot.admin.panel.views.brands.BrandsView;
import tg.bot.admin.panel.views.clients.ClientsView;
import tg.bot.admin.panel.views.currency.CurrencyView;
import tg.bot.admin.panel.views.messagekeyboard.MessageKeyboardView;
import tg.bot.admin.panel.views.messagekeyboardbutton.MessageKeyboardButtonView;
import tg.bot.admin.panel.views.messagetemplate.MessageTemplateView;
import tg.bot.admin.panel.views.orders.OrdersView;
import tg.bot.admin.panel.views.payments.PaymentsView;
import tg.bot.admin.panel.views.principal.PrincipalView;
import tg.bot.admin.panel.views.principalrole.PrincipalRoleView;
import tg.bot.admin.panel.views.products.ProductsView;
import tg.bot.admin.panel.views.sellingitem.SellingItemView;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H2 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("Admin Panel");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        AppNav nav = new AppNav();
        AppNavItem catalogGroup = new AppNavItem("Catalog");
        catalogGroup.setExpanded(true);

        AppNavItem systemGroup = new AppNavItem("System");
        systemGroup.setExpanded(true);

        AppNavItem ordersGroup = new AppNavItem("Orders");
        ordersGroup.setExpanded(true);

        AppNavItem messageGroup = new AppNavItem("Telegram Messages");
        messageGroup.setExpanded(true);

        if (accessChecker.hasAccess(BrandsView.class)) {
            catalogGroup.addItem(new AppNavItem("Brands", BrandsView.class, "la la-tag"));

        }
        if (accessChecker.hasAccess(ProductsView.class)) {
            catalogGroup.addItem(new AppNavItem("Products", ProductsView.class, "la la-envira"));

        }
        if (accessChecker.hasAccess(SellingItemView.class)) {
            catalogGroup.addItem(new AppNavItem("Selling Item", SellingItemView.class, "la la-folder"));

        }
        if (accessChecker.hasAccess(BookingView.class)) {
            ordersGroup.addItem(new AppNavItem("Booking", BookingView.class, "la la-columns"));

        }
        if (accessChecker.hasAccess(ClientsView.class)) {
            ordersGroup.addItem(new AppNavItem("Clients", ClientsView.class, "la la-user"));

        }
        if (accessChecker.hasAccess(PaymentsView.class)) {
            ordersGroup.addItem(new AppNavItem("Payments", PaymentsView.class, "la la-dollar-sign"));

        }
        if (accessChecker.hasAccess(OrdersView.class)) {
            ordersGroup.addItem(new AppNavItem("Orders", OrdersView.class, "la la-dolly"));

        }
        if (accessChecker.hasAccess(CurrencyView.class)) {
            ordersGroup.addItem(new AppNavItem("Currency", CurrencyView.class, "la la-comment-dollar"));

        }
        if (accessChecker.hasAccess(MessageTemplateView.class)) {
            messageGroup.addItem(new AppNavItem("Message Template", MessageTemplateView.class, "lab la-facebook-messenger"));

        }
        if (accessChecker.hasAccess(MessageKeyboardView.class)) {
            messageGroup.addItem(new AppNavItem("Message Keyboard", MessageKeyboardView.class, "la la-keyboard"));

        }
        if (accessChecker.hasAccess(MessageKeyboardButtonView.class)) {
            messageGroup.addItem(new AppNavItem("Message Keyboard Button", MessageKeyboardButtonView.class, "la la-keyboard"));

        }
        if (accessChecker.hasAccess(PrincipalView.class)) {
            systemGroup.addItem(new AppNavItem("Principal", PrincipalView.class, "la la-user-lock"));

        }
        if (accessChecker.hasAccess(PrincipalRoleView.class)) {
            systemGroup.addItem(new AppNavItem("Principal Role", PrincipalRoleView.class, "la la-lock"));

        }
        nav.addItem(catalogGroup);
        nav.addItem(ordersGroup);
        nav.addItem(messageGroup);
        nav.addItem(systemGroup);
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            var picture = user.getProfilePicture();
            if (ObjectUtils.isNotEmpty(picture)) {
                StreamResource resource = new StreamResource("profile-pic",
                        () -> new ByteArrayInputStream(picture));
                avatar.setImageResource(resource);
            }
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
