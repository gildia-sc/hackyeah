import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PlayerTeamsComponent } from './player-teams.component';

describe('PlayerTeamsComponent', () => {
  let component: PlayerTeamsComponent;
  let fixture: ComponentFixture<PlayerTeamsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PlayerTeamsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlayerTeamsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
