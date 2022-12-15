package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import org.springframework.util.ObjectUtils;
import tg.bot.admin.panel.data.service.ProductService;
import tg.bot.core.domain.Product;

import javax.persistence.EntityNotFoundException;

public class StringToProductConverter implements Converter<String, Product> {

    private final ProductService productService;
    public StringToProductConverter(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public Result<Product> convertToModel(String s, ValueContext valueContext) {
        if (ObjectUtils.isEmpty(s)) {
            return Result.ok(null);
        }
        return Result.ok(productService.findByCode(s)
                .orElseThrow(() -> new EntityNotFoundException("There is no Product for code = " + s)));
    }

    @Override
    public String convertToPresentation(Product product, ValueContext valueContext) {
        return product != null ? product.getCode() : null;
    }
}
