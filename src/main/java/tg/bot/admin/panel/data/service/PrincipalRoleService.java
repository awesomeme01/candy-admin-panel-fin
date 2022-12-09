package tg.bot.admin.panel.data.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tg.bot.core.domain.PrincipalRole;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class PrincipalRoleService {

    private final PrincipalRoleRepository repository;

    @Autowired
    public PrincipalRoleService(PrincipalRoleRepository repository) {
        this.repository = repository;
    }

    public Optional<PrincipalRole> get(Long id) {
        return repository.findById(id);
    }

    public PrincipalRole update(PrincipalRole entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<PrincipalRole> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
