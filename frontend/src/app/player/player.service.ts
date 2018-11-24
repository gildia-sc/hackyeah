import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlayerView } from "./view/player-view.model";
import { PlayerTeamView } from './view/player-team-view.model';
import { PlayerMatchView } from './view/player-match-view.model';
import { Player } from '../model/player.model';

@Injectable({
    providedIn: 'root'
})
export class PlayerService {

    private playersUrl = '/api/players';

    private playersViewUrl = this.playersUrl + '/view';

    constructor(private httpClient: HttpClient) { }

    getAllPlayers(): Observable<PlayerView[]> {
        return this.httpClient.get<PlayerView[]>(this.playersViewUrl);
    }

    getPlayerTeams(id: number): Observable<PlayerTeamView[]> {
        const url = `${this.playersViewUrl}/${id}/teams`;
        return this.httpClient.get<PlayerTeamView[]>(url);
    }

    getPlayerMatches(id: number): Observable<PlayerMatchView[]> {
        const url = `${this.playersViewUrl}/${id}/matches`;
        return this.httpClient.get<PlayerMatchView[]>(url);
    }

    deletePlayer(id: number) {
        return this.httpClient.delete<Player>(`${this.playersUrl}/${id}`)
      }
}
