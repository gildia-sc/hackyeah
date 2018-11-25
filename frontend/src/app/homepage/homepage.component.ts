import { Component, OnInit } from '@angular/core';
import { TitleService } from '../title/title.service';
import { Table, TablesService } from "../table/tables.service";

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit {
  tables: Table[];

  constructor(private readonly titleService: TitleService,
              private readonly tableService: TablesService) {
  }

  ngOnInit() {
    this.titleService.changeTitle("Homepage");
    this.tableService.getAllTables()
      .subscribe(tables => {
        this.tables = tables
      });
  }

}
