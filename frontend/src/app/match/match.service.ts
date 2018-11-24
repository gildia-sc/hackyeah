import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { Player } from "../model/player.model";

@Injectable({
  providedIn: 'root'
})
export class MatchService {
  private resourceUrl = '/api/matches';

  constructor(private httpClient: HttpClient) {
  }

  takePosition(tableCode: string, team: string, position: string): Observable<Match> {
    return this.httpClient.post<Match>(this.resourceUrl.concat(`/${tableCode}/${team.toUpperCase()}`), null, {
        params: new HttpParams().append('position', position.toUpperCase())});
  }

  getMatch(tableCode: string): Observable<Match> {
    return this.httpClient.get<Match>(this.resourceUrl.concat(`/${tableCode}`));
  }

  scoreGoal(tableCode: string, team: string, position?: string): Observable<Match> {
    return this.httpClient.post<Match>(this.resourceUrl.concat(`/${tableCode}/${team.toUpperCase()}/goal`), null,
      { params: position ? new HttpParams().append('position', position.toUpperCase()) : null });
  }

  switchPositions(tableCode: string, team: string): Observable<Match> {
    return this.httpClient.post<Match>(this.resourceUrl.concat(`/${tableCode}/${team.toUpperCase()}/switch`), null);
  }

  freePosition(tableCode: string, team: string, position: string) {
    return this.httpClient.post<Match>(this.resourceUrl.concat(`/${tableCode}/${team.toUpperCase()}/free`), null, {
      params: new HttpParams().append('position', position.toUpperCase())
    });
  }
}

export class Match {
  id: number;
  tableCode: string;
  alphaAttacker: Player;
  alphaGoalkeeper: Player;
  betaAttacker: Player;
  betaGoalkeeper: Player;
  started: boolean;
  alphaScore: number;
  betaScore: number;
}
