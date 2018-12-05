import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Route, RouterModule } from '@angular/router';
import { DeleteDialog} from './util/delete-dialog/delete-dialog.component';
import { RegisterComponent } from './register/register.component';
import { ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { LoginComponent } from "./login/login.component";
import { HomepageComponent } from "./homepage/homepage.component";
import { TableListComponent } from "./table/table-list/table-list.component";
import { PlayerListComponent } from './player/player-list/player-list.component';
import { PlayerTeamsComponent } from './player/player-teams/player-teams.component';
import { PlayerMatchesComponent } from './player/player-matches/player-matches.component';
import {ImageCropperComponent, CropperSettings} from 'ng2-img-cropper';

import {
  MatButtonModule,
  MatCardModule, MatChipsModule, MatCheckboxModule,
  MatDividerModule,
  MatExpansionModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule,
  MatProgressBarModule,
  MatSnackBarModule,
  MatToolbarModule,
  MatDialogModule,
  MatGridListModule
} from '@angular/material';
import { MatchComponent } from "./match/single/match.component";
import { TableEditComponent } from './table/table-edit/table-edit.component';
import { TableLinksComponent } from './table/table-links/table-links.component';
import { PendingRequestInterceptor } from './request-pending/pending-request.interceptor';
import { RequestPendingService } from './request-pending/request-pending.service';
import { WebsocketService } from "./websocket/websocket.service";
import { AuthGuard } from "./auth.guard";
import { TitleService } from './title/title.service';
import { PlayerEditComponent } from './player/player-edit/player-edit.component';
import { MatchListElementComponent } from './match/list/match-list/match-list-element.component';
import { TableImageComponent } from './match/table-image/table-image.component';

const ROUTES: Route[] = [
  { path: '', component: HomepageComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'tables', component: TableListComponent },
  { path: 'matches/:tableCode', component: MatchComponent, pathMatch: 'full', canActivate: [AuthGuard] },
  { path: 'matches/:tableCode/:side/:role', component: MatchComponent, pathMatch: 'full', canActivate: [AuthGuard] },
  { path: 'tables/:tableCode', component: TableEditComponent },
  { path: 'tables/:tableCode/links', component: TableLinksComponent },
  { path: 'tables/:tableCode/:side/:role', redirectTo: 'matches/:tableCode/:side/:role' },
  { path: 'players', component: PlayerListComponent },
  { path: 'players/:id', component: PlayerEditComponent },
  { path: 'players/:id/teams', component: PlayerTeamsComponent },
  { path: 'players/:id/matches', component: PlayerMatchesComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    TableListComponent,
    DeleteDialog,
    RegisterComponent,
    LoginComponent,
    HomepageComponent,
    MatchComponent,
    TableEditComponent,
    PlayerListComponent,
    PlayerTeamsComponent,
    PlayerMatchesComponent,
    TableLinksComponent,
    PlayerEditComponent,
    MatchListElementComponent,
    ImageCropperComponent,
    TableImageComponent
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
    RouterModule.forRoot(ROUTES, { useHash: true })
  ],
  providers: [
    RequestPendingService,
    WebsocketService,
    AuthGuard,
    TitleService,

    { provide: HTTP_INTERCEPTORS, useClass: PendingRequestInterceptor, multi: true },
  ],
  bootstrap: [AppComponent],
  entryComponents: [DeleteDialog]
})
export class AppModule {
}
