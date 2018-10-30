package hu.klavorar.kubernetesorchestrator.controller;

import hu.klavorar.kubernetesorchestrator.model.dto.common.LoginDetails;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to provide fake methods to have in Swagger UI
 */
@RestController
@RequestMapping(value = "/")
public class AuthController {

    /**
     * Fake method to have /login enabled on Swagger UI
     */
    @ApiOperation("JWT Login")
    @PostMapping("/login")
    public void fakeLogin(@ApiParam("body") @RequestBody LoginDetails details) {
        throw new IllegalStateException("This method shouldn't be called. It's implemented by Spring Security filters.");
    }

}
