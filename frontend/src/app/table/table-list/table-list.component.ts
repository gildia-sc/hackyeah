import { Component, OnInit } from '@angular/core';
import {Table, TablesService} from "../tables.service";
import {Observable} from "rxjs";

@Component({
  selector: 'app-table-list',
  templateUrl: './table-list.component.html',
  styleUrls: ['./table-list.component.css']
})
export class TableListComponent implements OnInit {

  tables: Observable<Table[]>;

  // tables: Table[] = [
  //   { code: "F16", alphaTeamColor: "0000ff", betaTeamColor: "ff0000"},
  //   { code: "W15", alphaTeamColor: "0000ff", betaTeamColor: "ff0000"}
  // ];

  constructor(private tablesService: TablesService) { }

  ngOnInit() {
    this.tables = this.tablesService.getAllTables()
  }
}


