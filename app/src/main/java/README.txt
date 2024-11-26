-----------------------------------------
Search ako tražim "acab", a nađe/ponudi acabo, acaba, acaban i acabado, sve u jednoj pesmi, onda traba sve da bude bold, ili pak da glavna reč bude bold a da ostale budu italic
Za Android, Search ne pamti prethodni search, mada na iOS pamti.
Search u vodoravnom položaju: ako tražim "vino", onda će naći "rujno vino" pio, ali ne i "opilo nas vin|o"
Zapravo će da nađe pesmu, ali neće da stavi u bold.
Pregledati ostale TODO
{} u bukvalnom prevodu?  uglavnom ne bi trebalo
zagrade () gde još ima? uglavnom ne treba nigde u bukvalnom prevodu
Dark Mode:
    nekad je meni beo a nekad crn.  Kao da je beo na cold start a crn kad se Activity ponovo kreira
    takođe, AF se učitava brže; pogledati zašto; search se popunjava u posebnom thread-u
    AF brže učitavanje uglavnom nije zbog dark mode; ako ugasim podršku za to, i dalje se sporije učitava
ovaj, onaj, taj: tri reči u srpskom ali dve u engleskom; još gore ako ima ovom, onom, tom, ovim, onim, tim, ovih, onih, tih, itd

Who?
    ovaj, ovi
    onaj, oni
    taj, ti

    ova, ove
    ona, one
    ta, te

    ovo, ova
    ono, ona
    to, ta

    svaki
    svaka
    svako

    nijedan
    nijedna
    nijedno

    neki, neki
    neka, neke
    neko, neka

    poneki, pokeki
    poneka, poneke
    poneko, poneka

Where?
    ovde
    onde
    tu

How?
    ovako
    onako
    tako

How big?
    koliki, koliki
    kolika, kolike
    koliko, kolika

    ovoliki, ovoliki
    onoliki, onoliki
    toliki, toliki

    ovolika, ovolike
    onolika, onolike
    tolika, tolike

    ovoliko, ovolika
    onoliko, onolika
    toliko, tolika

What like?
    kakav, kakvi
    kakva, kakve
    kakvo, kakva

    ovakav, ovakvi
    onakav, onakvi
    takav, takvi

    ovakva, ovakve
    onakva, onakve
    takva, takve

    ovakvo, ovakva
    onakvo, onakva
    takvo, takva

    nikakav, nikakvi
    nikakva, nikakve
    nikakvo, nikakva

    bilo kakav, bilo kakvi
    bilo kakva, bilo kakve
    bilo kakvo, bilo kakva

    ikakav, ikakvi
    ikakva, ikakve
    ikakvo, ikakva

    svakakav
    svakakva
    svakakvo

    nekakav, nekakvi
    nekakva, nekakve
    nekakvo, nekakva

-----------------------------------------
[(it|he|she) nešto] skloniti ako to nešto je u istoj formi
[setiti ćeš] -> setićeš: videti da pre toga nema spojeni oblik, a da posle toga nema razdvojeni
sam / su -> I have / they have koncept

U položenom stanju, iskoristiti prvi deo za naravoučenije i slično
Tokom OOBE, staviti da imena u meniju budu neokrivena/posebna, tako da skoro garantovano će ići po redu.
    - na primer ime "* [1-9] *"

Buba: šibica na početku treba da se čuje, ali sad ne mogu da nađem link.
Jorgovani sa boljim uvodom: https://www.youtube.com/watch?v=xdcpIcuWiLg
Primer (možda jedini) Lanetovog pisma https://www.youtube.com/watch?v=GH3JWHLsDMs
https://www.youtube.com/watch?v=c7u4v8kfPrQ
Neće vatra: tiše
Pišonja i Žuga da se zamene sa Bubom Erdeljan
Mrtav ladan oko 10 minuta, ceo deo sa vozom

Ako se (novi) asset završava brojem, obrisati stare fajlove sa manjim brojem.  Ili pak obrisati sve .mp3 fajlove koji nisu u assets.

-----------------------------------------

Tenor_howto da bude jedan flavor!
    Može li UTF-8 za notni zapis?
    Ili pak slika.
    https://vexflow.com/
Da dodam da glas (tenor, sopran, bas, alt) može da se promeni kroz search kao easter egg, prefix svuda za ime pesme, i onda dodatni download ide sam od sebe.

-----------------------------------------

s3cmd setacl --acl-public --recursive s3://mg94c18gonzales

Dodati ћирилицу ako neko traži "a36yka", nemam nigde "injekcije" ili "konjukcije" ili "Bedžihe", pa bi trebalo da može da se prebaci lako.  Ima Tanjug.

Da ne koristim strings direktno za UI nego uvek preko assets...

Za ostale tri pesme, najbolje da stavim što više minuta iz filmova jer to podstiče ljude da gledaju sami (na primer sa srpskim prevodom? hm, da li postoji srpski sub-titles? to zvuči glupo)
https://www.youtube.com/watch?v=lUi2xofN4zM

Onaj problem na emulatoru se dešava kad nema internet, onda gnjavi sa download u pozadini, ali ako se prebacim na drugu pesmu onda popuni WebView sa starom pesmom, a nova pesma pak još nije skinuta i tako ide unakrsno.

Izgleda da ne moram da koristim CPU lock, jer na primer na mom telefonu radi i svira.  Treba da ga testiram na duže distance.
Sa jednostavnijim slušalicama više ne radi (u kratkom vremenu kad se završi jedna pesma onda prestane da svira), znači treba CPU lock.
Ako je na speaker (bez slušalica), onda sledeća pesma uopšte ne počinje dok se de upali ekran.  Možda MediaPlayer ima svoj lock.

requestFocus() passing in your OnAudioFocusChangeListener.
Always call requestFocus() first, proceed only if focus is granted.
Slično tome, ako ima playback a onda zvoni alarm, ne pauzira

Treba dodati media button, za integraciju sa slušalicama
Slično tome treba da pauzira ako neko izvadi slušalice dok muzika svira

Kod playlist da bude "I promise" poruka (ako ima više pesama u listi) koja se menja na nekoliko raznih načina i bez koje ne može da se pusti Play, dok se načini ne potroše

for i in {1..36}; do git mv app/src/dijaspora/assets/$i app/src/dijaspora/assets/$(cat app/src/dijaspora/assets/links | head -n $i | tail -n 1); done
for i in {1..10}; do git mv app/src/gonzales/assets/$i app/src/gonzales/assets/$(cat app/src/gonzales/assets/links | head -n $i | tail -n 1); done

Provera da li si linkovi i imena dobri:
for f in gonzales dijaspora; do rm -f numbers.$f && for n in $(cat app/src/$f/assets/numbers); do cat app/src/$f/assets/$n | head -n 1 | sed -e 's|.*/||' | sed -e 's/.mp3//' >> numbers.$f; done; done
for f in gonzales dijaspora; do diff numbers.$f app/src/$f/assets/numbers; done

Za duže linije:
cat app/src/dijaspora/assets/anketa2 | sed -e 's/\.  /.#/g' | tr '#' '\n'
Pa se vratim da vratim?  Ručno je OK
cat app/src/dijaspora/assets/anketa2 | grep -E "^[^\-]"

Za debug:
git apply diff-debug
git diff app/build.gradle app/src/dijaspora/res/values/strings.xml app/src/gonzales/res/values/strings.xml > diff-debug

gs | grep modified | grep assets | awk '{print $2}'

Napad na padeže:
rm assets.all && for a in $(ls app/src/dijaspora/assets/*.bukvalno | grep -vE "leptirko|zajedno|kadodem|mirno"); do cat $a >> assets.all ; done
cat assets.all | grep -E "\[|\]" | sed -e 's/\[/#[/g' | sed -e 's/\]/]#/g' | tr '#' '\n' | grep -E "\[|\]" > groups.all
cat groups.all | wc -l
2721
cat groups.all | tr '[:upper:]' '[:lower:]' | tr -d '"' | grep -vE "\[(they|he|she|we|you|i\ |y'all)" | sort -u | tee groups.hunt.1 | wc -l
810
cat groups.hunt.1 | tr -d ',' | sed -e 's/ .*//' | sort -u | tee groups.hunt.2 | wc -l
187

Da padeži dobiju predlog "po Bogu" "ka Bogu" treba da bude isti padež i predlog
  - nominativ: ko, šta: bez predloga    | Bog   | on    | to   | ja
  - genitiv: od koga, od čega: from     | Boga  | njega | toga | mene
  - dativ: kome, čemu: to               | Bogu  | njemu | tom  | meni
  - akuzativ: koga, šta: bez predloga; mada... možda staviti da bude "for", jer uglavnom su završeci baš takvi?
  - vokativ: bez predloga
  - instrumental: s kim, čim: with      | Bogom | njim  | time | mnom
  - lokativ: o kome, o čemu, gde: isti kao dativ, dok se ne dokaže suprotno
  - 'from' vs. 'of' za dativ...

Kod analize padeža stao kod: diesel

Kasnije:
    self, mnom, nama, ' o '
    one koje imaju zagrade ili male/female, možda treba da budu svuda tako označene
    Zlatibore pitaj Taru - začiniti nekim pomagalicama

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

jerinicj@ je u Barseloni, ili potražiti nekog drugog (za anketu, ili pak za šalu Dice:E,dice:O,dice:A!)

ffmpeg -ss 2490 -i ~/Movies/Volver.mp3 -t 340 ~/Movies/volvera.mp3
Približno OK za dalju ručnu obradu: cat Volver.srt.es | grep -A 100000 "Ábreme, Sole\!" | grep -B 100000 "juntitas"  | grep -vE "[0-9][0-9][0-9]" | tr -d '\r' | tr '\n' '#' | sed -e 's/##/\n\- /g' | tr '#' ' ' > app/src/gonzales/assets/volvera

ffmpeg -ss 3210 -i ~/Movies/Volver.mp3 -t 330 ~/Movies/volverb.mp3
cat Volver.srt.es | grep -A 100000 "00:53:32,612" | grep -B 100000 "no te vayas así"  | grep -vE "[0-9][0-9][0-9]" | tr -d '\r' | tr '\n' '#' | sed -e 's/##/\n\- /g' | tr '#' ' ' > app/src/gonzales/assets/volverb

Još filmova:
Los abrazos rotos (2009)
Yo, también (2009)
Mar adentro (2004) https://www.youtube.com/watch?v=3Ant7vfOaP4
https://www.filmaffinity.com/es/ranking.php?rn=ranking_movies_mx
    - https://www.youtube.com/watch?v=YxkXK742rM0
    - https://www.youtube.com/watch?v=B42LhCN_0WM

Meksički filmovi za koje imam SRT:
    - Viento Negro 1965: https://www.youtube.com/watch?v=jGNKhlDre8A više vestern i malo prevod kasni ili žuri
    - Macario 1960: https://www.youtube.com/watch?v=ThQaEQNDjdQ nije loše ali je tematika možda "pogrešna", a prevod se ponekad naređa jedan na drugi
    - Nosotros los pobres 1948: https://www.youtube.com/watch?v=Eg9DwzJ8LYc drama koja izgleda OK za početak a prevod je OK

al + infinitivo
Introduce una acción o un acontecimiento que se produce simultáneamente o inmediatamente antes que otro:
    * decidió marcharse al comprender lo que pasaba
    * al abrir los ojos, todo estaba oscuro
    * perdió el equipaje al cambiarse de tren.
    * dakle i glagoski prilog prošli i sadašnji

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

"no puedo evitar" -> odslušao nekoliko pesama, nažalost nijedna nije baš melodična

"Conjugación Irregular" nalazi dosta stvari u rečniku

https://www.ingles.com/traductor/hechas
https://www.wordreference.com/conj/esverbs.aspx?v=dar

Bilbao posle Volver: probi uši, treba da se utiša anketa

-----------------------------------------

Za Špance imamo https://www.youtube.com/watch?v=um6DhjyF5q8 kao prvu umesto ABVGD

-----------------------------------------
