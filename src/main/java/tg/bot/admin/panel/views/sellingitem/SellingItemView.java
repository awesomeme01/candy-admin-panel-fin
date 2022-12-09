package tg.bot.admin.panel.views.sellingitem;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import tg.bot.admin.panel.data.service.BookingService;
import tg.bot.admin.panel.data.service.ProductService;
import tg.bot.admin.panel.views.a.util.ColumnNames;
import tg.bot.core.domain.SellingItem;
import tg.bot.admin.panel.data.service.SellingItemService;
import tg.bot.admin.panel.views.MainLayout;
import tg.bot.core.domain.base.AbstractAuditableEntity;
import tg.bot.admin.panel.views.a.util.converter.StringToProductConverter;
import tg.bot.admin.panel.views.a.util.converter.StringToBookingConverter;

@PageTitle("Selling Item")
@Route(value = "selling-item/:sellingItemID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class SellingItemView extends Div implements BeforeEnterObserver {

    private final String SELLINGITEM_ID = "sellingItemID";
    private final String SELLINGITEM_EDIT_ROUTE_TEMPLATE = "selling-item/%s/edit";

    private final Grid<SellingItem> grid = new Grid<>(SellingItem.class, false);

    private Upload picture;
    private Image picturePreview;
    private TextField product;
    private TextField bookedBy;
    private TextField weight;
    private TextField price;
    private DateTimePicker dateCreated;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<SellingItem> binder;

    private SellingItem sellingItem;

    private final SellingItemService sellingItemService;
    private final ProductService productService;
    private final BookingService bookingService;

    @Autowired
    public SellingItemView(SellingItemService sellingItemService, ProductService productService, BookingService bookingService) {
        this.productService = productService;
        this.bookingService = bookingService;
        this.sellingItemService = sellingItemService;
        addClassNames("selling-item-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        LitRenderer<SellingItem> pictureRenderer = LitRenderer
                .<SellingItem>of("<img style='height: 64px' src=${item.picture} />").withProperty("picture", item -> {
                    if (item != null && item.getPicture() != null) {
                        return "data:image;base64," + Base64.getEncoder().encodeToString(item.getPicture());
                    } else {
                        return "";
                    }
                });
        grid.addColumn(pictureRenderer).setHeader("Picture").setWidth("68px").setFlexGrow(0);

        grid.addColumn(p -> p.getProduct().getName())
                .setHeader(ColumnNames.NAME)
                .setAutoWidth(true);
        grid.addColumn(p -> p.getBooking().getClient().getName())
                .setHeader(ColumnNames.BOOKING_ID)
                .setAutoWidth(true);
        grid.addColumn(SellingItem::getWeight)
                .setHeader(ColumnNames.WEIGHT)
                .setAutoWidth(true);
        grid.addColumn(SellingItem::getPrice)
                .setHeader(ColumnNames.PRICE)
                .setAutoWidth(true);
        grid.addColumn(AbstractAuditableEntity::getDateCreated)
                .setHeader(ColumnNames.DATE_CREATED)
                .setAutoWidth(true);

        grid.setItems(query -> sellingItemService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SELLINGITEM_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(SellingItemView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(SellingItem.class);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(weight).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("weight");
        binder.forField(price).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("price");

//        binder.bindInstanceFields(this);
        binder.forField(this.product)
                .withConverter(new StringToProductConverter(productService))
                .bind(SellingItem::getProduct, SellingItem::setProduct);
        binder.forField(this.bookedBy)
                .withConverter(new StringToBookingConverter(bookingService))
                .bind(SellingItem::getBooking, SellingItem::setBooking);
        binder.forField(this.weight)
                .withConverter(new StringToDoubleConverter("Weight must be double!"))
                .bind(SellingItem::getWeight, SellingItem::setWeight);
        binder.forField(this.price)
                .withConverter(new StringToDoubleConverter("Price must be double!"))
                .bind(SellingItem::getPrice, SellingItem::setPrice);
        binder.bind(this.dateCreated, "dateCreated");

        attachImageUpload(picture, picturePreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.sellingItem == null) {
                    this.sellingItem = new SellingItem();
                }
                binder.writeBean(this.sellingItem);
                sellingItemService.update(this.sellingItem);
                clearForm();
                refreshGrid();
                Notification.show("SellingItem details stored.");
                UI.getCurrent().navigate(SellingItemView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the sellingItem details.");
            }
        });

    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> sellingItemId = event.getRouteParameters().get(SELLINGITEM_ID).map(Long::parseLong);
        if (sellingItemId.isPresent()) {
            Optional<SellingItem> sellingItemFromBackend = sellingItemService.get(sellingItemId.get());
            if (sellingItemFromBackend.isPresent()) {
                populateForm(sellingItemFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested sellingItem was not found, ID = %s", sellingItemId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(SellingItemView.class);
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
        Label pictureLabel = new Label("Picture");
        picturePreview = new Image();
        picturePreview.setWidth("100%");
        picture = new Upload();
        picture.getStyle().set("box-sizing", "border-box");
        picture.getElement().appendChild(picturePreview.getElement());
        product = new TextField("Product");
        bookedBy = new TextField("Booking Id");
        weight = new TextField("Weight");
        price = new TextField("Price");
        dateCreated = new DateTimePicker("Date Created");
        dateCreated.setStep(Duration.ofSeconds(1));
        formLayout.add(pictureLabel, picture, product, bookedBy, weight, price, dateCreated);

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

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            uploadBuffer.reset();
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            StreamResource resource = new StreamResource(e.getFileName(),
                    () -> new ByteArrayInputStream(uploadBuffer.toByteArray()));
            preview.setSrc(resource);
            preview.setVisible(true);
            if (this.sellingItem == null) {
                this.sellingItem = new SellingItem();
            }
            this.sellingItem.setPicture(uploadBuffer.toByteArray());
        });
        preview.setVisible(false);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SellingItem value) {
        this.sellingItem = value;
        binder.readBean(this.sellingItem);
        this.picturePreview.setVisible(value != null);
        if (value == null || value.getPicture() == null) {
            this.picture.clearFileList();
            this.picturePreview.setSrc("");
        } else {
            this.picturePreview.setSrc("data:image;base64," + Base64.getEncoder().encodeToString(value.getPicture()));
        }

    }
}
