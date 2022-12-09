package tg.bot.admin.panel.data.service;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.domain.entity.MessageKeyboard;

public interface MessageKeyboardRepository extends JpaRepository<MessageKeyboard, Long> {

}