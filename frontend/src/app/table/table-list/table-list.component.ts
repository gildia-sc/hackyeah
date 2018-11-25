import {Component, Inject, OnInit} from '@angular/core';
import {Table, TablesService} from "../tables.service";
import {Observable} from "rxjs";
import {MatDialog, MatDialogRef, MatSnackBar} from "@angular/material";
import {Router} from "@angular/router";
import {DeleteDialog} from '../../util/delete-dialog/delete-dialog.component'
import { TitleService } from '../../title/title.service';

@Component({
  selector: 'app-table-list',
  templateUrl: './table-list.component.html',
  styleUrls: ['./table-list.component.css']
})
export class TableListComponent implements OnInit {

  tables: Observable<Table[]>;

  constructor(private readonly tablesService: TablesService,
              private readonly dialog: MatDialog,
              private readonly router: Router,
              private readonly snackBar: MatSnackBar,
              private readonly titleServie: TitleService) { }

  ngOnInit() {
    this.titleServie.changeTitle("Tables");
    this.tables = this.tablesService.getAllTables();
  }

  openDeleteDialog(tableCode: string): void {
    const dialogRef = this.dialog.open(DeleteDialog, {
      width: '250px'
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      if(result) {
        this.tablesService.deleteTable(tableCode).subscribe(() => {
          this.tables = this.tablesService.getAllTables()
        }, () => {
          this.snackBar.open('Update failed', null, {
            duration: 3000
          });
        });
      }
    });
  }
}



