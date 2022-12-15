package tg.bot.admin.panel.data.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tg.bot.admin.panel.data.repository.MessageResponseTemplateRepository;
import tg.bot.domain.entity.MessageResponseTemplate;

@Service
public class MessageResponseTemplateService {

    private final MessageResponseTemplateRepository repository;

    @Autowired
    public MessageResponseTemplateService(MessageResponseTemplateRepository repository) {
        this.repository = repository;
    }

    public Optional<MessageResponseTemplate> get(Long id) {
        return repository.findById(id);
    }

    public MessageResponseTemplate update(MessageResponseTemplate entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<MessageResponseTemplate> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
