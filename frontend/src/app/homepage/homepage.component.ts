import { Component, OnInit } from '@angular/core';
import { TitleService } from '../title/title.service';

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit {

  constructor(private readonly titleService: TitleService) { }

  ngOnInit() {
    this.titleService.changeTitle("Homepage")
  }

}
