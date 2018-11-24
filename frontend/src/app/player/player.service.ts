import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlayerView } from "./player-view.model";

@Injectable({
    providedIn: 'root'
})
export class PlayerViewService {

    private playersUrl = '/api/players/view';

    constructor(private http: HttpClient) { }

    getAllPlayers(): Observable<PlayerView[]> {
        return this.http.get<PlayerView[]>(this.playersUrl);
    }
}
