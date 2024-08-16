U položenom stanju, iskoristiti prvi deo za naravoučenije i slično
Tokom OOBE, staviti da imena u meniju budu neokrivena/posebna, tako da skoro garantovano će ići po redu.
    - na primer ime "* [1-9] *"

Ko kaže da ti ne možeš razumeti srpski?  Slušaj ove pesme, treniraj tvoje uvo, i ti ćeš razumeti.  Možda takođe pričati.

Maybe they told you that Serbian is impossible to learn...
But why waste time listening to naysayers when you can spend it wisely listening to music?
Use the world of music to explore difficult concepts that native speakers take for granted but can't bother to explain.
Create playlists to listen in the background, train your ear by listening repeatedly, more than XYZ minutes of music.
Use landscape mode to explore the translation, with literal (wrong) translation available for many songs.
The grammar rules for Serbian, Croatian, Bosnian, Montenegrin are all the same, so you will acquire knowledge relevant for languages spoken by 15 million people in the area.
This app will NOT teach you to speak Serbian, but it is sure to help you on your journey.

^ Bla, bla, bla.  Trebalo bi da ilustrujem slikama a ne rečima.

You pay less than you would for a pie.

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
Severina & dodaj mi jastuče / ostani kod kuće, možda da uzmem verziju bez koncerta

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
13) Jesi li sama večeras: za učenje "li", i njega prevesti kao obrnuti upitnik
5) Kad zamirišu jorgovani (Verb declination for male/female) - podvučeno
11) Medvedova ženidba (Aorist) sa naglaskom na prvom slogu svuda ili pak podvučenim aorist rečima

Ako u pesmi nemam šta da naglasim, možda je suvišna?  Mada, vežbe su OK, pogotovu za playlist.

Ima li negde neželjeni miks u pozadni?  Pozdravi je ima ženski prateći vokal u refrenu, jel to treba?  Dodirni mi koljena ima nešto pri kraju.

Ako se (novi) asset završava brojem, obrisati stare fajlove sa manjim brojem.
Tenor_howto da bude jedan flavor!  Može li UTF-8 za notni zapis?  Ili pak slika.

-----------------------------------------

Raspored za Španski:
- Vale mucho
- No me importa nada (ovo kao da se nastavlja na prethodnu)
- El rey: "Pesma za sva vremena", podvući: svako ir/ar/er za infinitiv, svako as za futur
- La Llorona (kao da se nastavlja na prethodnu)
- Un ano de amor
- Un libro viejo (da se nastavlja na prethodnu)
- Quizas, quizas, quizas
- Ya no me interesas (da se nastavlja na prethodnu)
- boleros cubanos clasicos pogledati, ima oko sat i po

- neki rep u smislu komplikovanije, brže, al to može i kasnije
- Volver razgovor oko 5 minuta

Ime aplikacije za španski: A ja ribam ja ribam? (dok ti sereš)

Luz Casal pesme su preglasne u poređenju sa drugima...
La Basurita prevod je pogrešan na nekoliko mesta

-----------------------------------------

s3cmd setacl --acl-public --recursive s3://mg94c18gonzales

Dodati ћирилицу ako neko traži "a36yka", nemam nigde "injekcije" ili "konjukcije" ili "Bedžihe", pa bi trebalo da može da se prebaci lako

Da ne koristim strings direktno za UI nego uvek preko assets...

Zoom treba da uzme u obzir dužinu najduže linije u toj pesmi, pa da stalni faktor bude broj slova u liniji za vertikalno i horizontalno (dakle dva faktora ukupno).
Sad zapravo ne znam šta sam mislio pod tim ^

Onaj problem na emulatoru se dešava kad nema internet, onda gnjavi sa download u pozadini, ali ako se prebacim na drugu pesmu onda popuni WebView sa starom pesmom, a nova pesma pak još nije skinuta i tako ide unakrsno.

Izgleda da ne moram da koristim CPU lock, jer na primer na mom telefonu radi i svira.  Treba da ga testiram na duže distance.

requestFocus() passing in your OnAudioFocusChangeListener.
Always call requestFocus() first, proceed only if focus is granted.
Slično tome, ako ima playback a onda zvoni alarm, ne pauzira

Treba dodati media button, za integraciju sa slušalicama
Slično tome treba da pauzira ako neko izvadi slušalice dok muzika svira

Treba da nastavi da svira kad se upali/ugasi Dark mode
Kod playlist da bude "I promise" poruka (ako ima više pesama u listi) koja se menja na nekoliko raznih načina i bez koje ne može da se pusti Play, dok se načini ne potroše

Provera da li si linkovi i imena dobri:
for f in gonzales dijaspora; do rm -f numbers.$f && for n in $(cat app/src/$f/assets/numbers); do cat app/src/$f/assets/$n | head -n 1 | sed -e 's|.*/||' | sed -e 's/.mp3//' >> numbers.$f; done; done
for f in gonzales dijaspora; do diff numbers.$f app/src/$f/assets/numbers; done

for i in {1..36}; do git mv app/src/dijaspora/assets/$i app/src/dijaspora/assets/$(cat app/src/dijaspora/assets/links | head -n $i | tail -n 1); done
for i in {1..10}; do git mv app/src/gonzales/assets/$i app/src/gonzales/assets/$(cat app/src/gonzales/assets/links | head -n $i | tail -n 1); done

Sve tekstove da propustim kroz neki checker za španski, pogotovu da stavim akcenat za prošlo i buduće vreme.
