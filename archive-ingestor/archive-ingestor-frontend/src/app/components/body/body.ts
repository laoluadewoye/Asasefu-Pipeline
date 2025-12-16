import { Component, Input } from '@angular/core';
import { Tester } from "./tester/tester";
import { Settings } from "./settings/settings";

@Component({
  selector: 'app-body',
  imports: [Tester, Settings],
  templateUrl: './body.html',
  styleUrl: './body.css',
})
export class Body {
    @Input() recievedDefaultValue!: string;
}
