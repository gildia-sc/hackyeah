import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject, interval, Subject } from "rxjs";
import { scan, take, takeUntil } from "rxjs/operators";

@Component({
  selector: 'app-timer',
  templateUrl: './timer.component.html',
  styleUrls: ['./timer.component.css']
})
export class TimerComponent implements Timer, AfterViewInit, OnDestroy {
  private _started = false;
  private _destroyed$ = new Subject();

  currentTime$ = new BehaviorSubject<number>(0);

  constructor() {
  }

  ngOnDestroy(): void {
    this._destroyed$.next();
    this._destroyed$.complete();
  }

  ngAfterViewInit(): void {
    this.currentTime$.next(0);
  }

  get started(): boolean {
    return this._started;
  }

  start(elapsedSeconds: number = 0, onCompleteCallback?: () => any) {
    this._started = true;
    interval(1000)
      .pipe(
        scan(acc => ++acc, elapsedSeconds),
        takeUntil(this._destroyed$)
      )
      .subscribe(
        tick => this.currentTime$.next(tick),
        () => null,
        () => {
          this.currentTime$.next(0);
          if (onCompleteCallback) {
            onCompleteCallback();
          }
        }
      );
  }

  startCountdown(remainingTimeSeconds: number, onComplete?: () => any): void {
    this._destroyed$.next();
    this._started = true;
    interval(1000).pipe(
      take(remainingTimeSeconds),
      scan(acc => acc - 1, remainingTimeSeconds),
      takeUntil(this._destroyed$)
    ).subscribe(
      tick => {
        this.currentTime$.next(tick);
        if (tick === 0 && onComplete) {
          onComplete();
        }
      },
      null,
      () => this._started = false);
  }

  stop(): void {
    this._destroyed$.next();
    this._started = false;
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
  started: boolean;

  start(elapsedSeconds?: number, onCompleteCallback?: () => any): void;

  stop(): void;

  startCountdown(remainingTimeSeconds: number, onComplete?: () => any): void;
}
