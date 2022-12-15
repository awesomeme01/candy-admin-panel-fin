package tg.bot.admin.panel.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = {"tg.bot.domain.entity", "tg.bot.core.domain", "tg.bot.admin.panel.data.entity"})
public class HibernateConfiguration {
}
