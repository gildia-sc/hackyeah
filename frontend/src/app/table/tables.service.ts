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

  updateTable(code: string, table: Table): Observable<Table> {
    table.code = code
    return this.httpClient.put<Table>(`/api/tables/${code}`, table)
  }

  deleteTable(tableCode: string) {
    return this.httpClient.delete<Table>(`/api/tables/${tableCode}`)
  }
}

export class Table {
  code: string;
  teamAlphaColor: string;
  teamBetaColor: string;
}
