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

  constructor(private tablesService: TablesService) { }

  ngOnInit() {
    this.tables = this.tablesService.getAllTables()
  }
}


