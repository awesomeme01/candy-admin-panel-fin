package tg.bot.admin.panel.data.service;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.domain.entity.MessageKeyboardButton;

public interface MessageKeyboardButtonRepository extends JpaRepository<MessageKeyboardButton, Long> {

}