package hu.klavorar.kubernetesorchestrator.repository;

import hu.klavorar.kubernetesorchestrator.model.entity.KubernetesDeployment;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KubernetesDeploymentRepository extends PagingAndSortingRepository<KubernetesDeployment, Long> {
    KubernetesDeployment findByNameAndNamespace(String name, String namespace);

}
