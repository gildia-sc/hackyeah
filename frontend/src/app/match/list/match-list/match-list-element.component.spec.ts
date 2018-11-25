import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchListElementComponent } from './match-list-element.component';

describe('MatchListElementComponent', () => {
  let component: MatchListElementComponent;
  let fixture: ComponentFixture<MatchListElementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MatchListElementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MatchListElementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
