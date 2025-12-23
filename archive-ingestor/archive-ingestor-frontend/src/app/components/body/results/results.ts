import { Component, inject, input, InputSignal, OnChanges, OnInit, signal, SimpleChanges, WritableSignal } from '@angular/core';
import { ArchiveServerResponseData } from '../../../models/archive-server-response-data';
import { ArchiveStoryData } from '../../../models/archive-story-data';
import { ArchiveChapterData } from '../../../models/archive-chapter-data';
import { ArchiveSessionData } from '../../../models/archive-session-data';
import { ArchiveCompletedSession } from '../../../models/archive-completed-session';
import { ArchiveSessionGetService } from '../../../services/archive-session-get';
import { catchError } from 'rxjs';
import { ArchiveResultUnit } from '../../../models/archive-result-unit';
import { StoryMetadata } from './story-metadata/story-metadata';

@Component({
  selector: 'app-results',
  imports: [StoryMetadata],
  templateUrl: './results.html',
  styleUrl: './results.css',
})
export class Results implements OnInit, OnChanges {
    // General properties
    activeTab: WritableSignal<string> = signal<string>("");
    archiveSessionGetService: ArchiveSessionGetService = inject(ArchiveSessionGetService);

    // Session management properties
    parentCompletedSessionIds: InputSignal<string[]> = input.required<string[]>();
    completedSessionMap: WritableSignal<Map<string, ArchiveCompletedSession>> = signal<Map<string, ArchiveCompletedSession>>(new Map());
    sessionIdToHashMap: Map<string, string> = new Map();

    // Display mangement signals
    unaddressedUpdatedSessions: string[] = [];
    parentDefaultServiceWaitMilli: InputSignal<number> = input.required<number>();
    storyMetadataMap: WritableSignal<Map<string, ArchiveResultUnit>> = signal<Map<string, ArchiveResultUnit>>(new Map());
    chapterMap: WritableSignal<Map<string, ArchiveResultUnit>> = signal<Map<string, ArchiveResultUnit>>(new Map());

    // Test properties
    testResponse: ArchiveServerResponseData = new ArchiveServerResponseData();
    testData: string = `{
        "creationTimestamp":"2025-12-20T16:29:51.572145900Z[UTC]",
        "archiveStoryInfo":{
            "associations":[
                
            ],
            "kudos":4,
            "statusWhen":"2025-12-20",
            "guestKudos":0,
            "language":"English",
            "currentChapters":1,
            "title":"I love (I think) therefore I am",
            "creationHash":"a6b1ad057ae6be1f0d28cc7925dbfea584f9dc3dfa475a37c0f2b93bf472ed97",
            "endNotes":[
                "At first, this was going to be a simple selfcest smut fic, but once that Decartes quote came in, I decided to make it something truly unique and blend philosophy, transsexuality, and uhh, giving head. My favorite part was recurring elements in the story and allegories (something I got from recently listening to Hamilton).",
                "One of the major ones was Virto\\u2019s music being an allegory for wanting to be her true self, as it \\u201cstarted playing\\u201d on her own, even when she \\u201cturned it off.\\u201d She got a lot of curation from friends who also didn\\u2019t fit in or could at least fit in better, like Lalu and Leana, and other small details I liked.",
                "The other major one is the use of one of René Descartes\\u2019s more famous contributions to process her life in a digestible and hopeful way. For me, I found that I can attribute a lot of my personal philosophy to taking a humanities class and being exposed to a bunch of different ideologies, then ending up on channels like Unsolicited Advice and FD Signifier. It\\u2019s like your mind is a car garage, and while you park your car there, you can also fill it with a bunch of other stuff like tools so that if you ever need to fix your car, you have a wide variety of options to choose from instead of being limited to calling the dealership, you know?\\nAs for other neat literary occurrences, I\\u2019ll leave it up to you to interpret. I just loved writing and philosophy, and it\\u2019s played a big role in who I\\u2019ve become. Hope you enjoyed the story! Or the smut lol.",
                ""
            ],
            "bookmarks":1,
            "relationships":[
                
            ],
            "characters":[
                "Virto Hatage (Casual Collection)"
            ],
            "additionalTags":[
                "Autism",
                "Autistic Character",
                "Trans Female Character",
                "egg",
                "Pretransition x Posttransition",
                "Selfcest",
                "Philosophy",
                "Time Travel",
                "Smut",
                "Music",
                "Infodumping",
                "Sensual Infodumping",
                "Genderfuck",
                "Lesbian Sex",
                "Straight Sex",
                "Yuri",
                "Sexual Tension",
                "Descartes - Freeform",
                "Breast Play",
                "Self-Healing",
                "Oral Sex",
                "Smutober"
            ],
            "collections":[
                
            ],
            "ratings":[
                "Explicit"
            ],
            "creationTimestamp":"2025-12-20T16:29:21.888683600Z[UTC]",
            "registeredKudos":[
                "OctoHato",
                "dtohl",
                "Bectest",
                "Lady_Astarte"
            ],
            "unnamedRegisteredKudos":0,
            "categories":[
                "F/F"
            ],
            "publicBookmarks":[
                "Lady_Astarte"
            ],
            "summary":[
                "A younger Virto has a day of enlightenment when her older self comes from the future to give her some advice and encouragement. Non-canon events (well, until I decide it is lol). Please listen to the music in the story because it adds a lot more dimensions to it.",
                "Songs:",
                "1) Hate by Kota the Friend and Statik Selektah\\n2) GOD by Kendrick Lamar\\n3) Anything by SZA\\n4) Ruined Me by Muni Long\\n5) weird girl winter by socks. ft. Jem & Raine Fredericks",
                "",
                "",
                "",
                ""
            ],
            "comments":1,
            "warnings":[
                "No Archive Warnings Apply"
            ],
            "words":5384,
            "published":"2025-10-24",
            "hits":165,
            "startNotes":[
                "Well, uh, there is my smutober 2025 entry lmao. It came to me and it was too good an idea not to do."
            ],
            "series":[
                
            ],
            "fandoms":[
                "Original Work"
            ],
            "totalChapters":1,
            "status":"Null",
            "authors":[
                "olatheii"
            ]
        },
        "archiveChapters":[
            {
                "summary":[
                    
                ],
                "comments":{
                    "pages":{
                        "1":{
                            "Lady_Astarte_Sat_25_Oct_2025_12_59AM_UTC":{
                                "text":[
                                    "This is the exact kind of story that I need more of. Intuitively crafted with raw emotion, and dialogue that just keeps on giving. I could highlight so many incredible moments but I would be here all day. I plan to tell you everything in person anyway. Thank you so much for writing this <3"
                                ],
                                "user":"Lady_Astarte",
                                "posted":"Sat 25 Oct 2025 12:59AM UTC"
                            }
                        }
                    }
                },
                "startNotes":[
                    
                ],
                "pageTitle":"I love (I think) therefore I am - olatheii - Original Work [Archive of Our Own]",
                "creationTimestamp":"2025-12-20T16:29:49.267175900Z[UTC]",
                "chapterTitle":"Chapter 1: I love (I think) therefore I am",
                "paragraphs":[
                    "{Scene Song - Hate by Kota the Friend and Statik Selektah}",
                    "{Scene Song - Hate by Kota the Friend and Statik Selektah}",
                    "High school graduation was a blur. A mere two years ago, everyone went home under the assumption that life would resume in only a couple of weeks. For those two weeks, the entire eastern coast felt like it had come to a standstill. Carnivals weren\\u2019t moving, cars weren\\u2019t flying down the highway, and waking me up at night. The bugs that normally bothered me stopped showing up around my house. ",
                    "But it was fine, because after two weeks, life could resume. Friends could meet again, and I could get out of the house again. I held on to that hope for those two excruciating weeks, having to settle for texts over Instagram to sate my desire for social interaction. But then, we all got the same message from the Plessville school system.",
                    "Two weeks would turn into two months, and two months would turn into our entire senior year being cut from the sands of time. I could say a lot of things about it, but I would rather not. It doesn\\u2019t matter anyway.",
                    "Once I had that diploma in hand, my parents wanted to move back to Baltimore. Be closer to all the government activity, they said to me. I could go to George Washington, get my degree, and go right into the government with a few choice recommendations from their friends. It was easy, so easy to do.",
                    "But it didn\\u2019t feel right. ",
                    "I still think about the night I applied to UNJ Plessville. I could hear my mom getting into another argument with my dad through the floorboards. Sure, they yelled a lot, but she was just yelling because she cared. She wanted the best for me, and I was practically spitting in the face of all the work she had done. But something deep inside me was telling me that taking that yellow brick road would be the worst mistake of my life. At the very least, it would be a very boring choice.",
                    "I couldn\\u2019t tell them that, though, the pain of keeping my thoughts to myself always felt preferable to admitting the simple truth that I didn\\u2019t want to go. I didn\\u2019t want to move away from the little I\\u2019ve built here. The few and only friends I\\u2019ve made. The emotional tendrils that bind me to this small town in the middle of nowhere. ",
                    "only",
                    "They asked how I would pay, and I got a job as a TA and tutor on campus, along with some scholarships to boot. They asked where I would live, and I told them I would figure it out. That entire time, they thought I lucked out on a cheap apartment. The balls on their boy, I\\u2019m sure they were saying to themselves in relief. ",
                    "In reality, I slept in Leana\\u2019s mom\\u2019s house for the early days. It was hard to get an apartment without a solid job, and luckily, they didn\\u2019t ask too many questions. That, or maybe they just already knew. ",
                    "\\u201cIf you have not enjoyed yourselves tonight, you have not been listening\\u2026\\u201d",
                    "I paused my music playlist on my laptop and got out of bed. Nowadays, I have an apartment and a better job somewhere else. It isn\\u2019t much, but it\\u2019s enough to convenience my parents that they don\\u2019t need to fly up to New Jersey to get a dead body.",
                    "{Scene Song - N/A}",
                    "{Scene Song - N/A}",
                    "I found myself listening to Lalu\\u2019s playlists a lot. I found this Kota song through him, and it\\u2019s something about it that makes it really nice for sitting in bed. ",
                    "I think I just miss him. I could probably call him now, but whenever I try, he\\u2019s usually off doing basketball stuff. Eventually, I gave up on trying to reach out to him, but I get this ping in my head thinking about it. It\\u2019s the same thing with Leana, too, but even worse since I literally slept in her house for a while. Sometimes I wonder if I\\u2019m just a bad friend, if I\\u2019m just someone who is incapable of doing anything but filling out a worksheet, but then they call and somehow just know when I\\u2019m free. We would talk for hours and hours, catching up on all the things we missed, seemingly unable to let go of each other. But then, something would have to end the call. Sometimes it\\u2019s other people around them. Sometimes they have an outing planned. Sometimes we just run out of stuff to talk about on the phone. ",
                    "Looking around, I could use more outings. Walking around the house, I saw the pictures of me and my parents on the wall. The washer and dryer are dusty, the bathroom is a mess, and the kitchen is always cluttered with at least five dishes in the sink. It\\u2019s hard to work up the motivation to do much other than work and eat, and my skin feels so dry from not lotioning or doing literally any skin care routine.",
                    "I eventually made it down the steps to my first floor. I stared at the front door longingly, begging it to give me some reason to leave the house today, but it was just an inanimate object. It could do no such thing. ",
                    "\\u2026",
                    "I still think about the night I applied to UNJ Plessville. I thought that by doing that, I would be making my first grown-up decision. No longer would I be at the whims of my parents; I could go out and start my own life. Yet, years later, I feel even more pathetic than then. I am almost done with my degree, but then what? Find some slightly better stuck-up job to waste away in? Who had use for a scrawny kid who was way over his head? Who would want to hire a human search engine when they could just get someone who actually had experience with computers?",
                    "Yet, just when I had lost all hope that the universe would for some reason reward me for doing the bare minimum\\u2026",
                    "{Scene Song - Hate by Kota the Friend and Statik Selektah}",
                    "{Scene Song - Hate by Kota the Friend and Statik Selektah}",
                    "My laptop started playing music again.",
                    "I thought my mind was playing tricks, but the song wouldn\\u2019t stop playing. Eventually, Descartes won out.",
                    "It played, therefore it was.",
                    "I think, therefore I am.",
                    "I decided to go check it out. Maybe it was just Windows being buggy; they have been on a downward spiral lately, so I wouldn\\u2019t be surprised if it was. Assuming a technical error, my guard was completely down, and my eyes were droopier than a dog\\u2019s tongue in summer heat. ",
                    "Yet, when I opened the door, I saw one of the prettiest women that I have ever seen in my life. ",
                    "I froze in my steps as I saw her fiddling with my Tidal playlists, yet the even more shocking part was that she looked uncannily like me. My green hair looked like Derek\\u2019s from Teen Wolf, but she had flowing green waves that hugged her face and shoulders like a Valentine\\u2019s blanket. We had the same eyes, yet hers were adorned with beautiful, beautiful short lashes and flushed with a light dusting of pink powder. My heart restarted itself like an old Chevrolet, no, like someone with really bad stutter trying to pronounce \\u201cquantitative\\u201d when she batted them at me, and spun in my blue spinny chair, the one pleasure purchase I had made for myself up to this point. ",
                    "\\u201cYou have good taste in music,\\u201d she complimented me, pulling her knees to her face and her feet on the edge of my seat. I was affixed by the way her hair bounced around when she nodded to my music, the way her vibrating skin seemed to defy the dreary atmosphere of my near-empty bedroom. \\u201cYou should share it with people, the music I mean.\\u201d",
                    "\\u201cWho\\u2026how\\u2026\\u201d I wanted to say anything, but my speech just stumbled along until I clammed up out of my own befuddlement. I was never able to say the right thing anyway; it felt like I would always be behind in conversations, especially when it was a group where people tossed control of the mic like a hot potato. ",
                    "So I decided to say the first thing that came to my mind.",
                    "\\u201c...I really want your skin.\\u201d",
                    "I\\u2019m a fucking dumbass WHAT THE FUCK AM I DOINGGGGGHHHHH\\u2026",
                    "She didn\\u2019t respond immediately, but she didn\\u2019t need to. I already know that I fucked up. I folded, like Lalu would say. He would know what to do, he would know how I could apologize and start over with this girl-",
                    "\\u201cThat\\u2019s very sweet,\\u201d she responded, smiling as if I didn\\u2019t just sound like a serial killer ten seconds ago. I nodded, seeing an out and taking it like they were military rations. \\u201cI know alot of people would love to have my skin.\\u201d",
                    "\\u201cY-yeah\\u2026\\u201d I said.",
                    "\\u201cMmhmm, like serial killers.\\u201d",
                    "\\u201c......N-no\\u2026\\u201d",
                    "\\u201cBut you give more Menendez brothers than Ted Bundy.\\u201d",
                    "\\u201c....I\\u2026\\u201d",
                    "\\u201cI\\u2019m fucking with you, Virto,\\u201d she laughed. If she hadn\\u2019t just taken me for a rinse and spin cycle, then I would have questioned why she knew my name. But right now, I was just two wrong sentences away from a heart attack. ",
                    "\\u201cAlthough you\\u2019re probably wondering why I\\u2019m here,\\u201d she said, standing up from the seat. Now that she was standing, I couldn\\u2019t help but note how she was as tall as me, aside from the few millimeters I have on her. If I had more hair, then I might even think that-",
                    "\\u201cWell, to put it simply, I\\u2019m you from the future.\\u201d",
                    ". . .",
                    "",
                    "{Scene Song - GOD by Kendrick Lamar}",
                    "{Scene Song - GOD by Kendrick Lamar}",
                    "I don\\u2019t remember how, but I remembered floating. Being carried by a goddess whose body shone like the night sky, whose eyes radiated the light of the celestial nurseries that created our star and solar system. I drank the milk of eternal youth from her immortal bosom, and swooned for her gentle caresses across my Vitamin-deprived cheeks. ",
                    "Bliss would be how I described it, until I was splashed with a squirt of water that made me reflex back into reality.",
                    "\\u201cAre you alright?\\u201d My supposed future self said to me as I stirred back to consciousness. My head rested on soft, pillowy thighs, and my body was covered with a lab coat that I wasn\\u2019t sure was my own.",
                    "Rarely does reality measure up to a dream, but this was one of those times for me. ",
                    "\\u201cI was giving you water while you slept, you just seemed to latch on to my finger when I was checking your breathing,\\u201d she explained, holding up a jogger\\u2019s water bottle with a suction cup top. I blushed intensely, not sure how to explain why I was drinking so much.",
                    "\\u201cSo\\u2026you\\u2019re me, from the future?\\u201d I said, deciding to address the elephant in the room.",
                    "\\u201cDodging concerns of well-being, right\\u2026\\u201d She chuckled to herself. \\u201cI did dodge a lot of shit\\u2026\\u201d",
                    "\\u201cHey, I don\\u2019t-\\u201d I started, but she shot me a look that I didn\\u2019t think I could ever give. I dropped my head down like a puppy, but she lifted it back up with softened eyes. \\u201cJust, be honest,\\u201d she said, leaving it at that. \\u201cIt will save you a lot of pain.\\u201d",
                    "\\u201c...I don\\u2019t think I can\\u2026\\u201d I said with an ambiguous tone.",
                    "\\u201cWe never are and never will be, until we try,\\u201d she advised before allowing me to retreat from her concern. \\u201cBut yes, I am you from the future.\\u201d ",
                    "\\u201cBut\\u2026why come back here of all places?\\u201d I said. \\u201cThere\\u2019s nothing here\\u2026\\u201d",
                    "\\u201cThere\\u2019s everything I need,\\u201d she answered. The way her voice sounded, she made everything sound so easy. It was too the point that I questioned whether she was real once more. ",
                    "For one, she seemed way too put together. She spoke with the wisdom of a sage, and didn\\u2019t seem to mind what I guessed were my undesirable habits. She looked stylish and wore clothes that looked more expensive and authentic than my entire wardrobe from online shopping. She let her chubby waist show from her crop top, where I would feel too ashamed to take off my shirt at the community pool, and I\\u2019m a dude.",
                    "Which brought me to the last thing. She was a girl, a woman. If I said it out loud, it would sound stupid as hell, like I was a kid seeing Disney World for the first time. No woman had ever talked to me for this long, unless it was for studying for a group assignment. Even then, I never invited anyone over, and here she was, telling me that I\\u2019m going to turn out alright, and I can\\u2019t even do the decency of looking her in the eye when she was talking to me and consoling me. Even when I could muster eye contact, I would see her chest, my chest, my chest? In the bottom of my peripheral vision. ",
                    "I awkwardly shuffled around, praying she didn\\u2019t remember the stretching maneuver in her\\u2026erm, our new life as she asked me question after question.",
                    "\\u201cWhat are you thinking of doing after college?\\u201d She asked me. I wondered if she knew how much I hated that question, if she knew how much I didn\\u2019t know, yet\\u2026I felt answers swelling within me that would never see the light of day before. It was like she knew how to wind me up, like I was her doll to play with for her own amusement, her angel to groom while she secretly adorned her wings.",
                    "\\u201cI don\\u2019t know,\\u201d I answered honestly. \\u201cBut I really want to do something with physics. It always fascinated me, but\\u2026it feels stupid to chase after it.\\u201d I gripped the hem of my pants as I continued. \\u201cThere\\u2019 no jobs I can find on LinkedIn, and I can never remember the deadlines for the NASA internships. Sometimes I would just lie to people when they ask. Does\\u2026does that get better?\\u201d ",
                    "My future self glanced away in a way that told me it wouldn\\u2019t. Jesus fuck, I\\u2019m cooked.",
                    "\\u201cWait,\\u201d she interjected, as if she could see my brain starting to squirm. \\u201cYou don\\u2019t really go to NASA, but you do wonderful things, I swear.\\u201d",
                    "\\u201cReally? What do I do?\\u201d",
                    "\\u201cWell\\u2026I can\\u2019t tell you,\\u201d she says in a very sorry tone. \\u201cSince you know, space time continuum and all that.\\u201d",
                    "\\u201cScrew the space time continuum\\u2026\\u201d I said, kicking imaginary dirt against the carpet. \\u201cWhy can\\u2019t we just figure out some way to circumvent that then, huh? Why can\\u2019t we just break the laws of physics, and ruin them so that we can cheat and you wouldn\\u2019t have to give me such vague fucking answers??\\u201d",
                    "\\u201cI\\u2026honestly don\\u2019t know,\\u201d She admitted once more. \\u201cBut for now, I can\\u2019t make the journey you\\u2019re going to take any easier for you. But\\u2026maybe\\u2026seeing me will give you that kick in the ass you\\u2019ve been wanting forever.\\u201d",
                    "\\u201cBut how do I know you\\u2019re even real!?\\u201d",
                    "\\u201cWell, you\\u2019ve been talking-\\u201d",
                    "\\u201cDo you know how long I\\u2019ve been in this house?\\u201d I shouted at her. \\u201cMaybe, maybe I\\u2019m just having a REALLY BAD trip, or maybe there\\u2019s carbon monoxide poisoning, or maybe I hit my head falling down the stairs because I have weak fucking bones, and I\\u2019m actually in the hospital in a coma and my stupid fucking parents are keeping everyone away from the bed! What if, huh? What if???\\u201d",
                    "long",
                    "What if???",
                    "\\u201cWe do have some really\\u2026really dumb parents\\u2026\\u201d she said, rubbing her shoulder. I felt a weird, dastardly concoction of pride, cynicism, evilness, and frustration as I watched her be the one to stumble over her words for a change. It felt good in a horrible way to lash out, yet I was lashing out at the last person that I should. Many more people throughout my life deserve four times the anger I am directing at her, yet she has become my emotional lightning rod. Almost immediately upon the realization, that concoction gave way to regret. ",
                    "\\u201cYeah, we have dumb parents,\\u201d she repeated after a long while of thinking. \\u201cBut I think I could break down why I\\u2019m real into three different aspects.\\u201d",
                    "{Scene Song - Anything by SZA}",
                    "{Scene Song - Anything by SZA}",
                    "\\u201cReally?\\u201d I almost spat out under my breath.",
                    "\\u201cYeah,\\u201d she said. \\u201cThe easiest one is Descartes, because he did the legwork for us many moons ago.\\u201d",
                    "\\u201cI think, therefore I am?\\u201d I quoted, and she nodded. \\u201cYou see, a lot of people know the translated quote, but fewer people know the text around it\\u2026\\u201d It was then that she started to close in on the space I had made between us. I suddenly got nervous, as the reason was that it was harder for her to see how hard I was under my joggers.",
                    "\\u201cAs for me, I really only did as much as look it up on Wikipedia, but I think thinking about it after helped me rationalize why it made sense. Accordingly, seeing that our senses sometimes deceive us, I was willing to suppose that there existed nothing really such as they presented to us\\u2026\\u201d ",
                    "She started quoting Descartes to me while I tried to scoot away from her advance. If she knew how hot she was, I was, using Enlightenment era philosophy to appeal to me why such a goddess like her could exist, then maybe she could prevent my embarrassment. However, I ran out of room, and now she clasped my hand. Our thighs touched against each other, her legs threatening to envelope mine like a game of Agar.io. ",
                    "\\u201cThe first part, by itself, felt like a natural skepticism to me,\\u201d she continued. \\u201cI went through things, Virto, things that caused me to question my own constructs of reality. I thought that by escaping my past, I could finally ascend to my future, but instead I found myself falling even further, and further. The more I tried to escape, the more I fell, until I had reached a point where I felt that I had no more turns to make.\\u201d",
                    "As she explained, she leaned into me. Smelling the fresh, flowery scent of her cherry pink lipstick made my cock throb. ",
                    "\\u201cAnd because some men err in reasoning, and fall into paralogisms, even on the simplest matters of geometry, I, convinced that I was as open to error as any other, rejected as false all the reasonings I had hitherto taken for demonstrations. I essentially couldn\\u2019t figure out what to think anymore. I felt like I was moving on autopilot for the longest time, as I had no strong desire to attach to any sort of belief. Not a belief in god, not a belief in science, as nothing could give me what I truly craved.\\u201d",
                    "\\u201cA-and what was it you truly craved?\\u201d I whimpered.",
                    "\\u201cBeing,\\u201d she answered as succinctly as she could make it. She adjusted her glasses and fuck, why was that so hot? Would it be hot if I did\\u2026",
                    "fuck, why was that so hot? Would it be hot if I did\\u2026",
                    "\\u201cD-do you believe in god now?\\u201d I said, ashamed that I let that thought run through my head. ",
                    "\\u201cNo,\\u201d she answered short and sweet again. \\u201cBut the difference between then and now is that I believe in something. I believe in the worth of something. Other people can put it better than I, though. I\\u2019ve only seen YouTube essays.\\u201d",
                    "\\u201cYou seem really\\u2026smart for YouTube\\u2026\\u201d I was struggling to play it cool, and I was almost certain she knew of my affliction by now. It was impossible to hide. My dick sprang through my pants and stretched the fabric so much that even she, who was almost touching her lenses with mine, could see it in the corner of her vision.",
                    "{Scene Song - Ruined Me by Muni Long}",
                    "{Scene Song - Ruined Me by Muni Long}",
                    "\\u201c...Virto, are you hard?\\u201d She finally asked.",
                    "\\u201c...mmhmm\\u2026\\u201d I told her.",
                    "\\u201c...Does that make you feel\\u2026like your body is against you?\\u201d",
                    "\\u201c...kind of\\u2026\\u201d",
                    "\\u201c...I\\u2019m sorry-\\u201d",
                    "\\u201cNo wait!\\u201d I said, keeping her from moving her face away. \\u201cI\\u2026I like this, though. You are really pretty\\u2026\\u201d",
                    "\\u201cSo are you, younger me,\\u201d she said, her mouth hanging open a bit after she finished talking. As did mine. We just kinda stared into each other\\u2019s eyes for a while, drooling not in lust, but in hyperfixation. It was like we had both reached REM sleep, and our eyes were the ones who couldn\\u2019t sit still while our bodies shut off like statues. \\u201cElaborate,\\u201d she said to me in a way that made my cock throb again. ",
                    "Whatever she was doing, it made my cock not feel like the tool of a man at all. ",
                    "\\u201cI never liked what was attached to masculinity,\\u201d I said. \\u201cI felt\\u2026limited, to say the least. I felt like I could never be myself around anyone, especially my parents. I feel like they pushed a lot onto me, and at the same time denied me of a life I could have experienced. I feel like I learned too early what was expected of me, and because I had no say in the matter, that those feelings only worsened until it felt like it does right now\\u2026\\u201d",
                    "She never stopped staring into my eyes while I was talking. I\\u2019m not sure if I blinked. I\\u2019m not sure if I blinked either.",
                    "\\u201cElaborate,\\u201d she simply said again. She was scratching a different itch this time, which was causing my member to soften, but my brain to sparkle and light on fire. ",
                    "When I think about it, I usually don\\u2019t talk this long. No one asks me these many questions, but now I\\u2019m wondering if I wish they did. I always asked plenty of questions in class, to the point where the teachers got tired of me and called them \\u201cantics.\\u201d I felt\\u2026",
                    "\\u201cDemonized,\\u201d I continued, and I swore steam came off my breath as I spoke. \\u201cI felt limited, to say the least. I felt like I could never be myself around anyone, especially my teachers and classmates. I felt like I was always the only one interested in anything, and that liking anything too much would cause me to lose the few people who liked me. I feel like I have to blend into society, but not in the way like that Joker 2018 movie-\\u201d",
                    "\\u201cI am the last person you have to convince,\\u201d she said comfortably to me, and I nodded maybe one\\u2026hundred times too many. Or maybe I should have nodded a hundred more, nodded until my brain became a slurry mush and she could just hold me while I babbled on and on and-",
                    "\\u201cI don\\u2019t want to blend into society,\\u201d I settled on saying. \\u201cI want to feel like this all the time, where I feel free to be as questioning and curious as I want without feeling like shit for it. Not even my own parents, who supposedly wanted the best for me, who wanted me to be as successful as them, never cared enough to encourage it. They just said that the doctors said I was a\\u2026special kid.\\u201d Saying that last part felt like a special kind of poison, the one that really needed to just die. For a moment, the anger of the gods swirled through my body, and I felt as if my eyes even glowed with a special power, but then our glasses finally touched, and it all melted away.",
                    "Why did she have to understand me so well?",
                    "\\u201cI guess I eventually left all my dreams as\\u2026dreams,\\u201d I resigned, and instantly, she put a hand on my groin. She didn\\u2019t necessarily touch my tip, or did she aim for it, but the ridges that made up my unique fingerprint left a special kind of womb tattoo that I couldn\\u2019t easily forget. ",
                    "After her, I\\u2019ll probably never love again. This kind of heartbreak might never end\\u2026",
                    "\\u201cAnd finally, when I considered that the very same thoughts which we experience when awake may also be experienced when we are asleep, while there is at that time not one of them true, I supposed that all the objects that had ever entered into my mind when awake, had in them no more truth than the illusions of my dreams,\\u201d she moaned, massaging my bushy groin. Before I knew it, she was sitting on my lap, gently rolling her hips and grinding against her fleshy seat. She put my hand, or her hand, on her shirt, as if inviting herself to lift it up, but I wasn\\u2019t even sure I wanted that anymore. Sure, having my face pressed into boobs was nice, and I had never been with a woman before, and my semi-limp member was leaking all over her really nice and probably expensive pants (I am really sorry by the way, I didn\\u2019t mean to do that), but\\u2026",
                    "For once, my mind truly didn\\u2019t have an endless string of words to express how I felt.",
                    "{Scene Song - weird girl winter by socks. ft. Jem & Raine Fredericks}",
                    "{Scene Song - weird girl winter by socks. ft. Jem & Raine Fredericks}",
                    "To fill the gap, perverted thoughts ran through my head, but I didn\\u2019t hesitate.",
                    "\\u201cCan you\\u2026eat me out?\\u201d I asked her. ",
                    "\\u201cHmm?\\u201d I said back. \\u201cYou want me to tonguefuck your hole?\\u201d",
                    "\\u201cNo, I don\\u2019t think I\\u2019ll ever like that\\u2026\\u201d",
                    "\\u201cNever say never me~\\u201d She said, winking towards me. Fuck\\u2026",
                    "\\u201cCould you\\u2026eat out my cock?\\u201d I asked in the least masculine way I could. I didn\\u2019t know how else to say it. I wanted her, I wanted to push my head down onto there but it didn\\u2019t feel like what the movies were implying it felt like. ",
                    "\\u201cPlenty of transwomen also enjoy using their cock, just as plenty find displeasure from it being merely attached to them,\\u201d she explained to me like I was five. \\u201cNo one fits into perfect boxes, and honestly most of them were just made by rich white people so they can get ri\\u2026\\u201d she covered her mouth, batting her eyes as she looked at me. \\u201cSorry, I must be killing the mood for you.\\u201d",
                    "\\u201cNo, that just made it\\u2026hotter.\\u201d I was trying desperately to make sure that I was saying exactly what I meant, but I felt like I was leaving the constraints of what I thought was civilized society. I had no prior experiences, no expectations, no preprogramming for what I was supposed to say or how I should act. All I had were the two heads attached to my body, and I was so, so, so scared of being wrong and ruining something forever.",
                    "She didn\\u2019t seem to care, though. I wasn\\u2019t sure if there was some sort of time paradox where she went through a version of this where she was me, and another us was her, or if I was the first, and maybe we created a branch of reality that only we will ever get to experience. Both were arousing in their own ways, to the point where I wanted to throw up from the sheer amount of butterflies that were turning my guts into New York Times Square.",
                    "When she was sure that there was no awkwardness left to drain from the situation, she dismounted, exposing a big wetspot in both my pants and her pants. She stripped her bottom but left her top on, adjusting her glasses in a way that I couldn\\u2019t miss how her semi-plastic lens settled onto her perfect nose bridge, my future nose bridge.",
                    "I watched as she settled down onto her knees and lifted her top. She stopped just short of letting her breasts drop, wiggling the cotton fabric back and forth as my metapohranical tail wagged in anticipation. Finally, the ball dropped on New Year's Eve, and her perky C-cup breasts fell and settled into a soft rest against her torso. \\u201cDid that make you feel like a man?\\u201d She asked. She used that voice that I used when I felt like starting stupid shit, and I was falling for it hard.",
                    "\\u201cNooooo~\\u201d I droned emphatically. \\u201cI just feel like I\\u2019m in love\\u2026\\u201d",
                    "\\u201cGood,\\u201d I said to myself as I pulled my hardening member into the crevice on my chest. It wasn\\u2019t enveloping it like the hentai I\\u2019ve seen online, but with how much she oogled it like a diamond-encrusted heart it sure felt like it was. \\u201cIt\\u2019s good to love yourself. It gives you the courage and strength to, mmm, keep going\\u2026\\u201d",
                    "\\u201cFuck,\\u201d I moaned for the umpteenth time as she began to move her breasts up and down. Up close and personal, I could see how faded and dead my skin looked compared to hers. ",
                    "I had to fix that as soon as I could. ",
                    "\\u201cIt won\\u2019t take a day,\\u201d she added, seeming to read my mind again. \\u201cYou will have a lot of moments when you feel like-\\u201d",
                    "\\u201cI think I\\u2019m almost there\\u2026\\u201d I announced to her, surprised when she immediately stopped. I whined and pleaded for her to keep going, to not deny me, but-",
                    "\\u201cYou will have a lot of moments when you feel like you\\u2019re about to become who you wanted to be for so long, but then you will have moments that make you feel like you were excited for nothing,\\u201d she said with ravenous eyes. \\u201cTimes when progress would feel negated by strife, and canon events leave you feeling destroyed and devastated.\\u201d",
                    "\\u201cIt is during these times that you must keep going, Virto. You must live,\\u201d she hungrily said. \\u201cDo you promise me that?\\u201d",
                    "\\u201cY-yes\\u2026\\u201d I moaned. I wanted to live so much it was starting to hurt\\u2026\\u201d",
                    "\\u201cDo you promise me that no matter what, you will keep moving forward?\\u201d",
                    "\\u201cYesss, please, I promise\\u2026\\u201d",
                    "\\u201cThat you will always try to open the portal to a world anew? And that once you do, you will go through, and finally emerge into the pretty butterfly that you were?\\u201d She was itching to move, watching my face contort as I struggled against impending bliss. ",
                    "\\u201cYES!!\\u201d I screamed to the heavens. It was then that she started her breast play again with renewed vigor. I held her shoulders as I humped her chest like a feral cat, clawing at her, my hair as what I could only be described as the gentle caress of the light of celestial nurseries descended upon my mortal body. It was inhuman how hard I climaxed, curling over like I had been punched in the gut as I grabbed those beautiful, green, shoulder-length waves and tugged them so hard that I threatened to ruin my future self\\u2019s hair. My goddess took me to the hilt, and seemed to forgive me for my sins mid-pull as she milked my frenulum for all that it had. Even when I let go, and she could breathe once more, she milked my cock with her hand and let it splatter all over her beautiful face, her friendly smile, her rounded cheeks, and her pretty glasses.",
                    "When I opened my eyes again, she was sitting on me like she was before, gently rolling her hips against my groin. She took my hand and held it against her chest, and promised that one day, I could look myself in the mirror and do it to myself.",
                    "\\u201cBut immediately,\\u201d She began once more, \\u201cupon this I observed that, whilst I thus wished to think that all was false, it was absolutely necessary that I, who thus thought, should be something; And as I observed that this truth - I think, therefore I am - was so certain and of such evidence that no ground of doubt, however extravagant, could be alleged by the Sceptics capable of shaking it, I concluded that I might, without scruple, accept it as the first principle of the philosophy of which I was in search.\\u201d And then she adjusted her cum-glazed glasses, catching the excess milk that dripped from it with her tongue, and making a show of rolling it around her mouth before noisily swallowing it and opening her cherry pink lips to show the proof of her conquest. She did it right in front of my face, so my nose could once again smell the fragrance adorning her lips, now mixed with the vulgar smell of my feral thrusts.",
                    "thought",
                    "something",
                    "I think, therefore I am",
                    "however extravagant",
                    "accept it",
                    "Eventually, Descartes won out, and I came inside her.",
                    "I came, therefore I am.",
                    "I love her. Therefore, I love myself.",
                    "I love. Therefore. I am."
                ],
                "creationHash":"ae79e66c8413e18af434b5e8fa282dd7762cb137cea00586407450d75000e11a",
                "endNotes":[
                    
                ],
                "pageLink":"https://archiveofourown.org/works/73057466?view_adult=true"
            }
        ],
        "creationHash":"ae25a2cf46e9500c280b8ca074cb7c5a98afdb41d059826602cd9de0bafdfe20"
    }`;

    async ngOnInit () {
        // Create test response
        this.testResponse.sessionId = "13347b041eabd945f77682f71d3a23af167843b58130bb5fa3db2aab3118d195";
        this.testResponse.sessionNickname = "sample-session";
        this.testResponse.sessionFinished = true;
        this.testResponse.sessionCanceled = false;
        this.testResponse.sessionException = false;
        this.testResponse.parseChaptersCompleted = 1;
        this.testResponse.parseChaptersTotal = 1;
        this.testResponse.responseMessage = "This is an example of what a finished section would look like.";
        this.testResponse.parseResult = this.testData;

        // Add a new test session
        await this.addNewCompletedSession(this.testResponse);

        // Log test session
        console.log(this.completedSessionMap());
    }

    ngOnChanges(changes: SimpleChanges): void {
        console.log("Changes were made to result component: " + changes);
        this.refreshView();
    }

    selectTab(tabOption: string) {
        this.activeTab.set(tabOption);
    }

    formatParseResult(parseResult: string) {
        let parseResultJSON = JSON.parse(parseResult);
        let parseResultFinal;
        if (Object.keys(parseResultJSON).includes("parentArchiveStoryInfo")) {
            parseResultFinal = new ArchiveChapterData(parseResultJSON);
        }
        else if (Object.keys(parseResultJSON).includes("archiveStoryInfo")) {
            parseResultFinal = new ArchiveStoryData(parseResultJSON);
        }

        return parseResultFinal;
    }

    getCrypto() {
        try {
            return window.crypto;
        } catch {
            return crypto;
        }
    }

    async createNewCompletedSession(response: ArchiveServerResponseData) {
        // Create a new completed session
        let newOutcome: string;
        if (response.sessionFinished) {
            newOutcome = "Finished";
        }
        else if (response.sessionCanceled) {
            newOutcome = "Canceled";
        }
        else if (response.sessionException) {
            newOutcome = "Exception";
        }
        else {
            newOutcome = "Unexpected Error: No completion flag set.";
        }

        let newSessionData: ArchiveSessionData = new ArchiveSessionData();
        newSessionData.id = response.sessionId;
        newSessionData.nickname = response.sessionNickname;
        newSessionData.outcome = newOutcome;
        newSessionData.data = this.formatParseResult(response.parseResult);

        if (newSessionData.data === undefined) {
            newSessionData.outcome = newSessionData.outcome + " Unexpected Error: Parse result failed.";
        }

        // Do this funky hashing because node:crypto isn't allowed
        let hashInput = new TextEncoder().encode(
            response.sessionId + 
            response.sessionNickname + 
            newSessionData.outcome + 
            response.parseResult
        );
        let hashBuffer = await this.getCrypto().subtle.digest("SHA-256", hashInput);

        let hashArray = Array.from(new Uint8Array(hashBuffer));
        let hashHex = hashArray.map((b) => b.toString(16).padStart(2, "0")).join("");

        return new ArchiveCompletedSession({hash: btoa(hashHex), data: newSessionData})
    }

    async addNewCompletedSession(response: ArchiveServerResponseData) {
        // Create new completed session
        let newCompletedSession: ArchiveCompletedSession = await this.createNewCompletedSession(response);

        // Check id to hash map. Matching hash means the same data was sent in and no change is needed.
        let newHashMatch: boolean = newCompletedSession.hash === this.sessionIdToHashMap.get(newCompletedSession.data.id);

        // Do more things if the checks pass
        if (!newHashMatch) {
            // Add the session id to the id-hash map
            this.sessionIdToHashMap.set(newCompletedSession.data.id, newCompletedSession.hash);

            // Add the new completed session
            let csm = this.completedSessionMap();
            csm.set(newCompletedSession.hash, newCompletedSession);
            this.completedSessionMap.set(csm);

            // Add session id to sessions to address later
            this.unaddressedUpdatedSessions.push(newCompletedSession.data.id);
        }
    }

    async refreshView() {
        // Empty the array
        this.unaddressedUpdatedSessions = [];

        // Refresh completed session data
        let gsiFinished: boolean[] = Array(this.parentCompletedSessionIds().length).fill(false);
        this.parentCompletedSessionIds().forEach((sessionId: string, index: number) => {
            this.archiveSessionGetService.getSessionInformation(sessionId).pipe(
                catchError((err) => {
                    console.log(err);
                    throw err;
                })
            ).subscribe((result) => {
                this.addNewCompletedSession(result);
                gsiFinished[index] = true;
            });
        });

        // Wait for confirmation response
        let waitingForResponse: boolean = true;
        while (waitingForResponse) {
            await new Promise(resolve => setTimeout(resolve, this.parentDefaultServiceWaitMilli()));
            waitingForResponse = !gsiFinished.every((b) => b === true);
        }

        // Refresh other management signals
        this.unaddressedUpdatedSessions.forEach((sessionId) => {
            // Get hash
            let correlatedHash = this.sessionIdToHashMap.get(sessionId);

            // Get session if possible
            let completedSession: ArchiveCompletedSession | undefined;
            if (correlatedHash !== undefined) {
                completedSession = this.completedSessionMap().get(correlatedHash);
            }
            
            // Isolate story metadata and chapters from information
            let storyOrChapter = completedSession?.data.data;
            let metadata: ArchiveResultUnit;
            let chapters: Array<ArchiveResultUnit> = [];
            if (storyOrChapter instanceof ArchiveStoryData) {
                metadata = new ArchiveResultUnit({
                    id: completedSession?.data.id,
                    nickname: completedSession?.data.nickname,
                    data: storyOrChapter.archiveStoryInfo
                });
                storyOrChapter.archiveChapters.forEach((chapter, index) => {
                    chapters.push(new ArchiveResultUnit({
                        id: `${completedSession?.data.id}_${index}`,
                        nickname: `${completedSession?.data.nickname}-${index}`,
                        data: chapter
                    }));
                });
            }
            else if (storyOrChapter instanceof ArchiveChapterData) {
                metadata = new ArchiveResultUnit({
                    id: completedSession?.data.id,
                    nickname: completedSession?.data.nickname,
                    data: storyOrChapter.parentArchiveStoryInfo
                });
                chapters.push(new ArchiveResultUnit({
                    id: `${completedSession?.data.id}_single`,
                    nickname: `${completedSession?.data.nickname}-single`,
                    data: storyOrChapter
                }));
            }
            else {
                metadata = new ArchiveResultUnit({});
            }

            // Update management maps
            if (storyOrChapter instanceof ArchiveStoryData || storyOrChapter instanceof ArchiveChapterData) {
                let smm = this.storyMetadataMap();
                smm.set(metadata.id, metadata);
                this.storyMetadataMap.set(smm);

                let cm = this.chapterMap();
                chapters.forEach((chapter: ArchiveResultUnit) => cm.set(chapter.id, chapter));
                this.chapterMap.set(cm);
            }
        });
    }
}
