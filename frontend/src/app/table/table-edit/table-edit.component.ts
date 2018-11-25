import { Component, OnInit } from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {uniqueValidator} from "../../register/unique-validator";
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute, Router} from "@angular/router";
import {MatSnackBar} from "@angular/material";
import {map} from "rxjs/operators";
import {Observable} from "rxjs";
import {Table, TablesService} from "../tables.service";
import { TitleService } from '../../title/title.service';

@Component({
  selector: 'app-table-edit',
  templateUrl: './table-edit.component.html',
  styleUrls: ['./table-edit.component.css']
})
export class TableEditComponent implements OnInit {

  tableCode: Observable<string>;
  addNew : boolean = false;

  tableForm = this.formBuilder.group({
    code: ['', [Validators.required, Validators.maxLength(50)],
      // [uniqueValidator(this.httpClient, '/api/table-code-taken')]
    ],
    teamAlphaColor: ['', [Validators.required, Validators.pattern('#[0-9a-fA-F]{6}')]],
    teamBetaColor: ['', [Validators.required, Validators.pattern('#[0-9a-fA-F]{6}')]],
  });

  constructor(private readonly formBuilder: FormBuilder,
              private readonly httpClient: HttpClient,
              private readonly router: Router,
              private readonly snackBar: MatSnackBar,
              private readonly route: ActivatedRoute,
              private readonly tablesService: TablesService,
              private readonly titleService: TitleService) { }

  ngOnInit() {
    this.tableCode = this.route.paramMap.pipe(map(params => params.get('tableCode')));
    this.tableCode.subscribe(dataWithTableCode => {
      this.titleService.changeTitle(`Edit table ${dataWithTableCode}`);
      if(dataWithTableCode != '#new') {
        this.tablesService.getTableByCode(dataWithTableCode)
          .subscribe(table => {
              this.tableForm.get('code').setValue(dataWithTableCode)
              this.tableForm.get('teamAlphaColor').setValue(table.teamAlphaColor)
              this.tableForm.get('teamBetaColor').setValue(table.teamBetaColor)
            }
          )
        this.addNew = false;
      } else {
        this.addNew = true;
      }
    })
  }

  isAddNew() {
    return this.addNew;
  }

  submitForm() {
    let table = this.addNew
      ? this.tablesService.insertTable(this.tableForm.value)
      : this.tablesService.updateTable(this.tableForm.value)

    table.subscribe(() => {
      this.router.navigate(['/tables']);
    }, () => {
      this.snackBar.open('Update failed', null, {
        duration: 3000
      });
    });
  }
}
