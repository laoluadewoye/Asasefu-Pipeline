import { Component } from '@angular/core';
import { Tester } from "./tester/tester";

@Component({
  selector: 'app-body',
  imports: [Tester],
  templateUrl: './body.html',
  styleUrl: './body.css',
})
export class Body {
    
}
