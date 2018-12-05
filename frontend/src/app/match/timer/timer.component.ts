import { Component, Input } from '@angular/core';
import * as moment from "moment";
import { interval } from "rxjs";
import { take } from "rxjs/operators";

@Component({
  selector: 'app-timer',
  templateUrl: './timer.component.html',
  styleUrls: ['./timer.component.css']
})
export class TimerComponent implements Timer {
  @Input() currentTime: number;
  @Input() tickLimit: number;

  constructor() {
  }

  startTimer(startTime: moment.Moment, onCompleteCallback?: () => any, isCountdown: boolean = false) {
    interval(1000)
      .pipe(take(this.tickLimit))
      .subscribe(
        elapsed => isCountdown ? this.currentTime-- : this.currentTime = elapsed,
        () => null,
        () => this.restartTimer(onCompleteCallback, true, this.tickLimit)
      );
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

  restartTimer(onCompleteCallback?: () => any, isCountdown: boolean = false, initialValue?: number) {
    if (initialValue) {
      this.currentTime = initialValue;
    }
    this.startTimer(moment(), onCompleteCallback, isCountdown);
  }

}

export interface Timer {
  currentTime: number;
  startTimer(startTime: moment.Moment, onCompleteCallback?: () => any, isCountdown?: boolean): void;
  restartTimer(onCompleteCallback?: () => any, isCountdown?: boolean, initialValue?: number): void;
}
