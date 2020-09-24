import { Component, OnInit } from '@angular/core';
import { AbstractComponent } from '../common/abstract.component';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent extends AbstractComponent implements OnInit {
  COPY_FIELDS = 1;
  EDIT_FIELDS = 2;

  command = this.COPY_FIELDS;

  start() {
    this.closeModal('#modal-start');
  }

  ngOnInit() {
    this.openModal('#modal-start');
  }
}
