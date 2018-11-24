import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Route, RouterModule } from '@angular/router';
import { DeleteTableDialog} from './table/table-list/table-list.component';
import { RegisterComponent } from './register/register.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { LoginComponent } from "./login/login.component";
import { HomepageComponent } from "./homepage/homepage.component";
import { TableListComponent } from "./table/table-list/table-list.component";
import { PlayerListComponent } from './player/player-list/player-list.component';
import { PlayerTeamsComponent } from './player/player-teams/player-teams.component';
import { PlayerMatchesComponent } from './player/player-matches/player-matches.component';

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
  MatToolbarModule,
  MatDialogModule,
  MatGridListModule
} from '@angular/material';
import { MatchComponent } from "./match/single/match.component";
import { TableEditComponent } from './table/table-edit/table-edit.component';
import { TableLinksComponent } from './table/table-links/table-links.component';


const ROUTES: Route[] = [
  { path: '', component: HomepageComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'tables', component: TableListComponent },
  { path: 'matches/:tableCode', component: MatchComponent, pathMatch: 'full' },
  { path: 'tables/:tableCode', component: TableEditComponent },
  { path: 'tables/:tableCode/links', component: TableLinksComponent },
  { path: 'matches/:tableCode', component: MatchComponent, pathMatch: 'full' },
  { path: 'players', component: PlayerListComponent },
  { path: 'players/:id/teams', component: PlayerTeamsComponent },
  { path: 'players/:id/matches', component: PlayerMatchesComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    TableListComponent,
    DeleteTableDialog,
    RegisterComponent,
    LoginComponent,
    HomepageComponent,
    MatchComponent,
    TableEditComponent,
    PlayerListComponent,
    PlayerTeamsComponent,
    PlayerMatchesComponent,
    TableLinksComponent
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
    MatGridListModule,
    MatIconModule,
    MatMenuModule,
    MatProgressBarModule,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatDialogModule,
    RouterModule.forRoot(ROUTES)
  ],
  providers: [],
  bootstrap: [AppComponent],
  entryComponents: [DeleteTableDialog]
})
export class AppModule {
}
