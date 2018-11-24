import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Route, RouterModule } from '@angular/router';
import { TableListComponent } from './table/table-list/table-list.component';
import { RegisterComponent } from './register/register.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { LoginComponent } from "./login/login.component";
import { HomepageComponent } from "./homepage/homepage.component";

import {
  MatButtonModule,
  MatCardModule, MatChipsModule, MatCheckboxModule,
  MatDividerModule,
  MatExpansionModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule, MatProgressBarModule,
  MatSnackBarModule,
  MatToolbarModule
} from '@angular/material';
import { MatchComponent } from "./match/single/match.component";
import { TableEditComponent } from './table/table-edit/table-edit.component';

const ROUTES: Route[] = [
  { path: '', component: HomepageComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'tables', component: TableListComponent },
  { path: 'matches/:tableCode', component: MatchComponent, pathMatch: 'full' }
  { path: 'tables/:tableCode', component: TableEditComponent },
];

@NgModule({
  declarations: [
    AppComponent,
    TableListComponent,
    RegisterComponent,
    LoginComponent,
    HomepageComponent,
    MatchComponent,
    TableEditComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule,

    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule,
    MatExpansionModule,
    MatIconModule,
    MatMenuModule,
    MatProgressBarModule,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatSnackBarModule,
    RouterModule.forRoot(ROUTES)
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
