package tg.bot.admin.panel.data.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.core.domain.Principal;

public interface PrincipalRepository extends JpaRepository<Principal, Long> {

}