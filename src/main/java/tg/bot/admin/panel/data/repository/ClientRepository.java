package tg.bot.admin.panel.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tg.bot.core.domain.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @Query("from Client where username = :username")
    Client findClientByUsername(@Param("username") String username);
}
