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

  drawTable(): void {
    const ctx = this.tableCanvas.nativeElement.getContext('2d');
    this.drawBorder(ctx);

    const lineGap = Math.ceil((this.tableWidth - (8 * this.playerWidth)) / 9);

    ctx.fillStyle = this.betaColor;
    this.drawPlayers(ctx, lineGap, lineGap, 1);

    ctx.fillStyle = this.alphaColor;
    this.drawPlayers(ctx, this.tableWidth - lineGap - this.playerWidth, lineGap, -1);
  }

  private drawBorder(ctx: CanvasRenderingContext2D): void {
    ctx.fillStyle = 'black';
    ctx.strokeRect(0, 0, this.tableWidth, this.tableHeight);
  }

  private drawPlayers(ctx: CanvasRenderingContext2D, startX: number, lineGap: number, direction: number): void {
    this.drawLine(ctx, startX, 1);

    let currentX = startX + direction * (lineGap + this.playerWidth);
    this.drawLine(ctx, currentX, 2);

    currentX = startX + direction * (3 * lineGap + 3 * this.playerWidth);
    this.drawLine(ctx, currentX, 5);

    currentX = startX + direction * (5 * lineGap + 5 * this.playerWidth);
    this.drawLine(ctx, currentX, 3);
  }

  private drawLine(ctx: CanvasRenderingContext2D, startX: number, noOfPlayers: number): void {
    for (let i = 0; i < noOfPlayers; i++) {
      this.drawPlayer(ctx, startX, this.tableHeight / (noOfPlayers + 1) * (i + 1) - this.playerHeight / 2);
    }
  }

  private drawPlayer(ctx: CanvasRenderingContext2D, x: number, y: number): void {
    ctx.fillRect(x + 3, y, 4, 4);
    ctx.fillRect(x, y + 6, 10, 2);
    ctx.fillRect(x + 2, y + 8, 6, 6);
    ctx.fillRect(x + 2, y + 14, 2, 6);
    ctx.fillRect(x + 2 + 4, y + 14, 2, 6);
  }
}
