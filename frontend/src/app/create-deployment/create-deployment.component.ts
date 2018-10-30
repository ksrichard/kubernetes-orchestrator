import { Component, OnInit } from '@angular/core';
import {UploadDeploymentResponse} from './service/model/upload-deployment-response';
import {CreateDeploymentService} from './service/create-deployment.service';
import {FileItem, FileLikeObject, FileUploader, ParsedResponseHeaders} from 'ng2-file-upload';
import {environment} from '../../environments/environment';
import {AuthService} from '../auth/service/auth.service';

@Component({
  selector: 'app-create-deployment',
  templateUrl: './create-deployment.component.html',
  styleUrls: ['./create-deployment.component.css']
})
export class CreateDeploymentComponent implements OnInit {

  loading = false;
  error = '';
  successMessage = '';
  plainTextDefinition = '';

  // uploading
  public uploader: FileUploader;

  constructor(private createService: CreateDeploymentService, private auth: AuthService) { }

  ngOnInit() {
    this.initUploader();
  }

  initUploader() {
    this.uploader = new FileUploader({
      url: environment.backendApiDeploymentsUrl,
      authToken: 'Bearer ' + this.auth.getToken(),
      itemAlias: 'resourceDefinition'
    });
    this.uploader.onBeforeUploadItem = (item: FileItem) => this.uploadDefinitionStarted(item);
    this.uploader.onSuccessItem = (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => this.uploadDefinitionSuccess(item, response, status, headers);
    this.uploader.onWhenAddingFileFailed = (item: FileLikeObject, filter: any, options: any) => this.uploadDefinitionFailedToAddFile(item, filter, options);
    this.uploader.onErrorItem = (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => this.uploadDefinitionError(item, response, status, headers);
  }

  uploadDefinitionStarted(item: FileItem) {
    this.loading = true;
  }

  uploadDefinitionSuccess(item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) {
    this.loading = false;
    const uploadResponse: UploadDeploymentResponse = JSON.parse(response);
    if (uploadResponse.success) {
      this.successMessage = 'Successfully created Deployment!';
      this.error = '';
    } else {
      this.successMessage = '';
      this.error = uploadResponse.error.errorCode + ' - ' + uploadResponse.error.errorMessage;
    }
    this.initUploader();
  }

  uploadDefinitionFailedToAddFile(item: FileLikeObject, filter: any, options: any) {
    this.loading = false;
    this.error = 'Not allowed file type!';
    this.initUploader();
  }

  uploadDefinitionError(item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) {
    this.loading = false;
    console.error(response);
    this.error = 'Error during uploading file! Please try again!';
    this.initUploader();
  }

  createFromPlainText() {
    if (this.plainTextDefinition !== '') {
      this.loading = true;
      this.createService.createFromPlainText(
        (response: UploadDeploymentResponse) => {
          this.loading = false;
          if (response.success) {
            this.successMessage = 'Successfully created Deployment!';
            this.error = '';
            this.plainTextDefinition = '';
          } else {
            this.successMessage = '';
            this.error = response.error.errorCode + ' - ' + response.error.errorMessage;
          }
        },
        (err) => {
          console.log(err);
          this.loading = false;
          this.error = 'Error during creating Deployment! Please check the logs or try again!';
        },
        this.plainTextDefinition
      );
    } else {
      this.error = 'Please fill in the definition!';
      this.loading = false;
    }
  }

}
