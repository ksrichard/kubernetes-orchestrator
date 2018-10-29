package hu.klavorar.kubernetesorchestrator.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static okhttp3.TlsVersion.TLS_1_0;

@Configuration
public class TestKubernetesClientConfig {

    @Bean
    public KubernetesServer kubernetesServer() {
        return new KubernetesServer(true, true);
    }

    @Bean
    @Primary
    public KubernetesClient kubernetesClient() {
        KubernetesServer server = kubernetesServer();
        server.before();
        Config config = new ConfigBuilder()
                .withMasterUrl(server.getMockServer().url("/").toString())
                .withTrustCerts(true)
                .withTlsVersions(TLS_1_0)
                .build();
        return new DefaultKubernetesClient(config);
    }

}
