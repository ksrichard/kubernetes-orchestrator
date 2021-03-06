Kubernetes Orchestrator Backend API
---
This application is designed to creating/listing kubernetes Deployments on any cluster configured.

It is using `Spring Boot` for the REST API and using an in-memory database (H2) for storing Kubernetes deployment informations.

The application is using JWT as a security layer, by default a user is generated for the application: `admin/admin`

By default the application is running on the port `8080`, but it can be changed in the `application.properties` file (`server.port` property), or using command-line parameter `-Dserver.port=9090` when running from JAR.

All other configurations can be changed in `application.properties` like JWT options (`jwt.` prefix) or datasource options to change database (`spring.datasource.` prefix) etc...

Requirements
---
The project needs `Oracle JDK 8` and `Maven` installed to be able to run/build/test the project.
To use the project you have to configure the kubernetes client (see below) and make sure that the `cors.allowedOrigins` is set properly.
This property's value must be set to the URL of frontend project. Example (_this is the default value_):
```properties
cors.allowedOrigins=http://localhost:4200
```

List Kubernetes Deployments workflow
---
The application is designed to save all the listed Kubernetes Deployments in database.
The workflow is the following:
- When a request comes to list the Deployments of the configured Kubernetes cluster, the application will first request those type of resources from Kubernetes
- Then synchronizing the result of Kubernetes query with the database of the application to make it up-to-date
- Then returning the results from the freshly updated database with pagination

Create/Update Kubernetes Deployments workflow
---
- When a request comes to create a Deployment, it goes through a validation phase
- Then if the resource definition is valid, a namespace will be checked
- If no explicit namespace was sent in a URL parameter, then it will be fetched from the resource definition
- If the namespace does not exists, it will be created
- Then the Deployment will be applied to the kubernetes cluster

Kubernetes client configuration
---
To configure a cluster you can have several options, that you can set in the `backend/src/main/resources/application.properties`

All the properties with prefix `spring.cloud.kubernetes.client.` are about to configure the Kubernetes Client

Of course these properties can be set using command-line options when running from jar:
```bash
mvn clean install package
java -jar target/kubernetes-orchestrator-1.0.0-SNAPSHOT.jar -Dspring.cloud.kubernetes.client.master-url=https://localhost:6445 ...
```

Example configuration for a cluster:
```properties
# Kubernetes configuration
spring.cloud.kubernetes.client.master-url=https://localhost:6445
spring.cloud.kubernetes.client.username=
spring.cloud.kubernetes.client.password=
spring.cloud.kubernetes.client.trust-certs=true
spring.cloud.kubernetes.client.client-cert-data=LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM5RENDQWR5Z0F3SUJBZ0lJRHY5emVaR3dTbVl3RFFZSktvWklodmNOQVFFTEJRQXdGVEVUTUJFR0ExVUUKQXhNS2EzVmlaWEp1WlhSbGN6QWVGdzB4T0RBNU1EVXdOekk1TlRoYUZ3MHhPVEV3TWpnd09EQTBOVFJhTURZeApGekFWQmdOVkJBb1REbk41YzNSbGJUcHRZWE4wWlhKek1Sc3dHUVlEVlFRREV4SmtiMk5yWlhJdFptOXlMV1JsCmMydDBiM0F3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQzRXTyt4OGg0anhuYXQKM1UzYWpUWTdWZ1l6Y3FYbE9zNGZOd1BzUEErZ1IxRFhqZDlwaVFGcllkbzBxL2VEL2l5Wmk1eGtodHpQKzh2dgp3UFRZUlQvSU0reFEzR29XZC9Sa016SE8vN2s1em9xdXF6eVdBN202Z2ZKUjVDM2w2aE5sVFJmRnJSZ2ZDaWJpCnRHRVlwVTdKdFpHZFVaQTJzbWptT2pCYS9HQVNwQ0RtaUF0RWk3b0hXSk9wVWdqbUdkRGhvUmtpb3RPNkhNZnQKdU9HcklmMFJ2TkE3UUVjUWFZUnkrVFdvb1RUNDUyVU1kWVFOOWRJVWRGbXByRy9ySEYyM09KdXpYVUoyZ0EzaQpTejNyZVVFZHdXQzBKaG5iclVkcDJOUDNnK2JhdUxidmhUT3VvWXZ2V3o3YXYvQ0tETnBjUkpHN0swQWFNbDI4CnFKVkE4SEV0QWdNQkFBR2pKekFsTUE0R0ExVWREd0VCL3dRRUF3SUZvREFUQmdOVkhTVUVEREFLQmdnckJnRUYKQlFjREFqQU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FRRUF0ZFVPVGRKOUlLV0hkclZjWGR1S3VRZVpTOThxbnZ4cQpkeGJzNiswUVk2VFNmRjY0QmRDNXFsMm1sNStKemZjTFI0SVdSUERrZ2hHSkF6enNKcUpvN1g2SGJjUm90NVIrClAvRExhNlFrbTJsQmhxdlVNZHZNRjhoOEJvYXJRTkRyUHd6TGl4YURpWTgvSjAyL25NU1VHSENQTzhWUjZOWngKbVZXTzM0L3lIckdJZmJvOStENkx6K2hXbUlCM2xxa1p6ZHVoRFRtUDJTclJzZ0FZMWN6L05kSmRaVnRGM0c0dwpIRTlWRGg2YmV3UUhmMVVWdjNTZW4vYVMyT0lZa2MvMCtwK2tGQitKMWovZVBMZmY3M1N3WDJrZ3lITmt2aU45Cm8zL2I4b2NUSDhObjlMRGZjc2lqR3VLS1U5RGdOVDNKZ3FkT0lvY2MyMFRPZlMwRk9SYlNNQT09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K
spring.cloud.kubernetes.client.client-key-data=LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFb3dJQkFBS0NBUUVBdUZqdnNmSWVJOFoycmQxTjJvMDJPMVlHTTNLbDVUck9IemNEN0R3UG9FZFExNDNmCmFZa0JhMkhhTkt2M2cvNHNtWXVjWkliY3ovdkw3OEQwMkVVL3lEUHNVTnhxRm5mMFpETXh6dis1T2M2S3JxczgKbGdPNXVvSHlVZVF0NWVvVFpVMFh4YTBZSHdvbTRyUmhHS1ZPeWJXUm5WR1FOckpvNWpvd1d2eGdFcVFnNW9nTApSSXU2QjFpVHFWSUk1aG5RNGFFWklxTFR1aHpIN2JqaHF5SDlFYnpRTzBCSEVHbUVjdmsxcUtFMCtPZGxESFdFCkRmWFNGSFJacWF4djZ4eGR0emliczExQ2RvQU40a3M5NjNsQkhjRmd0Q1laMjYxSGFkalQ5NFBtMnJpMjc0VXoKcnFHTDcxcysyci93aWd6YVhFU1J1eXRBR2pKZHZLaVZRUEJ4TFFJREFRQUJBb0lCQURwU2VOTDFaSyt3MUU5QgpXcVl3VXQ0RzQwTlhuRUNma0NLMEJ4dXdETW1LaG5zTHVZanIvWFQ1cWsxTHNxMGh0djZFK3QrYkF6VVIxbjF0CnNJalRyOUFwRFRTdjV5aGdud01zWnkrMEcrZTcvb2d3U0tTMVhRY3lvWFZqR1ZwSjZnVDhsTzdIU29CWGQrSDEKYTFxZXdnUnkyelRJWHpYT3ovMDJuNkhKZ3FPaW0xKzZOYzZqczVKbE1NU2REVUNkTnUzYnRqRU80WFYxYnU3TwprdGk3QjN6eFdtSUdiOXVuazhheEFrdHNieFo0ZlRLRHl4ZXJkYWxmUU1zSHppdXhScjlteldaWTFKd2FXcE9KCkNaeTFxQVNLV3NhcEFlYU5kbE1NbnYwM1BDSTV6cnl4RzIvY2I5ZisvZDl2K2hkYWxVbjNHNEoyUVVkMnZ3V0oKNlBiQ2w4RUNnWUVBeFBmVmw5VVAvYlhKM1VpemxocnFQRXJod1hxaGNrVTN0ai9OOEUveTV5WlM0bUE5UUtZWQpBMzdBaUNBMUN3ekE0Zy94elJKNU9yTUlxMDRYbVdhRHRpQ2M2QjZoTjZzMW56aVpYd0l3L3Y4bGxCTGNEZndpClZzZ3hXc2JIbUt2QTBPRXBJd1V2VXRZQllhWWJjN2k2VkxJZE1ZVFV0WjZIakhWL0tPOEZKQVVDZ1lFQTc1aksKN1p3UHhhQitjeGJpdW04eUozcUtEWDNRcy9SSFhyaUt5aWF2d3hLTEFKc1IwV3ZoaktGODJGaTBpT2g1N21SSwpRZDNZN2ZKUDdGdzNUdFVCaGdWcUlab2p2bnFhdWxtOXdrdFpvam1reFlmSWhWS01vZ3kyN21PTmNka1BQejVrCkVIUFFITDdKN1FkdUhpWnU3OHFsV0ZjY0MyL3Yrb21taWtQbUNRa0NnWUE5NmszNUdWK2FlQitKY01ZTEJGRG4KUFA2MExCRTk4MllzNHVlZ2gvRXFHaERwMmlqRy9xOER3ajlSU3hhQmI3UGVHL2I5YUxZMDdueG1KUGtrSG1aKwp4TWV0NjVydi9lQ3dPVUYwN1NKcnRkYWhNa0I4SU9yR2JzMmM1R2x2bzJYTUEwdEVGK2dsV09JOFNOUE9UNTQ2CjYzWmp6NFp4eFBDRkJaWkFUc3BGNVFLQmdGZlhJWmJKWDNMcDYxSVFjdUU5QVJzK0xyelJncUZuRG45b2t1R1kKRzFvZVF6M2VuRUsyRllQMGRJdnd3WWRzQzcvZG51ZlZDTUg3MDFkcFc1R1lRQzI5Y0hLUFMwbDdNNXpjWjlpQwp2WUxmTlV5Q0FuNzJpNzZoZ0xPWFpmVDJTa082Z1A4VEpwcFRURDZOT2M3dDhrQVJnRnNKM0t0SXg2ZUhyYm9GCk9LSFpBb0dCQU1TYjlTQlAxa2d5WXpWdTdlNzA4RFFsclRhTWxlbzFNREkwTU9BbStZakQzN1E2YU0rekx0UkQKcWFhR2lKSS9lZUVtSUxzOXVCeis5Y1Q0STdTajk1M1RObjJGbHVLWXlSVjhIWkZmRVhrNlN1V2VMaStBUzN5cQptR2JQcXl5MEJ4SStUOWJHdkFlRXUwUE1OQmdPZFZEc3d1RUg2cktXNGJUU3l0em9DREM2Ci0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0tCg==
```
These configurations can be found in `~/.kube/config` file when kubectl is configured.
Example:
```
apiVersion: v1
clusters:
- cluster:
    insecure-skip-tls-verify: true
    server: https://localhost:6445
  name: docker-for-desktop-cluster
contexts:
- context:
    cluster: docker-for-desktop-cluster
    user: docker-for-desktop
  name: docker-for-desktop
current-context: docker-for-desktop
kind: Config
preferences: {}
users:
- name: docker-for-desktop
  user:
    client-certificate-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM5RENDQWR5Z0F3SUJBZ0lJRHY5emVaR3dTbVl3RFFZSktvWklodmNOQVFFTEJRQXdGVEVUTUJFR0ExVUUKQXhNS2EzVmlaWEp1WlhSbGN6QWVGdzB4T0RBNU1EVXdOekk1TlRoYUZ3MHhPVEV3TWpnd09EQTBOVFJhTURZeApGekFWQmdOVkJBb1REbk41YzNSbGJUcHRZWE4wWlhKek1Sc3dHUVlEVlFRREV4SmtiMk5yWlhJdFptOXlMV1JsCmMydDBiM0F3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQzRXTyt4OGg0anhuYXQKM1UzYWpUWTdWZ1l6Y3FYbE9zNGZOd1BzUEErZ1IxRFhqZDlwaVFGcllkbzBxL2VEL2l5Wmk1eGtodHpQKzh2dgp3UFRZUlQvSU0reFEzR29XZC9Sa016SE8vN2s1em9xdXF6eVdBN202Z2ZKUjVDM2w2aE5sVFJmRnJSZ2ZDaWJpCnRHRVlwVTdKdFpHZFVaQTJzbWptT2pCYS9HQVNwQ0RtaUF0RWk3b0hXSk9wVWdqbUdkRGhvUmtpb3RPNkhNZnQKdU9HcklmMFJ2TkE3UUVjUWFZUnkrVFdvb1RUNDUyVU1kWVFOOWRJVWRGbXByRy9ySEYyM09KdXpYVUoyZ0EzaQpTejNyZVVFZHdXQzBKaG5iclVkcDJOUDNnK2JhdUxidmhUT3VvWXZ2V3o3YXYvQ0tETnBjUkpHN0swQWFNbDI4CnFKVkE4SEV0QWdNQkFBR2pKekFsTUE0R0ExVWREd0VCL3dRRUF3SUZvREFUQmdOVkhTVUVEREFLQmdnckJnRUYKQlFjREFqQU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FRRUF0ZFVPVGRKOUlLV0hkclZjWGR1S3VRZVpTOThxbnZ4cQpkeGJzNiswUVk2VFNmRjY0QmRDNXFsMm1sNStKemZjTFI0SVdSUERrZ2hHSkF6enNKcUpvN1g2SGJjUm90NVIrClAvRExhNlFrbTJsQmhxdlVNZHZNRjhoOEJvYXJRTkRyUHd6TGl4YURpWTgvSjAyL25NU1VHSENQTzhWUjZOWngKbVZXTzM0L3lIckdJZmJvOStENkx6K2hXbUlCM2xxa1p6ZHVoRFRtUDJTclJzZ0FZMWN6L05kSmRaVnRGM0c0dwpIRTlWRGg2YmV3UUhmMVVWdjNTZW4vYVMyT0lZa2MvMCtwK2tGQitKMWovZVBMZmY3M1N3WDJrZ3lITmt2aU45Cm8zL2I4b2NUSDhObjlMRGZjc2lqR3VLS1U5RGdOVDNKZ3FkT0lvY2MyMFRPZlMwRk9SYlNNQT09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K
    client-key-data: LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFb3dJQkFBS0NBUUVBdUZqdnNmSWVJOFoycmQxTjJvMDJPMVlHTTNLbDVUck9IemNEN0R3UG9FZFExNDNmCmFZa0JhMkhhTkt2M2cvNHNtWXVjWkliY3ovdkw3OEQwMkVVL3lEUHNVTnhxRm5mMFpETXh6dis1T2M2S3JxczgKbGdPNXVvSHlVZVF0NWVvVFpVMFh4YTBZSHdvbTRyUmhHS1ZPeWJXUm5WR1FOckpvNWpvd1d2eGdFcVFnNW9nTApSSXU2QjFpVHFWSUk1aG5RNGFFWklxTFR1aHpIN2JqaHF5SDlFYnpRTzBCSEVHbUVjdmsxcUtFMCtPZGxESFdFCkRmWFNGSFJacWF4djZ4eGR0emliczExQ2RvQU40a3M5NjNsQkhjRmd0Q1laMjYxSGFkalQ5NFBtMnJpMjc0VXoKcnFHTDcxcysyci93aWd6YVhFU1J1eXRBR2pKZHZLaVZRUEJ4TFFJREFRQUJBb0lCQURwU2VOTDFaSyt3MUU5QgpXcVl3VXQ0RzQwTlhuRUNma0NLMEJ4dXdETW1LaG5zTHVZanIvWFQ1cWsxTHNxMGh0djZFK3QrYkF6VVIxbjF0CnNJalRyOUFwRFRTdjV5aGdud01zWnkrMEcrZTcvb2d3U0tTMVhRY3lvWFZqR1ZwSjZnVDhsTzdIU29CWGQrSDEKYTFxZXdnUnkyelRJWHpYT3ovMDJuNkhKZ3FPaW0xKzZOYzZqczVKbE1NU2REVUNkTnUzYnRqRU80WFYxYnU3TwprdGk3QjN6eFdtSUdiOXVuazhheEFrdHNieFo0ZlRLRHl4ZXJkYWxmUU1zSHppdXhScjlteldaWTFKd2FXcE9KCkNaeTFxQVNLV3NhcEFlYU5kbE1NbnYwM1BDSTV6cnl4RzIvY2I5ZisvZDl2K2hkYWxVbjNHNEoyUVVkMnZ3V0oKNlBiQ2w4RUNnWUVBeFBmVmw5VVAvYlhKM1VpemxocnFQRXJod1hxaGNrVTN0ai9OOEUveTV5WlM0bUE5UUtZWQpBMzdBaUNBMUN3ekE0Zy94elJKNU9yTUlxMDRYbVdhRHRpQ2M2QjZoTjZzMW56aVpYd0l3L3Y4bGxCTGNEZndpClZzZ3hXc2JIbUt2QTBPRXBJd1V2VXRZQllhWWJjN2k2VkxJZE1ZVFV0WjZIakhWL0tPOEZKQVVDZ1lFQTc1aksKN1p3UHhhQitjeGJpdW04eUozcUtEWDNRcy9SSFhyaUt5aWF2d3hLTEFKc1IwV3ZoaktGODJGaTBpT2g1N21SSwpRZDNZN2ZKUDdGdzNUdFVCaGdWcUlab2p2bnFhdWxtOXdrdFpvam1reFlmSWhWS01vZ3kyN21PTmNka1BQejVrCkVIUFFITDdKN1FkdUhpWnU3OHFsV0ZjY0MyL3Yrb21taWtQbUNRa0NnWUE5NmszNUdWK2FlQitKY01ZTEJGRG4KUFA2MExCRTk4MllzNHVlZ2gvRXFHaERwMmlqRy9xOER3ajlSU3hhQmI3UGVHL2I5YUxZMDdueG1KUGtrSG1aKwp4TWV0NjVydi9lQ3dPVUYwN1NKcnRkYWhNa0I4SU9yR2JzMmM1R2x2bzJYTUEwdEVGK2dsV09JOFNOUE9UNTQ2CjYzWmp6NFp4eFBDRkJaWkFUc3BGNVFLQmdGZlhJWmJKWDNMcDYxSVFjdUU5QVJzK0xyelJncUZuRG45b2t1R1kKRzFvZVF6M2VuRUsyRllQMGRJdnd3WWRzQzcvZG51ZlZDTUg3MDFkcFc1R1lRQzI5Y0hLUFMwbDdNNXpjWjlpQwp2WUxmTlV5Q0FuNzJpNzZoZ0xPWFpmVDJTa082Z1A4VEpwcFRURDZOT2M3dDhrQVJnRnNKM0t0SXg2ZUhyYm9GCk9LSFpBb0dCQU1TYjlTQlAxa2d5WXpWdTdlNzA4RFFsclRhTWxlbzFNREkwTU9BbStZakQzN1E2YU0rekx0UkQKcWFhR2lKSS9lZUVtSUxzOXVCeis5Y1Q0STdTajk1M1RObjJGbHVLWXlSVjhIWkZmRVhrNlN1V2VMaStBUzN5cQptR2JQcXl5MEJ4SStUOWJHdkFlRXUwUE1OQmdPZFZEc3d1RUg2cktXNGJUU3l0em9DREM2Ci0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0tCg==
```

Running the application
---
This application is a `Spring Boot` based API using `Maven`.

To run the application you can use the following maven command from the root of this project: 
```bash
mvn clean install spring-boot:run
```
or you can package the application as jar and run:
```bash
mvn clean install package
java -jar target/kubernetes-orchestrator-1.0.0-SNAPSHOT.jar
```

Code coverage
---
To check the code coverage of this project run the following maven command in the root of this project:
```bash
mvn clean install test
```
This will run all the tests in the project and generate an HTML Jacoco report.
The report can be found in `target/jacoco-out/index.html`

Manual testing
---
You can import a test collection to Postman from the following URL:
```
https://www.getpostman.com/collections/06199f9b2af213bb936e
```

OR

You can test APIs with built-in Swagger UI, to do this, start the application, then open up the following URL in your browser:
```
http://localhost:8080/swagger-ui.html
```
To test main API calls:

- Click on `auth-controller` line, click on `Try it out`, replace username and password fields in JSON content with `admin`
and click on `Execute` button at the bottom.
- After getting back the answer you have to check the response headers and copy the full content of `Authorization` header (`Bearer ....`).
- Scroll up on the page and click on `Authorize` button
- paste the previously copied `Authorization` field value into `Value` input and press on the `Authorize` button

From this point all of your queries will contain the needed JWT token to authenticate yourself, so feel free to try out any of the calls.
