package hu.klavorar.kubernetesorchestrator.repository;

import hu.klavorar.kubernetesorchestrator.model.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
    ApplicationUser findByUsername(String username);
}
