Kubernetes Orchestrator Frontend
---
This project was designed to create/list Kubernetes Deployments through the backend API.
The project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 7.0.3.

Requirements
---
The latest `NPM` and `Angular cli` needs to be installed in order to get all the dependencies and start the application.

Angular CLI installation: `npm install -g @angular/cli`

Configuration
---
If you are not running the backend application on the `localhost` and port `8080`, you have to change the environment variables in file `src/environments/environment.ts`.
If the host or port is different just change (based on the following example) all the `localhost:8080` parts.
Example:
```typescript
export const environment = {
  production: false,
  backendApiLoginUrl: 'http://localhost:8080/login',
  backendApiDeploymentsUrl: 'http://localhost:8080/resources/deployment',
  jwtWhitelistedDomains: ['localhost:8080']
};
```
To change the prod variables, just edit the same way, the file `src/environments/environment.prod.ts`.

Running the application
---
To install dependencies run `npm install` in the root of the frontend project directory.

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

Build
---
Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.
