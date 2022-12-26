package tg.bot.admin.panel.data.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tg.bot.admin.panel.data.repository.CurrencyRepository;
import tg.bot.core.domain.Currency;

import javax.persistence.EntityNotFoundException;

@Service
public class CurrencyService {

    private final CurrencyRepository repository;

    @Autowired
    public CurrencyService(CurrencyRepository repository) {
        this.repository = repository;
    }

    public Currency findByCode(String code) {
        return repository.findCurrencyByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Couldn't find Currency by code = " + code));
    }

    public Optional<Currency> get(Long id) {
        return repository.findById(id);
    }

    public Currency update(Currency entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Currency> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
