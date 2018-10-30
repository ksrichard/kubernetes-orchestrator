import { Component, OnInit } from '@angular/core';
import {ListDeploymentService} from './service/list-deployment.service';
import {ListDeploymentResponse} from './service/model/list-deployment-response';
import {Deployment} from './service/model/deployment';

@Component({
  selector: 'app-list-deployment',
  templateUrl: './list-deployment.component.html',
  styleUrls: ['./list-deployment.component.css']
})
export class ListDeploymentComponent implements OnInit {

  deployments: Deployment[];
  loading = false;
  error = '';

  // pagination
  itemsPerPage = 5;
  currentPage = 0;
  totalItems = 0;

  constructor(private listService: ListDeploymentService) { }

  ngOnInit() {
    this.loadDeployments(this.itemsPerPage, this.currentPage);
  }

  pageChanged(page: number) {
    this.currentPage = page;
    this.loadDeployments(this.itemsPerPage, page - 1);
  }

  loadDeployments(itemsPerPage, currentPage) {
    this.loading = true;
    this.listService.list(
      (response: ListDeploymentResponse) => {
        this.currentPage = response.number + 1;
        this.totalItems = response.totalElements;
        this.deployments = response.content;
        this.loading = false;
        this.error = '';
      },
      (err) => {
        console.log(err);
        this.error = 'Error during loading Kubernetes Deployments! Please try again or check error logs!';
        this.loading = false;
      }
      ,
      itemsPerPage,
      currentPage
    );
  }

}
