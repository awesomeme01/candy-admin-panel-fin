package tg.bot.admin.panel.data.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.core.domain.SellingItem;

public interface SellingItemRepository extends JpaRepository<SellingItem, Long> {

}