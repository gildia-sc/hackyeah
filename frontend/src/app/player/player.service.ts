import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlayerView } from "./view/player-view.model";
import { PlayerTeamView } from './view/player-team-view.model';
import { PlayerMatchView } from './view/player-match-view.model';

@Injectable({
    providedIn: 'root'
})
export class PlayerViewService {

    private playersUrl = '/api/players/view';

    constructor(private http: HttpClient) { }

    getAllPlayers(): Observable<PlayerView[]> {
        return this.http.get<PlayerView[]>(this.playersUrl);
    }

    getPlayerTeams(id: number): Observable<PlayerTeamView[]> {
        const url = `${this.playersUrl}/${id}/teams`;
        return this.http.get<PlayerTeamView[]>(url);
    }

    getPlayerMatches(id: number): Observable<PlayerMatchView[]> {
        const url = `${this.playersUrl}/${id}/matches`;
        return this.http.get<PlayerMatchView[]>(url);
    }
}
