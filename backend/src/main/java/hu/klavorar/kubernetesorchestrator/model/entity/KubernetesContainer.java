package hu.klavorar.kubernetesorchestrator.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class KubernetesContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String image;

    @OneToMany
    private List<KubernetesContainerPort> ports;
}
