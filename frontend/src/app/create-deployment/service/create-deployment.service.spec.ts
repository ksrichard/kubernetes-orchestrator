import { TestBed } from '@angular/core/testing';

import { CreateDeploymentService } from './create-deployment.service';

describe('CreateDeploymentService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CreateDeploymentService = TestBed.get(CreateDeploymentService);
    expect(service).toBeTruthy();
  });
});
