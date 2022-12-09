package tg.bot.admin.panel.data.service;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.core.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}