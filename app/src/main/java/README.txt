U položenom stanju, iskoristiti prvi deo za naravoučenije i slično
Tokom OOBE, staviti da imena u meniju budu neokrivena/posebna, tako da skoro garantovano će ići po redu.
    - na primer ime "* [1-9] *"

Buba: šibica na početku treba da se čuje, ali sad ne mogu da nađem link.

Ako se (novi) asset završava brojem, obrisati stare fajlove sa manjim brojem.  Ili pak obrisati sve .mp3 fajlove koji nisu u assets.

-----------------------------------------

Tenor_howto da bude jedan flavor!
    Može li UTF-8 za notni zapis?
    Ili pak slika.
    https://vexflow.com/
Da dodam da glas (tenor, sopran, bas, alt) može da se promeni kroz search kao easter egg, prefix svuda za ime pesme, i onda dodatni download ide sam od sebe.

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
    - osim Recuerdos koja je primetno tiša
La Basurita prevod je pogrešan na nekoliko mesta
Hey od Iglesiasa je preglasan početak

-----------------------------------------

s3cmd setacl --acl-public --recursive s3://mg94c18gonzales

Dodati ћирилицу ako neko traži "a36yka", nemam nigde "injekcije" ili "konjukcije" ili "Bedžihe", pa bi trebalo da može da se prebaci lako.  Ima Tanjug.

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

Provera da li si linkovi i imena dobri:
for f in gonzales dijaspora; do rm -f numbers.$f && for n in $(cat app/src/$f/assets/numbers); do cat app/src/$f/assets/$n | head -n 1 | sed -e 's|.*/||' | sed -e 's/.mp3//' >> numbers.$f; done; done
for f in gonzales dijaspora; do diff numbers.$f app/src/$f/assets/numbers; done

Sve tekstove da propustim kroz neki checker za španski, pogotovu da stavim akcenat za prošlo i buduće vreme.
🎓ako neko treba da uči sam (nema prevod i neće biti)
🕑za "coming soon"
for i in $(cat app/src/dijaspora/assets/numbers); do echo -n $i\ ; echo $(find app/src/dijaspora/ -name $i.bukvalno | wc -l) $(find app/src/dijaspora/assets/ -name $i.finalno | wc -l); done
for v in dijaspora gonzales; do for f in $(for i in $(cat app/src/$v/assets/numbers); do echo -n $i\ ; echo $(find app/src/$v/ -name $i.bukvalno | wc -l) $(find app/src/$v/assets/ -name $i.finalno | wc -l); done | grep 0 | awk '{print $1}'); do cp cs app/src/$v/assets/$f.bukvalno && cp cs app/src/$v/assets/$f.finalno; done; done
for f in $(grep -l https $(find app/src/dijaspora/assets/ -name \*.bukvalno)); do sed -i '' s'|^https://.*||' $f; done
for f in $(grep -l 2024 app/src/dijaspora/assets/*); do sed -I "" -e 's/2024/2025/' $f; done
for f in $(grep -l "N/A" app/src/dijaspora/assets/*.bukvalno | grep -v abvgd); do cp cs $f; done

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
        zatim svrstati que, aquel, cualqiuera, quales, yo, tu, el, ella
        decirte [reći tebi] ali? herirte [povrediti tebe] "a ti seguimos", "junto a ti", "juntos a un cielo azul"
    Que no se apartan de mi -> da se ne odvoje, ili koji se ne odvajaju
    prevod El Rey, posebno "hay qe"
    "it" koncept; que hay de nuevo
    interesas: ti mene više me ne interesuješ, ili pak ti mi više me ne interesuješ
    eras muy feliz (papel) nema smisla
    por que ili porque
    šta je rekla carta?
    ya ves -> "već"? vidiš
    kolko se često piše he->e? hace->ace?, hijo->ijo?, hizo->iso?
    ya -> ma?
    que vs. qué
    "why, you little!" -> potražiti "ma"
    "zar": pojačano "don't you know"

Imena za naglaske
    - ȅ: double grave
    - ê: circumflex
    - è: grave
    - é: acute

A   Ȁ   Â   À   Á
E   Ȅ   Ê   È   É
I   Ȉ   Î   Ì   Í
O   Ȍ   Ô   Ò   Ó
U   Ȕ   Û   Ù   Ú

a   ȁ   â   à   á
e   ȅ   ê   è   é
i   ȉ   î   ì   í
o   ȍ   ô   ò   ó
u   ȕ   û   ù   ú

https://issues.chromium.org/issues/350869464
    - ako izvadim WebView->TextView, onda nestane

    private static void keepScreenOn(@NonNull Activity activity, boolean on) {
        if (on) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

! Za AF: "We will soon take action because your app does not adhere to Google Play Developer Program policies. Fix violations as soon as possible."

"Jugošpanija"?
tu vs. tú

Ako je na speaker, onda sledeća pesma uopšte ne počinje dok se de upali ekran.  Možda MediaPlayer ima svoj lock.

jerinicj@ je u Barseloni

ffmpeg -ss 2490 -i ~/Movies/Volver.mp3 -t 340 ~/Movies/volvera.mp3
Približno OK za dalju ručnu obradu: cat Volver.srt.es | grep -A 100000 "Ábreme, Sole\!" | grep -B 100000 "juntitas"  | grep -vE "[0-9][0-9][0-9]" | tr -d '\r' | tr '\n' '#' | sed -e 's/##/\n\- /g' | tr '#' ' ' > app/src/gonzales/assets/volvera

ffmpeg -ss 3210 -i ~/Movies/Volver.mp3 -t 330 ~/Movies/volverb.mp3
cat Volver.srt.es | grep -A 100000 "00:53:32,612" | grep -B 100000 "no te vayas así"  | grep -vE "[0-9][0-9][0-9]" | tr -d '\r' | tr '\n' '#' | sed -e 's/##/\n\- /g' | tr '#' ' ' > app/src/gonzales/assets/volverb

Još filmova:
Los abrazos rotos (2009)
Yo, también (2009)
Mar adentro (2004) https://www.youtube.com/watch?v=3Ant7vfOaP4

al + infinitivo
Introduce una acción o un acontecimiento que se produce simultáneamente o inmediatamente antes que otro:
    * decidió marcharse al comprender lo que pasaba
    * al abrir los ojos, todo estaba oscuro
    * perdió el equipaje al cambiarse de tren.

ir + gerundio
Indica que la acción que se expresa se está realizando progresivamente:
    * el enfermo va mejorando poco a poco.

"a ras" ili "al ras" u perdi

SRT kaže...
    * 07x08: Si lo sé, te veo. -> Da sam znala, gledala bih te.
    * 07x10: Si hubiese ido con él, como le dije -> Da sam bio s njim, kako sam obećao

https://www.youtube.com/watch?v=SusylI-b7yo
https://www.youtube.com/watch?v=Z5NCrUiIFIk
https://www.youtube.com/watch?v=iZlhTMutExQ
https://www.youtube.com/watch?v=Vt6etwmGRMo
https://www.youtube.com/watch?v=wtpTCWF2fjk
https://www.youtube.com/watch?v=GqJlWKIdBCI
https://www.youtube.com/watch?v=XR_u-DWaK-w

Intervju sa Šakirom pokazuje da uz dovoljno slušanja treba sve da se razume.
Napraviti da nema prevod, nego da ima 1/42, 2/42 itd, ali da pokazuje ljutite face ako neko pokuša da vara.
Treba da +1/42 bude da je neko odslušao sve pesme, i to tako da je razmaknuto vremenski.
Ili pak da pokazuje face koje se polako menjaju u zavisnosti od broja ali ne pokazuje broj.
Ili pak da pokazuje samo broj bez nagoveštaja kad će da se završi

"no puedo evitar" -> dodati nekoliko pesama

"Conjugación Irregular" nalazi dosta stvari u rečniku

https://www.ingles.com/traductor/hechas

Za Špance imamo https://www.youtube.com/watch?v=um6DhjyF5q8 kao prvu umesto ABVGD
