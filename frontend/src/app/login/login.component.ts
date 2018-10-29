import { Component, OnInit } from '@angular/core';
import {AuthService} from '../auth/service/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  model: any = {};
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) { }

  ngOnInit() {
    if (this.auth.loggedIn()) {
      this.router.navigate(['/']);
    }
  }

  login() {
    this.loading = true;
    this.auth.login(
      this.model.username,
      this.model.password,
      () => {
        this.router.navigate(['/']);
      },
      (error) => {
        this.error = 'Username or password is incorrect!';
        this.loading = false;
      }
    );
  }

}
