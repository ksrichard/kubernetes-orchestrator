package hu.klavorar.kubernetesorchestrator.service;

import hu.klavorar.kubernetesorchestrator.Application;
import hu.klavorar.kubernetesorchestrator.config.TestKubernetesClientConfig;
import hu.klavorar.kubernetesorchestrator.enums.ApplicationErrorCode;
import hu.klavorar.kubernetesorchestrator.model.dto.deployment.UploadDeploymentResponse;
import hu.klavorar.kubernetesorchestrator.model.entity.KubernetesDeployment;
import hu.klavorar.kubernetesorchestrator.repository.KubernetesDeploymentRepository;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static hu.klavorar.kubernetesorchestrator.enums.ApplicationErrorCode.INVALID_RESOURCE_DEFINITION;
import static hu.klavorar.kubernetesorchestrator.service.KubernetesDeploymentService.DEPLOYMENT_KIND;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/test-application.properties")
@SpringBootTest(classes = {TestKubernetesClientConfig.class, Application.class})
@DataJpaTest
public class CustomUserDetailsServiceTest {

    @Autowired
    private KubernetesServer kubernetesServer;

    @SpyBean
    private KubernetesClient kubernetesClient;

    @SpyBean
    private KubernetesDeploymentService service;

    @Autowired
    private KubernetesDeploymentRepository deploymentRepository;

    private String namespace = "test";

    @Test
    public void testApplyDeploymentTooManyDeploymentInDef() throws Exception {
        String resourceDefinition = getResourceDefinition("/example-deployments.yaml");
        UploadDeploymentResponse response = service.applyDeployment(resourceDefinition, namespace);
        assertFalse(response.getSuccess());
        assertEquals(response.getError().getErrorCode(), INVALID_RESOURCE_DEFINITION.getErrorCode());
        assertEquals(response.getError().getErrorMessage(), "Invalid resource definition! Only 1 Deployment can be in the resource definition!");
    }

    @Test
    public void testApplyDeploymentWrongDeploymentKind() throws Exception {
        String resourceDefinition = getResourceDefinition("/example-deployment-wrong-kind.yaml");
        UploadDeploymentResponse response = service.applyDeployment(resourceDefinition, namespace);
        assertFalse(response.getSuccess());
        assertEquals(response.getError().getErrorCode(), INVALID_RESOURCE_DEFINITION.getErrorCode());
        assertEquals(response.getError().getErrorMessage(), "Invalid resource definition! Invalid kubernetes resource kind! It must be '" + DEPLOYMENT_KIND + "'");
    }

    @Test
    public void testApplyDeploymentEmptyNamespace() throws Exception {
        String resourceDefinition = getResourceDefinition("/example-deployment-no-namespace.yaml");
        UploadDeploymentResponse response = service.applyDeployment(resourceDefinition, null);
        assertFalse(response.getSuccess());
        assertEquals(response.getError().getErrorCode(), ApplicationErrorCode.DEPLOYMENT_VALIDATION_ERROR.getErrorCode());
    }

    @Test
    public void testApplyDeploymentNewNamespaceCreated() throws Exception {
        kubernetesClient.namespaces().withName(namespace).delete();
        Namespace nameSpaceFoundBefore = kubernetesClient.namespaces().withName(namespace).get();
        assertNull(nameSpaceFoundBefore);

        String resourceDefinition = getResourceDefinition("/example-deployment.yaml");
        UploadDeploymentResponse response = service.applyDeployment(resourceDefinition, null);

        assertTrue(response.getSuccess());
        Namespace nameSpaceFoundAfter = kubernetesClient.namespaces().withName(namespace).get();
        assertNotNull(nameSpaceFoundAfter);
        assertTrue(kubernetesClient.apps().deployments().inAnyNamespace().list().getItems().size() > 0);
    }

    @Test
    public void testApplyDeploymentNoNewNamespaceCreated() throws Exception {
        Namespace nameSpaceFoundBefore = kubernetesClient.namespaces().createNew().withNewMetadata().withName(namespace).endMetadata().done();
        assertNotNull(nameSpaceFoundBefore);

        String resourceDefinition = getResourceDefinition("/example-deployment.yaml");
        UploadDeploymentResponse response = service.applyDeployment(resourceDefinition, null);

        assertTrue(response.getSuccess());
        Namespace nameSpaceFoundAfter = kubernetesClient.namespaces().withName(namespace).get();
        assertNotNull(nameSpaceFoundAfter);
        assertTrue(kubernetesClient.apps().deployments().inAnyNamespace().list().getItems().size() > 0);
    }

    @Test
    public void testGetDeploymentsNoDeployments() throws Exception {
        kubernetesClient.apps().deployments().delete();
        Pageable pageRequest = PageRequest.of(0,20);
        Page<KubernetesDeployment> deploymentPage = service.getDeployments(pageRequest);
        assertEquals(0, deploymentPage.getTotalElements());
    }

    @Test
    public void testGetDeployments() throws Exception {
        kubernetesClient.apps().deployments().delete();
        String uid = "some-UID";
        String resourceDefinition = getResourceDefinition("/example-deployment.yaml");
        Deployment deployment = kubernetesClient.apps().deployments().inNamespace(namespace).load(strToInputStream(resourceDefinition)).createOrReplace();
        deployment.getMetadata().setUid(uid);
        kubernetesClient.resource(deployment).createOrReplace();

        Pageable pageRequest = PageRequest.of(0,20);
        Page<KubernetesDeployment> deploymentPage = service.getDeployments(pageRequest);
        assertEquals(1, deploymentPage.getTotalElements());
    }

    @Test
    public void testGetDeploymentsChangedNumberOfDeployments() throws Exception {
        kubernetesClient.apps().deployments().delete();
        String uid = "some-UID";
        String resourceDefinition = getResourceDefinition("/example-deployment.yaml");
        Deployment deployment = kubernetesClient.apps().deployments().inNamespace(namespace).load(strToInputStream(resourceDefinition)).createOrReplace();
        deployment.getMetadata().setUid(uid);
        kubernetesClient.resource(deployment).createOrReplace();

        assertEquals(deploymentRepository.count(), 0);

        Pageable pageRequest = PageRequest.of(0,20);
        Page<KubernetesDeployment> deploymentPage = service.getDeployments(pageRequest);
        assertEquals(1, deploymentPage.getTotalElements());
        assertEquals(deploymentRepository.count(), 1);

        resourceDefinition = getResourceDefinition("/example-deployment2.yaml");
        deployment = kubernetesClient.apps().deployments().inNamespace(namespace).load(strToInputStream(resourceDefinition)).createOrReplace();
        deployment.getMetadata().setUid(uid + "2");
        kubernetesClient.resource(deployment).createOrReplace();

        assertEquals(deploymentRepository.count(), 1);

        deploymentPage = service.getDeployments(pageRequest);
        assertEquals(2, deploymentPage.getTotalElements());
        assertEquals(deploymentRepository.count(), 2);
    }

    private String getResourceDefinition(String fileName) throws IOException {
        File resourceDefFile = ResourceUtils.getFile(this.getClass().getResource(fileName));
        return new String(Files.readAllBytes(resourceDefFile.toPath()));
    }

    private InputStream strToInputStream(String input) {
        return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }

}
