import { Component } from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {LoginService} from "./login/login.service";
import {MatSnackBar} from "@angular/material";
import {Principal} from "./login/principal.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  constructor(private loginService: LoginService,
              private snackBar: MatSnackBar,
              private principal: Principal) { }

  logout() {
    console.log("Logout")
    this.loginService.logout()
    this.snackBar.open('Logout completed', null, {
      duration: 3000
    });
  }

  isAuthenticated() {
    return this.principal.isAuthenticated()
  }

  isAnonymous() {
    return !this.principal.isAuthenticated()
  }
}
