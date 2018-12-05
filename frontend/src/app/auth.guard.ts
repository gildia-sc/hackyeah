import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { Principal } from "./login/principal.service";
import { AccountService } from "./login/account.service";
import { catchError, map } from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router,
              private principal: Principal,
              private account: AccountService) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {

    return this.principal.isAuthenticated() || this.account.get().pipe(map(response => {
      this.principal.authenticate(response);
      return true;
    }), catchError(() => {
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return of(false)
    }))
  }
}
