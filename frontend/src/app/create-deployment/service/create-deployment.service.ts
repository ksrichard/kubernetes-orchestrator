import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {tap} from 'rxjs/operators';
import {UploadDeploymentResponse} from './model/upload-deployment-response';

@Injectable({
  providedIn: 'root'
})
export class CreateDeploymentService {

  public response: UploadDeploymentResponse;

  constructor(private http: HttpClient) { }

  public createFromPlainText(successCallback: Function, errorCallback: Function, plainTextDefinition: string) {
    const headers = new HttpHeaders().set('Content-type', 'text/plain');
    this.http
      .post<UploadDeploymentResponse>(environment.backendApiDeploymentsUrl, plainTextDefinition, {headers: headers})
      .pipe(
        tap(
          data => {
            if (data) {
              this.response = data;
            }
          },
          error => errorCallback(error)
        )
      )
      .subscribe(
        response => {
          if (response) {
            this.response = response;
          }
        },
        err => errorCallback(err),
        () => successCallback(this.response)
      );
  }

}
