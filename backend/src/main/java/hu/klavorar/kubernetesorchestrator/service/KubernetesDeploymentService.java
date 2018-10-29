package hu.klavorar.kubernetesorchestrator.service;

import hu.klavorar.kubernetesorchestrator.enums.ApplicationErrorCode;
import hu.klavorar.kubernetesorchestrator.model.dto.common.ErrorResponse;
import hu.klavorar.kubernetesorchestrator.model.dto.deployment.DeploymentValidationResponse;
import hu.klavorar.kubernetesorchestrator.model.dto.deployment.UploadDeploymentResponse;
import hu.klavorar.kubernetesorchestrator.model.entity.KubernetesContainer;
import hu.klavorar.kubernetesorchestrator.model.entity.KubernetesContainerPort;
import hu.klavorar.kubernetesorchestrator.model.entity.KubernetesDeployment;
import hu.klavorar.kubernetesorchestrator.repository.KubernetesContainerPortRepository;
import hu.klavorar.kubernetesorchestrator.repository.KubernetesContainerRepository;
import hu.klavorar.kubernetesorchestrator.repository.KubernetesDeploymentRepository;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A service to list/create Kubernetes deployment resources
 */
@Service
@Log4j2
public class KubernetesDeploymentService {

    public static final String DEPLOYMENT_KIND = "Deployment";

    @Autowired
    private KubernetesClient kubernetesClient;

    @Autowired
    private KubernetesDeploymentRepository deploymentRepository;

    @Autowired
    private KubernetesContainerRepository containerRepository;

    @Autowired
    private KubernetesContainerPortRepository containerPortRepository;

    /**
     * Applies deployment resource definition on the target Kubernetes cluster
     * @param resourceDefinition content of Kubernetes resource definition
     * @param namespace an optional namespace where we should apply the changes, it will be fetched from resource definition if null
     * @return UploadDeploymentResponse to show if the apply process was successful or not
     * @throws IOException
     */
    public UploadDeploymentResponse applyDeployment(String resourceDefinition, String namespace) throws IOException {
        log.info("Applying resource definition to Kubernetes: {}", resourceDefinition);
        // validating resource definition
        DeploymentValidationResponse validationResponse = isValidKubernetesDeployment(strToInputStream(resourceDefinition));
        if(!validationResponse.getIsValid()) {
            return new UploadDeploymentResponse(Boolean.FALSE, new ErrorResponse(ApplicationErrorCode.INVALID_RESOURCE_DEFINITION.getErrorCode(), "Invalid resource definition! " + validationResponse.getErrorMessage()));
        }

        // getting namespace from definition if not present as parameter
        if(namespace == null) {
            log.info("Explicit namespace not provided, getting from resource definition...");
            List<HasMetadata> result = kubernetesClient.load(strToInputStream(resourceDefinition)).get();
            namespace = result.get(0).getMetadata().getNamespace();

            // if namespace is still empty
            if(namespace == null) {
                log.error("Namespace can't be empty!");
                return new UploadDeploymentResponse(Boolean.FALSE, new ErrorResponse(ApplicationErrorCode.DEPLOYMENT_VALIDATION_ERROR.getErrorCode(), "Namespace can't be empty!"));
            }
        }

        // check if namespace exists
        Namespace foundNamespace = kubernetesClient.namespaces().withName(namespace).get();
        if(foundNamespace == null) { // create namespace
            kubernetesClient.namespaces().createNew().withNewMetadata().withName(namespace).endMetadata().done();
        }

        Deployment finalDeployment = kubernetesClient.apps().deployments().inNamespace(namespace).load(strToInputStream(resourceDefinition)).createOrReplace();

        if(finalDeployment != null) {
            log.info("Successfully created/updated deployment! {}", finalDeployment);
            return new UploadDeploymentResponse(Boolean.TRUE, null);
        }

        log.info("Deployment creation failed...");
        return new UploadDeploymentResponse(Boolean.FALSE, new ErrorResponse(ApplicationErrorCode.KUBERNETES_GENERIC_ERROR.getErrorCode(), "Could not create kubernetes resource!"));
    }

    /**
     * Method to get all Kubernetes deployments, sync with DB and return as a pageable result
     * @param pageable Pageable object to pass to repository method
     * @return Paged response of KubernetesDeployment objects
     */
    public Page<KubernetesDeployment> getDeployments(Pageable pageable) {
        log.info("Get Kubernetes deployments... {}", pageable);
        DeploymentList deployments = kubernetesClient.apps().deployments().inAnyNamespace().list();
        if(deployments != null && deployments.getItems() != null) {
            List<Deployment> deploymentItems = deployments.getItems();
            log.debug("Successfully fetched deployments from Kubernetes! {}", deploymentItems);
            log.info("Successfully fetched deployments from Kubernetes!");
            syncKubernetesDeploymentDetails(deploymentItems); // synchronizing database with result of kubernetes query
            return deploymentRepository.findAll(pageable);
        }
        return null;
    }

    /**
     * Create an InputStream from the given String
     * @param input String to convert to InputStream
     * @return InputStream
     */
    private InputStream strToInputStream(String input) {
        return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Checks if the actual kubernetes resource definition is valid or not
     * @param resourceDefinitionFileInputStream the InputStream of the Kubernetes resource definition
     * @return DeploymentValidationResponse which contains the response of the validation
     */
    private DeploymentValidationResponse isValidKubernetesDeployment(InputStream resourceDefinitionFileInputStream) {
        log.info("Validating resource definition...");
        List<HasMetadata> result = kubernetesClient.load(resourceDefinitionFileInputStream).get();

        if(result.size() > 1) {
            log.error("Only 1 Deployment can be in the resource definition!");
            return new DeploymentValidationResponse(Boolean.FALSE, "Only 1 Deployment can be in the resource definition!");
        }

        HasMetadata metadata = result.get(0);

        if(!metadata.getKind().equals(DEPLOYMENT_KIND)) {
            log.error("Invalid kubernetes resource kind! It must be '{}'", DEPLOYMENT_KIND);
            return new DeploymentValidationResponse(Boolean.FALSE, "Invalid kubernetes resource kind! It must be '" + DEPLOYMENT_KIND + "'");
        }

        log.info("Resource definition is valid!");
        return new DeploymentValidationResponse(Boolean.TRUE, null);
    }

    /**
     * Synchronization method to apply new changes in database when needed regarding Kubernetes deployments
     * @param deployments List of deployments to check
     */
    @Transactional
    public void syncKubernetesDeploymentDetails(List<Deployment> deployments) {
        log.info("Synchronizing deployments from Kubernetes with database: {}", deployments);
        // applying changes if there are
        deployments.forEach(deployment -> {
            String uid = deployment.getMetadata().getUid();
            String resourceVersion = deployment.getMetadata().getResourceVersion();
            KubernetesDeployment foundDeployment = deploymentRepository.findByNameAndNamespace(deployment.getMetadata().getName(), deployment.getMetadata().getNamespace());
            if(foundDeployment != null) {
                // check if we have been stored the latest version from deployment in DB
                if((foundDeployment.getResourceUid() != null && !foundDeployment.getResourceUid().equals(uid)) || (foundDeployment.getResourceVersion() != null && !foundDeployment.getResourceVersion().equals(resourceVersion))) {
                    createOrUpdateDeploymentFromResource(foundDeployment, deployment);
                }
            } else {
                createOrUpdateDeploymentFromResource(null, deployment);
            }
        });

        // delete all rows from database which is not existing in deployment list
        deploymentRepository.findAll().forEach(kubernetesDeployment -> {
            if (deployments.stream().noneMatch(deployment -> deployment != null && deployment.getMetadata() != null && kubernetesDeployment.getResourceUid().equals(deployment.getMetadata().getUid()))) {
                deploymentRepository.delete(kubernetesDeployment);
            }
        });
    }

    /**
     * Create or Update deployment DB related rows from a Kubernetes Deployment query result
     * @param retDeployment Optional deployment, if set, then it will updated
     * @param deployment Deployment object from Kubernetes client query result
     * @return KubernetesDeployment object which will be saved in tha database after the transaction has been successfully finished
     */
    private KubernetesDeployment createOrUpdateDeploymentFromResource(KubernetesDeployment retDeployment, Deployment deployment) {
        if(retDeployment == null) {
            retDeployment = new KubernetesDeployment();
        }
        // set basic details
        retDeployment.setName(deployment.getMetadata().getName());
        retDeployment.setNamespace(deployment.getMetadata().getNamespace());
        retDeployment.setResourceUid(deployment.getMetadata().getUid());
        retDeployment.setResourceVersion(deployment.getMetadata().getResourceVersion());

        // set containers
        List<KubernetesContainer> containersToSet = deployment.getSpec().getTemplate().getSpec().getContainers().stream().map(container -> {

            // set ports
            List<KubernetesContainerPort> currentPorts = container.getPorts().stream().map(port -> {
                KubernetesContainerPort currentPort = new KubernetesContainerPort();
                currentPort.setName(port.getName());
                currentPort.setContainerPort(port.getContainerPort());
                currentPort.setProtocol(port.getProtocol());
                return currentPort;
            }).collect(Collectors.toList());
            containerPortRepository.saveAll(currentPorts);

            // construct KubernetesContainer
            KubernetesContainer containerToUse = new KubernetesContainer();
            containerToUse.setName(container.getName());
            containerToUse.setImage(container.getImage());
            containerToUse.setPorts(currentPorts);

            return containerToUse;
        }).collect(Collectors.toList());
        containerRepository.saveAll(containersToSet);

        retDeployment.setContainers(containersToSet);

        deploymentRepository.save(retDeployment);
        log.info("Saving Deployment to database: {}", retDeployment);
        return retDeployment;
    }

}
