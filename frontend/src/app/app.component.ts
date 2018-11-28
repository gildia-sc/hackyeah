import { Component, Input, OnInit } from '@angular/core';
import { LoginService } from "./login/login.service";
import { MatSnackBar } from "@angular/material";
import { Principal } from "./login/principal.service";
import { RequestPendingService } from './request-pending/request-pending.service';
import { TitleService } from './title/title.service';
import { AccountService } from "./login/account.service";
import { Router } from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  accountLink = '';
  progressBarMode = 'determinate';
  title = "";

  constructor(private loginService: LoginService,
              private snackBar: MatSnackBar,
              private principal: Principal,
              private account: AccountService,
              private requestPendingService: RequestPendingService,
              private router: Router,
              private titleService: TitleService) {
  }

  ngOnInit(): void {
    this.requestPendingService.requestPendingSubject.subscribe(value => {
      setTimeout(() => {
        this.progressBarMode = value ? 'indeterminate' : 'determinate';
      });
    });
    if (!this.principal.isAuthenticated()) {
      this.account.get().subscribe(response => this.principal.authenticate(response.body), err => console.log(err));
    }
    this.titleService.titleChanged$.subscribe(title => this.title = title);
    this.principal.getAuthenticationState().subscribe(principal => this.accountLink = '/players/' + principal.id);
  }

  logout() {
    console.log("Logout")
    this.loginService.logout()
    this.snackBar.open('Logout completed', null, {
      duration: 3000
    })
      .afterDismissed()
      .subscribe(() => this.router.navigate(['/']));
  }

  isAuthenticated(): boolean {
    return this.principal.isAuthenticated()
  }

  isAnonymous(): boolean {
    return !this.isAuthenticated()
  }
}
