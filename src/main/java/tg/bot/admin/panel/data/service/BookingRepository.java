package tg.bot.admin.panel.data.service;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.core.domain.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}