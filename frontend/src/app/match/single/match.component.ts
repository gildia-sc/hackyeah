import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { MatchService } from '../match.service';
import { ActivatedRoute } from '@angular/router';
import { WebsocketService } from '../../websocket/websocket.service';
import { MatSnackBar } from '@angular/material';
import { TitleService } from '../../title/title.service';
import * as moment from 'moment';
import { Match } from '../../model/match.model';

@Component({
  selector: 'app-table',
  templateUrl: './match.component.html',
  styleUrls: ['./match.component.css'],
})
export class MatchComponent implements OnInit {

  readonly tableWidth = 256;
  readonly tableHeight = 164;
  readonly playerWidth = 10;
  readonly playerHeight = 20;

  private tableCode: string;

  private startPopupDisplayed = false;
  match: Match;

  timer = 0;

  @ViewChild('tableCanvas')
  tableCanvas: ElementRef;

  constructor(private matchService: MatchService,
              private route: ActivatedRoute,
              private webSocketService: WebsocketService,
              private snackBar: MatSnackBar,
              private titleService: TitleService) {
  }

  scoreGoal(team: string, position?: string) {
    if (this.matchStarted && !this.matchEnded) {
      this.matchService.scoreGoal(this.tableCode, team, position).subscribe();
    }
  }

  takePosition(team: string, position: string) {
    if (!this.matchStarted) {
      this.matchService.takePosition(this.tableCode, team, position).subscribe();
    }
  }

  freePositionOrScoreGoal(team: string, position: string) {
    if (!this.matchStarted) {
      this.matchService.freePosition(this.tableCode, team, position).subscribe();
    }

    if (this.matchStarted && !this.matchEnded) {
      this.matchService.scoreGoal(this.tableCode, team, position).subscribe();
    }
  }

  switchPositions(team: string) {
    if (!this.matchEnded) {
      this.matchService.switchPositions(this.tableCode, team).subscribe();
    }
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.tableCode = params['tableCode'];
      this.titleService.changeTitle(`Match ${this.tableCode}`);
      this.subscribeToTableChannel(this.tableCode);
      this.matchService.getMatch(this.tableCode).subscribe(match => {
        if (match) {
          this.match = match;
          this.drawTable();
          this.startTimer();
          let side = params['side'];
          let role = params['role'];
          if(side && role) {
            console.log(`Enter with side ${side} and ${role}`);
            if (!this.matchStarted) {
              this.matchService.takePosition(this.tableCode, side, role).subscribe(() => {
                this.snackBar.open('Position taken', null, {
                  duration: 3000,
                });
              });
            } else {
              this.snackBar.open('Match already started, position has not been taken', null, {
                duration: 3000,
              });
            }
          } else {
            console.log('Enter without side or role');
          }
        }
      });
    });
  }

  subscribeToTableChannel(tableCode: string) {
    this.webSocketService.subscribeToChannel(`${tableCode}`, message => {
      if (message.body) {
        let oldMatch : Match = { ...this.match };
        this.match = JSON.parse(message.body) as Match;
        this.startTimer();

        if (this.matchStarted && !this.startPopupDisplayed) {
          this.startPopupDisplayed = true;
          this.snackBar.open('The match has started, good luck and have fun!', null, {
            duration: 5000,
          });
          this.playAudio('begin_match');
        } else if (this.matchEnded) {
          this.displayDisplayWinner();
          this.playAudio('end_match');
        } else if (this.match.alphaScore > oldMatch.alphaScore) {
          this.playAudio('goal_for_alpha');
        } else if (this.match.betaScore > oldMatch.betaScore) {
          this.playAudio('goal_for_beta');
        }
      }
    });
  }

  private playAudio(audioName: string){
    let audio = new Audio();
    audio.src = `../../../assets/sound/${audioName}.mp3`;
    audio.load();
    audio.play();
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

  private startTimer() {
    if (this.matchStarted && this.timer === 0) {
      const startTime = moment();
      const timerCallback = () => {
        const now = moment();
        const secondsElapsed = now.diff(startTime, 'seconds');
        if (this.matchStarted) {
          this.timer = secondsElapsed;
          setTimeout(timerCallback, 1000);
        } else {
          this.timer = 0;
        }
      };
      setTimeout(timerCallback, 1000);
    }
  }

  get alphaScore(): number {
    return this.match != null ? this.match.alphaScore : 0;
  }

  get betaScore(): number {
    return this.match != null ? this.match.betaScore : 0;
  }

  get alphaColor(): string {
    return this.match != null ? this.match.alphaColor : 'blue';
  }

  get betaColor(): string {
    return this.match != null ? this.match.betaColor : 'red';
  }

  get matchStarted() {
    return this.match != null ? this.match.started : false;
  }

  get matchEnded() {
    return this.match != null ? this.match.endTime != null : false;
  }

  private resetTable() {
    this.match = null;
  }

  private displayDisplayWinner() {
    let winner;

    if (this.alphaScore > this.betaScore) {
      winner = 'Alpha';
    } else {
      winner = 'Beta';
    }

    this.snackBar.open(`The match has ended. Team ${winner} has won the match! Final result is ${this.alphaScore} : ${this.betaScore}.`,
      null, {duration: 5000},
    )
      .afterDismissed()
      .subscribe(() => this.resetTable());

  }

  pad(num: number): string {
    return ('0' + num).slice(-2);
  }

  hhmmss(secs: number): string {
    let minutes = Math.floor(secs / 60);
    secs = secs % 60;
    const hours = Math.floor(minutes / 60);
    minutes = minutes % 60;
    return this.pad(hours) + ':' + this.pad(minutes) + ':' + this.pad(secs);
  }
}
