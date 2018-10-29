package hu.klavorar.kubernetesorchestrator.service;

import hu.klavorar.kubernetesorchestrator.Application;
import hu.klavorar.kubernetesorchestrator.config.TestKubernetesClientConfig;
import hu.klavorar.kubernetesorchestrator.enums.ApplicationErrorCode;
import hu.klavorar.kubernetesorchestrator.model.dto.deployment.UploadDeploymentResponse;
import hu.klavorar.kubernetesorchestrator.model.entity.ApplicationUser;
import hu.klavorar.kubernetesorchestrator.model.entity.KubernetesDeployment;
import hu.klavorar.kubernetesorchestrator.repository.ApplicationUserRepository;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/test-application.properties")
@SpringBootTest(classes = {TestKubernetesClientConfig.class, Application.class})
public class CustomUserDetailsServiceTest {

    @MockBean
    private ApplicationUserRepository applicationUserRepository;

    @SpyBean
    private CustomUserDetailsService service;

    private String username = "admin";

    @Test(expected = UsernameNotFoundException.class)
    public void testLoadUserByUsernameUsernameNull() {
        when(applicationUserRepository.findByUsername(username)).thenReturn(null);
        service.loadUserByUsername(username);
    }

    @Test
    public void testLoadUserByUsernameSuccessful() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername(username);
        user.setPassword("somepassword123");
        when(applicationUserRepository.findByUsername(username)).thenReturn(user);
        UserDetails details = service.loadUserByUsername(username);
        assertEquals(details.getUsername(), user.getUsername());
        assertEquals(details.getPassword(), user.getPassword());
    }

}
