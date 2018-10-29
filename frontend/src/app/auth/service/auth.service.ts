import { Injectable } from '@angular/core';
import {JwtHelperService} from '@auth0/angular-jwt';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {environment} from '../../../environments/environment';

export const AUTH_TOKEN_NAME = 'token';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  public token: string;

  constructor(public jwtHelper: JwtHelperService, private http: HttpClient) { }

  public loggedIn(): boolean {
    return !this.jwtHelper.isTokenExpired();
  }

  public login(username: string, password: string, successCallback: Function, errorCallback: Function): void {
    this.http
      .post(environment.backendApiLoginUrl, JSON.stringify({ username: username, password: password }), { observe: 'response' })
      .subscribe(
        (response: HttpResponse<any>) => {
          if (response.ok && response.headers.get('authorization')) {
            this.token = response.headers.get('authorization').replace('Bearer ', '');
            sessionStorage.setItem(AUTH_TOKEN_NAME, this.token);
          }
        },
            err => errorCallback(err),
            () => successCallback()
      );
  }

  public logout(): void {
    // clear token remove user from local storage to log user out
    this.token = null;
    sessionStorage.removeItem(AUTH_TOKEN_NAME);
  }

}
