import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpParams} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {ListDeploymentResponse} from './model/list-deployment-response';
import {Deployment} from './model/deployment';
import {catchError, tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ListDeploymentService {

  public response: ListDeploymentResponse;

  constructor(private http: HttpClient) { }

  public list(successCallback: Function, errorCallback: Function, size: number, page: number) {
    const params = new HttpParams()
      .set('size', '' + size)
      .set('page', '' + page);

    this.http
      .get<ListDeploymentResponse>(environment.backendApiDeploymentsUrl, { params: params })
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
