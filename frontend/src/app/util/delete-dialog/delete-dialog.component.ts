import {MatDialog, MatDialogRef, MatSnackBar} from "@angular/material";
import {Component, Inject, OnInit} from '@angular/core';

@Component({
    selector: 'delete-dialog',
    templateUrl: 'delete-dialog.html',
  })
  export class DeleteDialog {
  
    constructor(public dialogRef: MatDialogRef<DeleteDialog>) {}
  
    onCancelClick() {
      this.dialogRef.close(false);
    }
  
    onDeleteClick() {
      this.dialogRef.close(true);
    }
  }
  