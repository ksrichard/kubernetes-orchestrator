package hu.klavorar.kubernetesorchestrator.controller;

import hu.klavorar.kubernetesorchestrator.enums.ApplicationErrorCode;
import hu.klavorar.kubernetesorchestrator.model.dto.common.ErrorResponse;
import hu.klavorar.kubernetesorchestrator.model.dto.deployment.UploadDeploymentResponse;
import hu.klavorar.kubernetesorchestrator.model.entity.KubernetesDeployment;
import hu.klavorar.kubernetesorchestrator.service.KubernetesDeploymentService;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controller to handle calls regarding Kubernetes resources
 */
@RestController
@Log4j2
@RequestMapping(value = "/resources")
public class KubernetesRestController {

    @Autowired
    private KubernetesDeploymentService kubernetesDeploymentService;

    /**
     * List Kubernetes deployments, the response is paged
     * @param pageable Automatically fetched pageable values from query
     * @return Paged KubernetesDeployment response
     */
    @GetMapping(value = "/deployment")
    public Page<KubernetesDeployment> list(Pageable pageable) {
        return kubernetesDeploymentService.getDeployments(pageable);
    }

    /**
     * Create/Update Kubernetes deployment resource by uploading a resource definition file
     * @param resourceDefinitionFile MultipartFile
     * @param namespace Optional request parameter, if it's set then it will be used as the resource's namespace
     * @return UploadDeploymentResponse to show if any errors happened or not
     * @throws IOException
     */
    @PostMapping(value = "/deployment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UploadDeploymentResponse multipartUploadDeployment(
            @RequestParam("resourceDefinition") MultipartFile resourceDefinitionFile,
            @RequestParam(value = "namespace", required = false) String namespace) throws IOException {
        return kubernetesDeploymentService.applyDeployment(new String(resourceDefinitionFile.getBytes()), namespace);
    }

    /**
     * Create/Update Kubernetes deployment resource by sending plain text body as resource definition
     * @param resourceDefinition Kubernetes resource definition String
     * @param namespace Optional request parameter, if it's set then it will be used as the resource's namespace
     * @return UploadDeploymentResponse to show if any errors happened or not
     * @throws IOException
     */
    @PostMapping(value = "/deployment", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UploadDeploymentResponse plaintextUploadDeployment(
            @RequestBody String resourceDefinition,
            @RequestParam(value = "namespace", required = false) String namespace) throws IOException {
        return kubernetesDeploymentService.applyDeployment(resourceDefinition, namespace);
    }

    /**
     * Exception handler for Kubernetes client exceptions
     * @param ex Exception that has been thrown
     * @return ResponseEntity<ErrorResponse> ErrorResponse object with message
     */
    @ExceptionHandler({ KubernetesClientException.class })
    public ResponseEntity<ErrorResponse> handleKubernetesClientException(Exception ex) {
        ErrorResponse response = new ErrorResponse(ApplicationErrorCode.KUBERNETES_GENERIC_ERROR.getErrorCode(), "Error during listing/creating kubernetes resources!");
        log.error("Error during listing/creating kubernetes resources!", ex);
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for generic errors
     * @param ex Exception that has been thrown
     * @return ResponseEntity<ErrorResponse> ErrorResponse object with message
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = new ErrorResponse(ApplicationErrorCode.APP_GENERIC_ERROR.getErrorCode(), "Unknown error during listing/creating kubernetes resources!");
        log.error("Unknown error during listing/creating kubernetes resources!", ex);
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
