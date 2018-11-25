import { Component, OnInit } from '@angular/core';
import { Match, MatchService } from "../match.service";
import { ActivatedRoute } from "@angular/router";
import { WebsocketService } from "../../websocket/websocket.service";
import { MatSnackBar } from "@angular/material";
import { TitleService } from '../../title/title.service';
import * as moment from 'moment';

@Component({
  selector: 'app-table',
  templateUrl: './match.component.html',
  styleUrls: ['./match.component.css']
})
export class MatchComponent implements OnInit {
  private tableCode: string;
  private startPopupDisplayed = false;

  match: Match;

  timer = 0;

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
      this.matchService.freePosition(this.tableCode, team, position).subscribe()
    }

    if (this.matchStarted && !this.matchEnded) {
      this.matchService.scoreGoal(this.tableCode, team, position).subscribe();
    }
  }

  switchPositions(team: string) {
    if (!this.matchEnded) {
      this.matchService.switchPositions(this.tableCode, team).subscribe()
    }
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.tableCode = params['tableCode']
      this.titleService.changeTitle(`Match ${this.tableCode}`);
      this.subscribeToTableChannel(this.tableCode);
      this.matchService.getMatch(this.tableCode).subscribe(match => {
        if (match) {
          this.match = match;
          this.startTimer();
          let side = params['side'];
          let role = params['role'];
          if(side && role) {
            console.log(`Enter with side ${side} and ${role}`)
            if (!this.matchStarted) {
              this.matchService.takePosition(this.tableCode, side, role).subscribe(() => {
                this.snackBar.open('Position taken', null, {
                  duration: 3000
                });
              })
            } else {
              this.snackBar.open('Match already started, position has not taken', null, {
                duration: 3000
              });
            }
          } else {
            console.log('Enter without side or role')
          }
        }
      });
    })
  }

  subscribeToTableChannel(tableCode: string) {
    this.webSocketService.subscribeToChannel(`${tableCode}`, message => {
      if (message.body) {
        this.match = JSON.parse(message.body) as Match;
        this.startTimer();

        if (this.matchStarted && !this.startPopupDisplayed) {
          this.startPopupDisplayed = true;
          this.snackBar.open('The match has started, good luck and have fun!', null, {
            duration: 5000
          })
        }

        if (this.matchEnded) {
          this.displayDisplayWinner();
        }

      }
    })
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
      winner = 'Alpha'
    } else {
      winner = 'Beta'
    }

    this.snackBar.open(`The match has ended. Team ${winner} has won the match! Final result is ${this.alphaScore} : ${this.betaScore}.`,
      null, {duration: 5000}
    )
      .afterDismissed()
      .subscribe(() => this.resetTable());

  }

  pad(num: number): string {
    return ("0" + num).slice(-2);
  }

  hhmmss(secs: number): string {
    let minutes = Math.floor(secs / 60);
    secs = secs % 60;
    const hours = Math.floor(minutes / 60);
    minutes = minutes % 60;
    return this.pad(hours) + ":" + this.pad(minutes) + ":" + this.pad(secs);
  }
}
