package tg.bot.admin.panel.data.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.domain.entity.MessageResponseTemplate;

public interface MessageResponseTemplateRepository extends JpaRepository<MessageResponseTemplate, Long> {

}