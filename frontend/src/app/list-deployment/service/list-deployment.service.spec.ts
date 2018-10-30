import { TestBed } from '@angular/core/testing';

import { ListDeploymentService } from './list-deployment.service';

describe('ListDeploymentService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ListDeploymentService = TestBed.get(ListDeploymentService);
    expect(service).toBeTruthy();
  });
});
