package hu.klavorar.kubernetesorchestrator.model.dto.deployment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeploymentValidationResponse {
    private Boolean isValid;
    private String errorMessage;
}
