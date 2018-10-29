package hu.klavorar.kubernetesorchestrator;

import hu.klavorar.kubernetesorchestrator.config.TestKubernetesClientConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/test-application.properties")
@SpringBootTest(classes = {TestKubernetesClientConfig.class, Application.class})
public class ApplicationTests {

    @Test
    public void contextLoads() {
    }

}
