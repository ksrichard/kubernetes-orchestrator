package hu.klavorar.kubernetesorchestrator.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class KubernetesDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String resourceUid;

    private String resourceVersion;

    private String namespace;

    private String name;

    @OneToMany
    private List<KubernetesContainer> containers;

}
