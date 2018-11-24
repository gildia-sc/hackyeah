import { Component, OnInit } from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {LoginService} from "./login/login.service";
import {MatSnackBar} from "@angular/material";
import {Principal} from "./login/principal.service";
import { RequestPendingService } from './request-pending/request-pending.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  progressBarMode = 'determinate';

  constructor(private loginService: LoginService,
              private snackBar: MatSnackBar,
              private principal: Principal,
              private requestPendingService: RequestPendingService) { }

  ngOnInit(): void {
    this.requestPendingService.requestPendingSubject.subscribe(value => {
      this.progressBarMode = value ? 'indeterminate' : 'determinate';
    });
  }

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
