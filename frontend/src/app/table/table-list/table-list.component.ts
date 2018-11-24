import {Component, Inject, OnInit} from '@angular/core';
import {Table, TablesService} from "../tables.service";
import {Observable} from "rxjs";
import {MatDialog, MatDialogRef, MatSnackBar} from "@angular/material";
import {Router} from "@angular/router";

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
              private readonly snackBar: MatSnackBar) { }

  ngOnInit() {
    this.tables = this.tablesService.getAllTables()
  }

  openDeleteDialog(tableCode: string): void {
    const dialogRef = this.dialog.open(DeleteTableDialog, {
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

@Component({
  selector: 'delete-table-dialog',
  templateUrl: 'delete-table-dialog.html',
})
export class DeleteTableDialog {

  constructor(public dialogRef: MatDialogRef<DeleteTableDialog>) {}

  onCancelClick() {
    this.dialogRef.close(false);
  }

  onDeleteClick() {
    this.dialogRef.close(true);
  }
}


