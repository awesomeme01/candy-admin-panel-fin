package tg.bot.admin.panel.views.a.util.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import tg.bot.admin.panel.data.service.BrandService;
import tg.bot.core.domain.Brand;

@Component
public class StringToBrandConverter implements Converter<String, Brand> {

    private final BrandService brandService;

    public StringToBrandConverter(BrandService brandService) {
        this.brandService = brandService;
    }

    @Override
    public Result<Brand> convertToModel(String s, ValueContext valueContext) {
        if (ObjectUtils.isEmpty(s)) {
            return Result.ok(null);
        }
        return Result.ok(brandService.findByName(s)
                .orElseGet(() -> brandService.update(new Brand(s, null))));
    }

    @Override
    public String convertToPresentation(Brand brand, ValueContext valueContext) {
        return brand.getName();
    }
}
