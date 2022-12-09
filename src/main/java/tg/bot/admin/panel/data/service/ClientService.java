package tg.bot.admin.panel.data.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tg.bot.core.domain.Brand;
import tg.bot.core.domain.Client;
import tg.bot.core.repository.ClientRepository;

import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository clientRepository) {
        this.repository = clientRepository;
    }


    public Optional<Client> findByName(String name) {
        return Optional.ofNullable(this.repository.findByUsername(name));
    }

    public Optional<Client> get(Long id) {
        return repository.findById(id);
    }

    public Client update(Client entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Client> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
