package tg.bot.admin.panel.data.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tg.bot.core.domain.Principal;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class PrincipalService {

    private final PrincipalRepository repository;

    @Autowired
    public PrincipalService(PrincipalRepository repository) {
        this.repository = repository;
    }

    public Optional<Principal> get(Long id) {
        return repository.findById(id);
    }

    public Principal update(Principal entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Principal> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
