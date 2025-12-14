import { Component, inject, OnInit, signal } from '@angular/core';
import { ArchiveServerSpecService } from '../../services/archive-server-spec';
import { ArchiveServerSpecData } from '../../models/archive-server-spec-data';
import { catchError } from 'rxjs';

@Component({
  selector: 'app-footer',
  imports: [],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class Footer implements OnInit {
    archiveServerSpecService = inject(ArchiveServerSpecService);
    archiveServerSpecData = signal<ArchiveServerSpecData>({
        archiveIngestorVersion: "[Not Available]", 
        latestOTWArchiveVersion: "[Not Available]"
    });

    ngOnInit(): void {
        this.archiveServerSpecService.getArchiveServerSpecData().pipe(
            catchError((err) => {
                console.log(err);
                throw err;
            })
        ).subscribe((result) => {
            this.archiveServerSpecData.set(result);
        });
    }
}
