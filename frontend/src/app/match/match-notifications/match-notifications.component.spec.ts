import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchNotificationsComponent } from './match-notifications.component';

describe('MatchNotificationsComponent', () => {
  let component: MatchNotificationsComponent;
  let fixture: ComponentFixture<MatchNotificationsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MatchNotificationsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MatchNotificationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
