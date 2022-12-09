package tg.bot.admin.panel.data.service;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.admin.panel.data.entity.SamplePerson;

public interface SamplePersonRepository extends JpaRepository<SamplePerson, Long> {

}