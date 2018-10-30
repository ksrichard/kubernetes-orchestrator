import {ErrorResponse} from './error-response';

export class UploadDeploymentResponse {
  constructor(public success: boolean, public error: ErrorResponse) {}
}
