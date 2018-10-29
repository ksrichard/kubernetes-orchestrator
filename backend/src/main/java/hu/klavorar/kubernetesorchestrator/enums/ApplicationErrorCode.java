package hu.klavorar.kubernetesorchestrator.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationErrorCode {
    KUBERNETES_GENERIC_ERROR("KUBE-500"),
    INVALID_RESOURCE_DEFINITION("KUBE-501"),
    DEPLOYMENT_VALIDATION_ERROR("KUBE-502"),
    APP_GENERIC_ERROR("APP-503");

    private String errorCode;
}
