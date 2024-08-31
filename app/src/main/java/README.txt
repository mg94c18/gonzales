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

Free from common mistakes made even by native speakers, correctness can be questioned only by 99.9% percentile of the population.

You pay less than you would for a pie.

These songs are translated correctly, with caveats that poetry and music, like other art, is subjective, and that I am native Serbian and not English.

Buba: šibica na početku treba da se čuje, ali sad ne mogu da nađem link.

Ako se (novi) asset završava brojem, obrisati stare fajlove sa manjim brojem.  Ili pak obrisati sve .mp3 fajlove koji nisu u assets.

-----------------------------------------

Tenor_howto da bude jedan flavor!
    Može li UTF-8 za notni zapis?
    Ili pak slika.
    https://vexflow.com/

-----------------------------------------

Raspored za Španski:
- Volver razgovor oko 5 minuta

Ime aplikacije za španski: A ja ribam ja ribam? (dok ti sereš)
Za srpski ikonica sa dva mrava, Ants are my friends
Ili pak: "Plava riba" kao "Plava riba, kljukana dinastija, svastikin but"
Ili pak: Sajfer?  Bah, zauzeto

Ili pak: "Šifra Em"
Explore the difficult aspects of Serbian language by listening to and deciphering the words from 39 hand-picked and meticulously transcribed musical tracks.
3 modes of operation:
* A configurable playlist for background offline listening, 2+ hours total length
* Read-along while listening (NO karaoke, just study the words)
* Deeper study mode with context-aware word-by-word mappings, and a final translation

Enjoy the playlists for practicing listening while doing chores, even sing when nobody hears you.
Meticulously transcribed, with context-aware word-by-word translations and a final translation for your study.

Luz Casal pesme su preglasne u poređenju sa drugima...
La Basurita prevod je pogrešan na nekoliko mesta
Hey od Iglesiasa je preglasan početak

-----------------------------------------

s3cmd setacl --acl-public --recursive s3://mg94c18gonzales

Dodati ћирилицу ako neko traži "a36yka", nemam nigde "injekcije" ili "konjukcije" ili "Bedžihe", pa bi trebalo da može da se prebaci lako

Da ne koristim strings direktno za UI nego uvek preko assets...

Onaj problem na emulatoru se dešava kad nema internet, onda gnjavi sa download u pozadini, ali ako se prebacim na drugu pesmu onda popuni WebView sa starom pesmom, a nova pesma pak još nije skinuta i tako ide unakrsno.

Izgleda da ne moram da koristim CPU lock, jer na primer na mom telefonu radi i svira.  Treba da ga testiram na duže distance.

requestFocus() passing in your OnAudioFocusChangeListener.
Always call requestFocus() first, proceed only if focus is granted.
Slično tome, ako ima playback a onda zvoni alarm, ne pauzira

Treba dodati media button, za integraciju sa slušalicama
Slično tome treba da pauzira ako neko izvadi slušalice dok muzika svira

Treba da nastavi da svira kad se upali/ugasi Dark mode
Kod playlist da bude "I promise" poruka (ako ima više pesama u listi) koja se menja na nekoliko raznih načina i bez koje ne može da se pusti Play, dok se načini ne potroše

for i in {1..36}; do git mv app/src/dijaspora/assets/$i app/src/dijaspora/assets/$(cat app/src/dijaspora/assets/links | head -n $i | tail -n 1); done
for i in {1..10}; do git mv app/src/gonzales/assets/$i app/src/gonzales/assets/$(cat app/src/gonzales/assets/links | head -n $i | tail -n 1); done

Da li je dobar pattern za reči:
for f in gonzales dijaspora; do for n in $(cat app/src/$f/assets/numbers); do cat app/src/$f/assets/$n | java -cp . WordPatternTest; done; done 2>X

Sve tekstove da propustim kroz neki checker za španski, pogotovu da stavim akcenat za prošlo i buduće vreme.

mp3 fajlovi na Macbook nekad pritisnem pause pa play, a on nastavi malo unazad.  fixed-bit-rate problem?  Mada Android radi kako treba.
Trebalo bi da stavim da sačuva poziciju u onStop tako da može da nastavi kasnije od tog istog mesta.
Da testiram kada download gnjavi a ja rotiram ekran.

Crash ima i kad otključam ekran u landscape položaju, izgleda u onResume()

ffmpeg -ss 5 -i ~/Espanol/SR/zenidba.mp3 zenidba.mp3
ffmpeg -i ~/Espanol/SR/necevatra.mp3 -t 175 necevatra.mp3
ffmpeg -i ~/Espanol/SR/kengurmolitva.mp3 -filter:a "volume=3.5" kengur.mp3
ffmpeg -i BS_Druze.mp3 -filter:a "volume=0.5" druze.mp3

Unakrsna provera da [] stvari nisu protivurečne
Provera da je zaista 1:1 preslikavanje za bukvalno
Ćirilica i "Tanjug" pripaziti

Jaime:
    Que en el perdón a crecido -> Que en el perdón ha crecido
    Que entres o que salgas -> zaista?
    Nadie que me comprenda -> nikog da me sluša ili nikog ko me sluša
    koliko padeža? yo, me, mi, conmigo; "a mi"? još neki?
        zatim svrstati que, aquel, cualqiuera, quales
        decirte [reći tebi] ali? herirte [povrediti tebe] "a ti seguimos", "junto a ti"
    Que no se apartan de mi -> da se ne odvoje, ili koji se ne odvajaju
    prevod El Rey, posebno "hay qe"
    "it" koncept; que hay de nuevo
    interesas: ti mene više me ne interesuješ, ili pak ti mi više me ne interesuješ
    eras muy feliz (papel) nema smisla
    por que ili porque
    šta je rekla carta?
    ya ves -> "već"? vidiš
    kolko se često piše he->e? hace->ace?, hijo->ijo?, hizo->iso?

Čovek za koga ili Čovek za kog
Staviti WordPatternTest da bude unit test
    - bah, Robolectric se žali da nemam android.useAndroidX property...

Kandidati za izbacivanje:
    - Mirno spavaj nano
    - Crni leptiri
    - Te noći kad umrem
    - Pišonja i Žuga u paklu droge

Pomeriti "Druže" malo gore jer nije komplikovana
Dodati "Ne volem" od Balaševića

Lutka: možda starija verzija sa drugačijim glasom i publikom
