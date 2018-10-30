import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import {RouterModule} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AuthModule} from './auth/auth.module';
import {AuthGuard} from './auth/guard/auth.guard';
import { LogoutComponent } from './logout/logout.component';
import { ListDeploymentComponent } from './list-deployment/list-deployment.component';
import { SimplePageComponent } from './simple-page/simple-page.component';
import {ListDeploymentService} from './list-deployment/service/list-deployment.service';
import {HttpClientModule} from '../../node_modules/@angular/common/http';
import {NgxPaginationModule} from 'ngx-pagination';
import {JwtModule} from '@auth0/angular-jwt';
import {AUTH_TOKEN_NAME} from './auth/service/auth.service';
import {environment} from '../environments/environment';
import { CreateDeploymentComponent } from './create-deployment/create-deployment.component';
import {CreateDeploymentService} from './create-deployment/service/create-deployment.service';
import {FileUploadModule} from 'ng2-file-upload';

const routes = [
  { path: 'login', component: LoginComponent },
  { path: 'logout', component: LogoutComponent, canActivate: [AuthGuard] },
  { path: 'list-deployments', component: ListDeploymentComponent, canActivate: [AuthGuard] },
  { path: 'create-deployment', component: CreateDeploymentComponent, canActivate: [AuthGuard] },
  { path: '', component: HomeComponent, canActivate: [AuthGuard]},
  { path: '**', redirectTo: '' }
];

export function tokenGetter() {
  return sessionStorage.getItem(AUTH_TOKEN_NAME);
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomeComponent,
    LogoutComponent,
    ListDeploymentComponent,
    SimplePageComponent,
    CreateDeploymentComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: tokenGetter,
        whitelistedDomains: environment.jwtWhitelistedDomains
      }
    }),
    FormsModule,
    ReactiveFormsModule,
    AuthModule,
    NgxPaginationModule,
    FileUploadModule,
    RouterModule.forRoot(routes)
  ],
  providers: [ListDeploymentService, CreateDeploymentService],
  bootstrap: [AppComponent]
})
export class AppModule { }
