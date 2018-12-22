import { Component, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { Match } from "../../model/match.model";
import { Timer } from "../timer/timer.component";
import { MatSnackBar } from "@angular/material";
import * as moment from "moment";

@Component({
  selector: 'app-match-notifications',
  templateUrl: './match-notifications.component.html',
  styleUrls: ['./match-notifications.component.css']
})
export class MatchNotificationsComponent implements OnChanges {
  @ViewChild("timer") private timer: Timer;
  @Input() match: Match;

  private _startPopupDisplayed = false;

  constructor(private snackBar: MatSnackBar) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['match']) {
      const matchChanges = changes['match'];
      const oldMatch: Match = matchChanges.previousValue;
      const currentMatch: Match = matchChanges.currentValue;

      this.startCountdownTimer(currentMatch);

      if (currentMatch.startTime && !this.timer.started) {
        this.startTimer(currentMatch);
        this._startPopupDisplayed = true;
        setTimeout(() => this.snackBar.open('The match has started, good luck and have fun!', null, {
          duration: 3000,
        }), 10);
        this.playAudio('begin_match');
      } else if (currentMatch.endTime) {
        this.displayWinner(currentMatch);
        this.playAudio('end_match');
        this.timer.stop();
      } else if (oldMatch && currentMatch.alphaScore > oldMatch.alphaScore) {
        this.playAudio('goal_for_alpha');
      } else if (oldMatch && currentMatch.betaScore > oldMatch.betaScore) {
        this.playAudio('goal_for_beta');
      }
    }
  }

  private startCountdownTimer(match: Match) {
    if (!match.startTime && !match.endTime) {
      const reservationStartTime = moment(match.reservationStart);
      const now = moment();
      const elapsed = Math.trunc(now.diff(reservationStartTime) / 1000);
      this.timer.startCountdown(60 - elapsed);
    }
  }

  private startTimer(match: Match) {
    if (match.startTime) {
      const alreadyElapsed = Math.trunc(moment().diff(moment(match.startTime)) / 1000);
      this.timer.stop();
      this.timer.start(alreadyElapsed);
    }
  }

  private playAudio(audioName: string) {
    let audio = new Audio();
    audio.src = `../../../../assets/sound/${audioName}.mp3`;
    audio.load();
    audio.play();
  }

  private displayWinner(match: Match) {
    let winner;

    if (match.alphaScore > match.betaScore) {
      winner = 'Alpha';
    } else {
      winner = 'Beta';
    }

    setTimeout(() => this.snackBar.open(`The match has ended. Team ${winner} has won the match! Final result is ${match.alphaScore} : ${match.betaScore}.`,
      null, {duration: 5000},
    ), 10);
  }
}
