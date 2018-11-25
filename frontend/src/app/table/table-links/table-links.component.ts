import { Component, OnInit } from '@angular/core';
import {map} from "rxjs/operators";
import {FormBuilder, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {MatSnackBar} from "@angular/material";
import {TablesService} from "../tables.service";
import { TitleService } from '../../title/title.service';

@Component({
  selector: 'app-table-links',
  templateUrl: './table-links.component.html',
  styleUrls: ['./table-links.component.css']
})
export class TableLinksComponent implements OnInit {

  tableLinksForm = this.formBuilder.group({
    code: [{value: '', disabled: true }],
    teamAlphaAttacker: [{value: '', disabled: true }],
    teamAlphaGoalkeeper: [{value: '', disabled: true }],
    teamBetaAttacker: [{value: '', disabled: true }],
    teamBetaGoalkeeper: [{value: '', disabled: true }]
  });

  constructor(private readonly formBuilder: FormBuilder,
              private readonly router: Router,
              private readonly snackBar: MatSnackBar,
              private readonly route: ActivatedRoute,
              private readonly tablesService: TablesService,
              private readonly titleService: TitleService) { }

  ngOnInit() {
    let tableCode = this.route.paramMap.pipe(map(params => params.get('tableCode')));
    tableCode.subscribe(dataWithTableCode => {
      this.titleService.changeTitle(`Links for table ${dataWithTableCode}`);
      if(dataWithTableCode != '#new') {
        this.tablesService.getTableByCode(dataWithTableCode)
          .subscribe(table => {
              this.tableLinksForm.get('code').setValue(dataWithTableCode)
              this.tableLinksForm.get('teamAlphaAttacker').setValue(this.getUrl(dataWithTableCode, 'alpha', 'attacker'))
              this.tableLinksForm.get('teamAlphaGoalkeeper').setValue(this.getUrl(dataWithTableCode, 'alpha', 'goalkeeper'))
              this.tableLinksForm.get('teamBetaAttacker').setValue(this.getUrl(dataWithTableCode, 'beta', 'attacker'))
              this.tableLinksForm.get('teamBetaGoalkeeper').setValue(this.getUrl(dataWithTableCode, 'beta', 'goalkeeper'))
            }
          )
      }
    })
  }

  getUrl(tableCode: string, teamName: string, position: string): string {
    let baseUrl = location.protocol + '//' + location.hostname + (location.port ? ':'+location.port: '');
    return `${baseUrl}/table/${tableCode}/${teamName}/${position}`
  }

}
