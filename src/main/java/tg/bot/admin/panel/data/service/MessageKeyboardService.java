package tg.bot.admin.panel.data.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tg.bot.admin.panel.data.repository.MessageKeyboardRepository;
import tg.bot.domain.entity.MessageKeyboard;

@Service
public class MessageKeyboardService {

    private final MessageKeyboardRepository repository;

    @Autowired
    public MessageKeyboardService(MessageKeyboardRepository repository) {
        this.repository = repository;
    }

    public Optional<MessageKeyboard> get(Long id) {
        return repository.findById(id);
    }

    public MessageKeyboard update(MessageKeyboard entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<MessageKeyboard> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
