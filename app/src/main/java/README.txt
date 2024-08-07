U položenom stanju, iskoristiti prvi deo za naravoučenije i slično
Tokom OOBE, staviti da imena u meniju budu neokrivena/posebna, tako da skoro garantovano će ići po redu.
    - na primer ime "* [1-9] *"

Ko kaže da ti ne možeš razumeti srpski?  Slušaj ove pesme, treniraj tvoje uvo, i ti ćeš razumeti.  Možda takođe pričati.
These songs are translated correctly, with caveats that poetry and music, like other art, is subjective, and that I am native Serbian and not English.

Azbuka: prebaciti na ovu staru: https://www.youtube.com/watch?v=jWDlaXrJOI4

Medvedovu ženidbu staviti "Aorist" i dodati svuda akcente na prvom slogu.
Koja je težina/vrednost pesme?
	- broj različitih reči
	- broj istih baznih reči koje se ponavljaju ali u svom drugom obliku
	- ili pak koncept koji se ponavlja ali u drugom obliku

Tozovac možda predugačak: https://www.youtube.com/watch?v=bPKLTSNZc1w
Srce je moje: možda takođe predugačko.
Možda pronaći negde raznorazne pesme od tih izvođača, pa staviti u algoritam da se vidi koja je korisna.

Obojiti reči drugom bojom, ili pak drugim fontom/monospace?  Uglavnom imenice (bold), glagole (italic), možda i zamenice (podvučene).
<p>Tambien <ins>me</ins> <em>dijo</em> un <tt>arriero</tt></p>
Mada, izgleda možda nepotrebno ako postoji bukvalni prevod, videće se šta je šta, a previše pomoći nije dobro za pamćenje.

Treba definivno da može da se izbaci podvlačenje (i to po pesmi) jer je to u originalu, a vratiće se na tu pesmu kasnije.
Mada, ako se vrati kasnije, možda će već uglavnom znati šta treba i vratiće se na kratko.

https://developer.android.com/media/platform/mediaplayer

Raspored za srpski:
1) ABVGD
2) U dva će čistači odneti đubre (Verb forms; endings matter) - podvučeno m,š, i futur,infinitiv
3) Daj, ne pitaj (Endings matter; nouns also change) podvučeni m,š, i dana,dane,dani
4) Zlatibore pitaj Taru () podvučena poslednja slova svaki put kad je Zlatibor i Tara
4a) ? Ove noći nećeš spavati (Make past from present)
13) Jesi li sama večeras: za učenje "li"
14) Luče moje (Da li mirno spavaš), slično
5) Kad zamirišu jorgovani (Verb declination for male/female) - podvučeno
6) Daj, ne pitaj (Aca Lukas)
7) Ljubav je, kad se neko voli
8) Zajedno - podvučeno "mo" svuda
9) Rekla je - negde pri vrhu, za učenje "da", podvući ono "da" kod rekla je da nije važno, i prevod onog "šta da joj kažem" jer tu znači "to".  U bukvalnom insisitrati da "da nije važno" a ne da "nije bilo važno"
10) Kab bi jelen imo krila (Conditionals) sa podvučenim "bi" svuda
11) Medvedova ženidba (Aorist) sa naglaskom na prvom slogu svuda
12) Šta da ti pričam kad sve znaš: https://www.youtube.com/watch?v=vTW7_cg10ws

Neke lakše i kraće pesme bez mnogo učenja ali za vežbu (ima dosta kandidata u Gonzales-SR.txt)
Na kraju:
GRU: Za tebe uvek biću tu
Novak sa originalom i sa prevodom: https://www.youtube.com/watch?v=FR2HyUurVCc
Tjelo Hristovo
Bez prevoda: prednosti i mane grada: https://www.youtube.com/watch?v=zNHbw9vWNTk ali odseći ove ruse na kraju jer nisu Crnogorci
Svađa u studiju: https://www.youtube.com/watch?v=J0Guz3gVDV4
(Sa peharom) Boža zvani Pub.

Pesme sa delom prevoda
- Crni leptir: "nekad sam leteo ... krila mi spržila"
- Pozdravi je pozdravi: "eh da mogu ... bi joj rekle sve"
- Ostariću, neću znati (možda)

Raspored za Španski:
- El Rey: "Pesma za sva vremena", podvući: svako ir/ar/er za infinitiv, svako as za futur
- No me importa nada: "Pesma za sve glagole"
- Recordaras

s3cmd setacl --acl-public --recursive s3://mg94c18gonzales

Dodati ćirilicu ako neko traži "a36yka"

Zoom treba da uzme u obzir dužinu najduže linije u toj pesmi, pa da stalni faktor bude broj slova u liniji za vertikalno i horizontalno (dakle dva faktora ukupno).
Sad zapravo ne znam šta sam mislio pod tim ^

Onaj problem na emulatoru se dešava kad nema internet, onda gnjavi sa download u pozadini, ali ako se prebacim na drugu pesmu onda popuni WebView sa starom pesmom, a nova pesma pak još nije skinuta i tako ide unakrsno.

Treba dodati sledeće:
- integraciju sa dugmetom sa slušalica, tako da može da se pauzira i nastavi
- obaveštenje da može da se skloni (i da samim tim ugasi muziku)
- da može da se pauzira i nastavi bez da se otključa ekran (obaveštenje se nalazi na lock screen)

Takođe nastavlja da svira ako odšetam na neku drugu aplikaciju.
Ovo je diskutabilno, pa treba da dodam neki sistem koji će da uradi to što korisnik hoće u zavisnosti od pattern-a korišćenja.

Izgleda da ne moram da koristim CPU lock, jer na primer na mom telefonu radi i svira.  Treba da ga testiram na duže distance.

Verovatno je bolje da za obično slušanje stavim da svira iz WebView, a da svoj MediaPlayer koristim za playlist.
