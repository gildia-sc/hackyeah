import { Component, Input, OnDestroy } from '@angular/core';
import { interval, Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";

@Component({
  selector: 'app-timer',
  templateUrl: './timer.component.html',
  styleUrls: ['./timer.component.css']
})
export class TimerComponent implements Timer, OnDestroy {
  @Input() currentTime: number = 0;
  @Input() countdownTickLimit: number = 60;

  private _started = false;
  private _destroyed$ = new Subject();

  constructor() {
  }

  start(elapsedSeconds: number = 0, onCompleteCallback?: () => any) {
    if (!this._started) {
      this.currentTime = elapsedSeconds;
      interval(1000)
        .pipe(
          takeUntil(this._destroyed$)
        )
        .subscribe(
          () => {
            if (elapsedSeconds !== 0 && !this._started) {
              this.currentTime += elapsedSeconds;
              this._started = true;
            }
            this.currentTime++;
          },
          () => null,
          () => {
            if (onCompleteCallback) {
              onCompleteCallback();
            }
          }
        );
    }
  }

  ngOnDestroy(): void {
    this._destroyed$.next();
    this._destroyed$.complete();
  }

  stop() {
    this._started = false;
    this._destroyed$.next();
  }

  restartTimer(onCompleteCallback?: () => any, initialValue?: number) {
    if (initialValue) {
      this.currentTime = initialValue;
    }
    this.start(0, onCompleteCallback);
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

export interface Timer {
  currentTime: number;
  start(elapsedSeconds?: number, onCompleteCallback?: () => any): void;
  stop();
  restartTimer(onCompleteCallback?: () => any, initialValue?: number): void;
}
