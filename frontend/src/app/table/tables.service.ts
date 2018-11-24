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

  getTableByCode(code: string): Observable<Table> {
    return this.httpClient.get<Table>(`/api/tables/${code}`)
  }

  insertTable(table: Table): Observable<Table> {
    return this.httpClient.post<Table>('/api/tables', table)
  }

  updateTable(table: Table): Observable<Table> {
    return this.httpClient.put<Table>(`/api/tables/${table.code}`, table)
  }
}

export class Table {
  code: string;
  teamAlphaColor: string;
  teamBetaColor: string;
}
