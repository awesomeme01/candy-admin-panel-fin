package tg.bot.admin.panel.data.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tg.bot.admin.panel.data.repository.PrincipalRepository;
import tg.bot.core.domain.Principal;

@Service
public class PrincipalService {

    private final PrincipalRepository repository;
    private final PasswordEncoder encoder;

    @Autowired
    public PrincipalService(PrincipalRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public Optional<Principal> get(Long id) {
        return repository.findById(id);
    }

    public Optional<Principal> findByUsername(String username) {
        return repository.findPrincipalByUsername(username);
    }

    public Principal update(Principal entity) {
        entity.setPassword(encoder.encode(entity.getPassword()));
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
