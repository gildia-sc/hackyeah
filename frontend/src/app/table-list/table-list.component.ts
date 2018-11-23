import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-table-list',
  templateUrl: './table-list.component.html',
  styleUrls: ['./table-list.component.css']
})
export class TableListComponent implements OnInit {
  tables: Table[] = [
    { code: "F16", alphaTeamColor: "0000ff", betaTeamColor: "ff0000"},
    { code: "W15", alphaTeamColor: "0000ff", betaTeamColor: "ff0000"}
  ];

  constructor() { }

  ngOnInit() {
  }

}

export class Table {
  code: string;
  alphaTeamColor: string;
  betaTeamColor: string;
}
