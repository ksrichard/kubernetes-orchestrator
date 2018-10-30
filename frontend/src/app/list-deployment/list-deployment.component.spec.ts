import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ListDeploymentComponent } from './list-deployment.component';

describe('ListDeploymentComponent', () => {
  let component: ListDeploymentComponent;
  let fixture: ComponentFixture<ListDeploymentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ListDeploymentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListDeploymentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
