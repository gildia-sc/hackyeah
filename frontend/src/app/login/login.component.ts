import { Component, OnInit } from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {HttpClient} from "@angular/common/http";
import {LoginService} from "./login.service";
import {Router} from "@angular/router";
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  authenticationError: boolean;
  loginForm = this.formBuilder.group({
    login: ['', [Validators.required, Validators.maxLength(50)]],
    password: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(100)]],
    rememberMe: [true]
  });

  constructor(private readonly formBuilder: FormBuilder,
              private readonly httpClient: HttpClient,
              private readonly router: Router,
              private loginService: LoginService,
              public snackBar: MatSnackBar) { }

  ngOnInit() {
  }

  login() {
    this.loginService
      .login({
        username: this.loginForm.get('login').value,
        password: this.loginForm.get('password').value,
        rememberMe: this.loginForm.get('rememberMe').value
      })
      .then(() => {
        this.authenticationError = false;
        this.router.navigate(['']);

        // previousState was set in the authExpiredInterceptor before being redirected to login modal.
        // since login is succesful, go to stored previousState and clear previousState
        // const redirect = this.stateStorageService.getUrl();
        // if (redirect) {
        //   this.stateStorageService.storeUrl(null);
        //   this.router.navigate([redirect]);
        // }
      })
      .catch(() => {
        this.authenticationError = true;
        this.snackBar.open('Authentication failed', null, {
          duration: 3000
        });
      });
  }
}
