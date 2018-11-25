import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TitleService {

  private changeTitleSubject = new Subject<string>();

  titleChanged$ = this.changeTitleSubject.asObservable();

  constructor() { }

  changeTitle(title: string): void {
    this.changeTitleSubject.next(title);
  }
}
