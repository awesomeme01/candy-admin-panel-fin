package tg.bot.admin.panel.data.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.core.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}