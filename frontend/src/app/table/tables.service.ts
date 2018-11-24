import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class TablesService {

  constructor(private httpClient: HttpClient) { }

  getAllTables(): Observable<Table[]> {
    return this.httpClient.get<Table[]>('/api/tables')
  }
}

export class Table {
  code: string;
  alphaTeamColor: string;
  betaTeamColor: string;
}
