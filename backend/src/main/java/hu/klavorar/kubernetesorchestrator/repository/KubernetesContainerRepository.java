package hu.klavorar.kubernetesorchestrator.repository;

import hu.klavorar.kubernetesorchestrator.model.entity.KubernetesContainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KubernetesContainerRepository extends JpaRepository<KubernetesContainer, Long> {
}
