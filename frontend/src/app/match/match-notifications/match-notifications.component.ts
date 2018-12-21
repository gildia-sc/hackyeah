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
      const currentMatch = matchChanges.currentValue;
      const oldMatch = matchChanges.previousValue;

      this.startTimer(currentMatch);

      if (currentMatch.startTime && this.timer.currentTime == 0) {
        this._startPopupDisplayed = true;
        setTimeout(() => this.snackBar.open('The match has started, good luck and have fun!', null, {
          duration: 3000,
        }), 10);
        this.playAudio('begin_match');
      } else if (currentMatch.endTime) {
        this.displayDisplayWinner(currentMatch);
        this.playAudio('end_match');
        this.timer.stop();
      } else if (oldMatch && currentMatch.alphaScore > oldMatch.alphaScore) {
        this.playAudio('goal_for_alpha');
      } else if (oldMatch && currentMatch.betaScore > oldMatch.betaScore) {
        this.playAudio('goal_for_beta');
      }
    }
  }

  private startTimer(match: Match) {
    if (match.startTime && this.timer.currentTime === 0) {
      const alreadyElapsed = Math.trunc(moment().diff(moment(match.startTime)) / 1000);
      this.timer.start(alreadyElapsed);
    }
  }

  private playAudio(audioName: string) {
    let audio = new Audio();
    audio.src = `../../../../assets/sound/${audioName}.mp3`;
    audio.load();
    audio.play();
  }

  private displayDisplayWinner(match: Match) {
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
