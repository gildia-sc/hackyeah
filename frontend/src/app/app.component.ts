import { Component, OnInit } from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {LoginService} from "./login/login.service";
import {MatSnackBar} from "@angular/material";
import {Principal} from "./login/principal.service";
import { RequestPendingService } from './request-pending/request-pending.service';
import { TitleService } from './title/title.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  progressBarMode = 'determinate';
  title = "";

  constructor(private loginService: LoginService,
              private snackBar: MatSnackBar,
              private principal: Principal,
              private requestPendingService: RequestPendingService,
              private titleService: TitleService) { }

  ngOnInit(): void {
    this.requestPendingService.requestPendingSubject.subscribe(value => {
      setTimeout(() => {
        this.progressBarMode = value ? 'indeterminate' : 'determinate';
      });
    });
    this.titleService.titleChanged$.subscribe(title => this.title = title);
  }

  logout() {
    console.log("Logout")
    this.loginService.logout()
    this.snackBar.open('Logout completed', null, {
      duration: 3000
    });
  }

  getAccountLink(): string {
    if (this.principal.getUserId() != null) {
      return '/players/' + this.principal.getUserId()
    }
    return ''
  }

  isAuthenticated() {
    return this.principal.isAuthenticated()
  }

  isAnonymous() {
    return !this.principal.isAuthenticated()
  }
}
