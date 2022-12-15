package tg.bot.admin.panel.data.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.core.domain.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

}