import { Component } from '@angular/core';
import { TestAPI } from "./testapi/testapi";

@Component({
  selector: 'app-body',
  imports: [TestAPI],
  templateUrl: './body.html',
  styleUrl: './body.css',
})
export class Body {
    
}
