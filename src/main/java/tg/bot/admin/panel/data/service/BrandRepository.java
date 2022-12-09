package tg.bot.admin.panel.data.service;


import org.springframework.data.jpa.repository.JpaRepository;
import tg.bot.core.domain.Brand;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findBrandByName(String s);
}