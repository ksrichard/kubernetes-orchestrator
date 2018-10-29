package hu.klavorar.kubernetesorchestrator.model.dto.deployment;

import hu.klavorar.kubernetesorchestrator.model.dto.common.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadDeploymentResponse {

    private Boolean success;

    private ErrorResponse error;

}
