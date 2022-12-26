package tg.bot.admin.panel.data.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.domain.entity.MessageKeyboardButton;

import java.util.Optional;

public interface MessageKeyboardButtonRepository extends JpaRepository<MessageKeyboardButton, Long> {
    Optional<MessageKeyboardButton> findMessageKeyboardButtonByLabel(String label);
}