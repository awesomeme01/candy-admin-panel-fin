package tg.bot.admin.panel.data.service;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.admin.panel.data.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
}