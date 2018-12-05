import { Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';

@Component({
  selector: 'app-table-image',
  templateUrl: './table-image.component.html',
  styleUrls: ['./table-image.component.css']
})
export class TableImageComponent implements OnChanges {
  @Input() tableWidth = 256;
  @Input() tableHeight = 164;
  @Input() playerWidth = 10;
  @Input() playerHeight = 20;
  @Input() alphaColor = '#0000ff';
  @Input() betaColor = '#ff0000';

  @ViewChild('tableCanvas')
  tableCanvas: ElementRef;

  constructor() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.drawTable();
  }

  drawTable() {
    const ctx = this.tableCanvas.nativeElement.getContext('2d');
    const lineGap = Math.ceil((this.tableWidth - (8 * this.playerWidth)) / 9);
    ctx.fillStyle = 'black';
    ctx.strokeRect(0, 0, this.tableWidth, this.tableHeight);
    ctx.fillStyle = this.betaColor;
    this.drawPlayers(ctx, lineGap, lineGap, 1);
    ctx.fillStyle = this.alphaColor;
    this.drawPlayers(ctx, this.tableWidth - lineGap - this.playerWidth, lineGap, -1);
  }

  private drawPlayers(ctx, startX, lineGap, direction) {
    this.drawPlayer(ctx, startX, this.tableHeight / 2 - this.playerHeight / 2);

    let currentX = startX + direction * (lineGap + this.playerWidth);
    this.drawPlayer(ctx, currentX, (this.tableHeight) / 2 - (this.playerHeight / 2) - this.playerHeight);
    this.drawPlayer(ctx, currentX, (this.tableHeight) / 2 - (this.playerHeight / 2) + this.playerHeight);

    currentX = startX + direction * (3 * lineGap + 3 * this.playerWidth);
    this.drawPlayer(ctx, currentX, (this.tableHeight) / 2 - (this.playerHeight / 2) - ((this.playerHeight + 5) * 2));
    this.drawPlayer(ctx, currentX, (this.tableHeight) / 2 - (this.playerHeight / 2) - (this.playerHeight + 5));
    this.drawPlayer(ctx, currentX, (this.tableHeight) / 2 - (this.playerHeight / 2));
    this.drawPlayer(ctx, currentX, (this.tableHeight) / 2 - (this.playerHeight / 2) + (this.playerHeight + 5));
    this.drawPlayer(ctx, currentX, (this.tableHeight) / 2 - (this.playerHeight / 2) + ((this.playerHeight + 5) * 2));

    currentX = startX + direction * (5 * lineGap + 5 * this.playerWidth);
    this.drawPlayer(ctx, currentX, (this.tableHeight) / 2 - (this.playerHeight / 2) - (this.playerHeight + 5));
    this.drawPlayer(ctx, currentX, (this.tableHeight) / 2 - (this.playerHeight / 2));
    this.drawPlayer(ctx, currentX, (this.tableHeight) / 2 - (this.playerHeight / 2) + (this.playerHeight + 5));
  }

  private drawPlayer(ctx, x: number, y: number) {
    ctx.fillRect(x + 3, y, 4, 4);
    ctx.fillRect(x, y + 6, 10, 2);
    ctx.fillRect(x + 2, y + 8, 6, 6);
    ctx.fillRect(x + 2, y + 14, 2, 6);
    ctx.fillRect(x + 2 + 4, y + 14, 2, 6);
  }


}
