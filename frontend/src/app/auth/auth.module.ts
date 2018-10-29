import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {AUTH_TOKEN_NAME, AuthService} from './service/auth.service';
import {JwtModule} from '@auth0/angular-jwt';
import {HttpClientModule} from '@angular/common/http';

export function tokenGetter() {
  return sessionStorage.getItem(AUTH_TOKEN_NAME);
}

@NgModule({
  imports: [
    CommonModule,
    HttpClientModule,
    JwtModule.forRoot({
      config: {
        tokenGetter: tokenGetter
      }
    })
  ],
  declarations: [],
  providers: [
    AuthService
  ]
})
export class AuthModule { }
