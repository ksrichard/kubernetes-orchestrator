package hu.klavorar.kubernetesorchestrator.repository;

import hu.klavorar.kubernetesorchestrator.model.entity.KubernetesContainerPort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KubernetesContainerPortRepository extends JpaRepository<KubernetesContainerPort, Long> {
}
