package hu.klavorar.kubernetesorchestrator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.klavorar.kubernetesorchestrator.Application;
import hu.klavorar.kubernetesorchestrator.config.TestKubernetesClientConfig;
import hu.klavorar.kubernetesorchestrator.enums.ApplicationErrorCode;
import hu.klavorar.kubernetesorchestrator.model.dto.common.ErrorResponse;
import hu.klavorar.kubernetesorchestrator.model.dto.deployment.UploadDeploymentResponse;
import hu.klavorar.kubernetesorchestrator.model.entity.KubernetesDeployment;
import hu.klavorar.kubernetesorchestrator.service.KubernetesDeploymentService;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/test-application.properties")
@SpringBootTest(classes = {TestKubernetesClientConfig.class, Application.class})
@AutoConfigureMockMvc
public class KubernetesRestControllerTest {

    @MockBean
    private KubernetesDeploymentService kubernetesDeploymentService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testLoginSuccessful() throws Exception {
        mockMvc.perform(
                post("/login")
                .content("{\n" +
                        "    \"username\": \"admin\",\n" +
                        "    \"password\": \"admin\"\n" +
                        "}")
        )
                .andExpect(status().isOk())
                .andExpect(header().exists("authorization"));
    }

    @Test
    public void testLoginFailed() throws Exception {
        mockMvc.perform(
                post("/login")
                        .content("{\n" +
                                "    \"username\": \"admin\",\n" +
                                "    \"password\": \"admin123\"\n" +
                                "}")
        )
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("authorization"));
    }

    @Test
    public void testListUnauthorized() throws Exception {
        mockMvc.perform(
                get("/resources/deployment")
        )
                .andExpect(status().is(403));
    }

    @Test
    public void testListAuthorized() throws Exception {
        mockMvc.perform(
                get("/resources/deployment")
                .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isOk());
    }

    @Test
    public void testListSuccess() throws Exception {
        List<KubernetesDeployment> deploymentList = new ArrayList<>();
        Page<KubernetesDeployment> deploymentPage = new PageImpl<>(deploymentList);
        when(kubernetesDeploymentService.getDeployments(any(Pageable.class))).thenReturn(deploymentPage);
        mockMvc.perform(
                get("/resources/deployment")
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json("{\"content\":[],\"pageable\":\"INSTANCE\",\"last\":true,\"totalPages\":1,\"totalElements\":0,\"size\":0,\"number\":0,\"numberOfElements\":0,\"first\":true,\"sort\":{\"sorted\":false,\"unsorted\":true}}"));
    }

    @Test
    public void testMultipartUploadDeploymentSuccess() throws Exception {
        UploadDeploymentResponse response = new UploadDeploymentResponse(Boolean.TRUE, null);
        String resourceDefStr = getResourceDefinition("/example-deployment.yaml");
        when(kubernetesDeploymentService.applyDeployment(resourceDefStr, null)).thenReturn(response);
        MockMultipartFile defFile = new MockMultipartFile("resourceDefinition", "example-deployment.yaml", "text/plain", resourceDefStr.getBytes());
        mockMvc.perform(
                multipart("/resources/deployment")
                        .file(defFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testMultipartUploadDeploymentFailed() throws Exception {
        UploadDeploymentResponse response = new UploadDeploymentResponse(Boolean.FALSE, new ErrorResponse(ApplicationErrorCode.APP_GENERIC_ERROR.getErrorCode(),"Some error"));
        String resourceDefStr = getResourceDefinition("/example-deployment.yaml");
        when(kubernetesDeploymentService.applyDeployment(resourceDefStr, null)).thenReturn(response);
        MockMultipartFile defFile = new MockMultipartFile("resourceDefinition", "example-deployment.yaml", "text/plain", resourceDefStr.getBytes());
        mockMvc.perform(
                multipart("/resources/deployment")
                        .file(defFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testMultipartUploadDeploymentSuccessWithNamespace() throws Exception {
        String namespace = "test2";
        UploadDeploymentResponse response = new UploadDeploymentResponse(Boolean.TRUE, null);
        String resourceDefStr = getResourceDefinition("/example-deployment.yaml");
        when(kubernetesDeploymentService.applyDeployment(resourceDefStr, namespace)).thenReturn(response);
        MockMultipartFile defFile = new MockMultipartFile("resourceDefinition", "example-deployment.yaml", "text/plain", resourceDefStr.getBytes());
        mockMvc.perform(
                multipart("/resources/deployment?namespace=" + namespace)
                        .file(defFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testPlaintextUploadDeploymentSuccess() throws Exception {
        UploadDeploymentResponse response = new UploadDeploymentResponse(Boolean.TRUE, null);
        String resourceDefStr = getResourceDefinition("/example-deployment.yaml");
        when(kubernetesDeploymentService.applyDeployment(resourceDefStr, null)).thenReturn(response);
        mockMvc.perform(
                post("/resources/deployment")
                        .content(resourceDefStr)
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testPlaintextUploadDeploymentFailed() throws Exception {
        UploadDeploymentResponse response = new UploadDeploymentResponse(Boolean.FALSE, new ErrorResponse(ApplicationErrorCode.APP_GENERIC_ERROR.getErrorCode(),"Some error"));
        String resourceDefStr = getResourceDefinition("/example-deployment.yaml");
        when(kubernetesDeploymentService.applyDeployment(resourceDefStr, null)).thenReturn(response);
        mockMvc.perform(
                post("/resources/deployment")
                        .content(resourceDefStr)
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testPlaintextUploadDeploymentSuccessWithNamespace() throws Exception {
        String namespace = "test2";
        UploadDeploymentResponse response = new UploadDeploymentResponse(Boolean.TRUE, null);
        String resourceDefStr = getResourceDefinition("/example-deployment.yaml");
        when(kubernetesDeploymentService.applyDeployment(resourceDefStr, namespace)).thenReturn(response);
        mockMvc.perform(
                post("/resources/deployment?namespace=" + namespace)
                        .content(resourceDefStr)
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testListKubernetesClientException() throws Exception {
        ErrorResponse response = new ErrorResponse(ApplicationErrorCode.KUBERNETES_GENERIC_ERROR.getErrorCode(), "Error during listing/creating kubernetes resources!");
        when(kubernetesDeploymentService.getDeployments(any(Pageable.class))).thenThrow(KubernetesClientException.class);
        mockMvc.perform(
                get("/resources/deployment")
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testListException() throws Exception {
        ErrorResponse response = new ErrorResponse(ApplicationErrorCode.APP_GENERIC_ERROR.getErrorCode(), "Unknown error during listing/creating kubernetes resources!");
        when(kubernetesDeploymentService.getDeployments(any(Pageable.class))).thenThrow(RuntimeException.class);
        mockMvc.perform(
                get("/resources/deployment")
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testMultipartUploadDeploymentKubernetesClientException() throws Exception {
        ErrorResponse response = new ErrorResponse(ApplicationErrorCode.KUBERNETES_GENERIC_ERROR.getErrorCode(), "Error during listing/creating kubernetes resources!");
        String resourceDefStr = getResourceDefinition("/example-deployment.yaml");
        when(kubernetesDeploymentService.applyDeployment(resourceDefStr, null)).thenThrow(KubernetesClientException.class);
        MockMultipartFile defFile = new MockMultipartFile("resourceDefinition", "example-deployment.yaml", "text/plain", resourceDefStr.getBytes());
        mockMvc.perform(
                multipart("/resources/deployment")
                        .file(defFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testMultipartUploadDeploymentException() throws Exception {
        ErrorResponse response = new ErrorResponse(ApplicationErrorCode.APP_GENERIC_ERROR.getErrorCode(), "Unknown error during listing/creating kubernetes resources!");
        String resourceDefStr = getResourceDefinition("/example-deployment.yaml");
        when(kubernetesDeploymentService.applyDeployment(resourceDefStr, null)).thenThrow(RuntimeException.class);
        MockMultipartFile defFile = new MockMultipartFile("resourceDefinition", "example-deployment.yaml", "text/plain", resourceDefStr.getBytes());
        mockMvc.perform(
                multipart("/resources/deployment")
                        .file(defFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testPlaintextUploadDeploymentKubernetesClientException() throws Exception {
        ErrorResponse response = new ErrorResponse(ApplicationErrorCode.KUBERNETES_GENERIC_ERROR.getErrorCode(), "Error during listing/creating kubernetes resources!");
        String resourceDefStr = getResourceDefinition("/example-deployment.yaml");
        when(kubernetesDeploymentService.applyDeployment(resourceDefStr, null)).thenThrow(KubernetesClientException.class);
        mockMvc.perform(
                post("/resources/deployment")
                        .content(resourceDefStr)
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    @Test
    public void testPlaintextUploadDeploymentException() throws Exception {
        ErrorResponse response = new ErrorResponse(ApplicationErrorCode.APP_GENERIC_ERROR.getErrorCode(), "Unknown error during listing/creating kubernetes resources!");
        String resourceDefStr = getResourceDefinition("/example-deployment.yaml");
        when(kubernetesDeploymentService.applyDeployment(resourceDefStr, null)).thenThrow(RuntimeException.class);
        mockMvc.perform(
                post("/resources/deployment")
                        .content(resourceDefStr)
                        .contentType(MediaType.TEXT_PLAIN)
                        .header("Authorization", getLoginHeaderValue())
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(getObjectAsJson(response)));
    }

    private String getObjectAsJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }

    private String getResourceDefinition(String fileName) throws IOException {
        File resourceDefFile = ResourceUtils.getFile(this.getClass().getResource(fileName));
        return new String(Files.readAllBytes(resourceDefFile.toPath()));
    }

    private String getLoginHeaderValue() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/login")
                        .content("{\n" +
                                "    \"username\": \"admin\",\n" +
                                "    \"password\": \"admin\"\n" +
                                "}")
        ).andReturn();
        return "Bearer " + result.getResponse().getHeader("authorization");
    }

}
