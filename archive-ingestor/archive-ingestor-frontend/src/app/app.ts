import { Component, signal, WritableSignal } from '@angular/core';
import { Header } from './components/header/header';
import { Footer } from "./components/footer/footer";
import { Body } from './components/body/body';

@Component({
  selector: 'app-root',
  imports: [Header, Body, Footer],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title: WritableSignal<string> = signal('Archive Ingestor');
  readonly defaultValue: string = "[Not Available]";
}
