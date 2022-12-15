package tg.bot.admin.panel.data.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tg.bot.admin.panel.data.repository.MessageKeyboardButtonRepository;
import tg.bot.domain.entity.MessageKeyboardButton;

@Service
public class MessageKeyboardButtonService {

    private final MessageKeyboardButtonRepository repository;

    @Autowired
    public MessageKeyboardButtonService(MessageKeyboardButtonRepository repository) {
        this.repository = repository;
    }

    public Optional<MessageKeyboardButton> get(Long id) {
        return repository.findById(id);
    }

    public MessageKeyboardButton update(MessageKeyboardButton entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<MessageKeyboardButton> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
