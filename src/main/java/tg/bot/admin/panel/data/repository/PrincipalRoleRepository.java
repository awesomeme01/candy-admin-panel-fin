package tg.bot.admin.panel.data.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.core.domain.PrincipalRole;

public interface PrincipalRoleRepository extends JpaRepository<PrincipalRole, Long> {

}