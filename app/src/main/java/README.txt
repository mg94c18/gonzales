-----------------------------------------
Search ako tražim "acab", a nađe/ponudi acabo, acaba, acaban i acabado, sve u jednoj pesmi, onda treba sve da bude bold, ili pak da glavna reč bude bold a da ostale budu italic
Za duže pesme, na primer ove ankete, treba da bude kompletno skraćena verzija cele stvari, tako da na primer ima rečenica gde je ta reč ili slična reč, jedna ispod ili iznad kao kontekst, i tri tačke između.  A da se na primer u tom trenutku pojavi opcija u meniju da može da se ugasi skraćeni prikaz i da se prebaci na kompletni.
Za Android, Search ne pamti prethodni search, mada na iOS pamti.
Search u vodoravnom položaju: ako tražim "vino", onda će naći "rujno vino" pio, ali ne i "opilo nas vin|o"
Zapravo će da nađe pesmu, ali neće da stavi u bold.
Pregledati ostale TODO
{} u bukvalnom prevodu?  uglavnom ne bi trebalo
zagrade () gde još ima? uglavnom ne treba nigde u bukvalnom prevodu
AF se učitava brže; pogledati zašto; search se popunjava u posebnom thread-u; uglavnom nije zbog dark mode; ako ugasim podršku za to, i dalje se sporije učitava
ovaj, onaj, taj: tri reči u srpskom ali dve u engleskom; još gore ako ima ovom, onom, tom, ovim, onim, tim, ovih, onih, tih, itd

spinner se nije ugasio kad sam dodao escucha.mp3 i probao download preko cellular, ali je odradio vrlo brzo preko WiFi
nije se ugasio ni kad sam otišao na drugu pesmu; simulirati sa WiFi sharing ali bez kabla, ili pak preko emulatora

Ako ostavim da svira i koristim start/stop na slušalicama (kod provere, na primer), ekran se uopšte ne gasi sam od sebe.
Ako tražim reč koja se javlja u dužem tekstu (bilo koji od ovih dvosatnih), jako teško se nalazi zbog skrolovanja.
Ako tražim reč vertikalno (jer položeno je jako teško sa tastaturom), nađem je pa rotiram, opet ode na početak a trebalo bi da bude tu negde na istoj poziciji.  Ovo nije problem za pesme ali jeste za duže tekstove (ankete).

"Like that" vs. "that way" za "tako", ili pak "such {female}"
bude, budem -> be {in future}
? self -> myself, itself
is¿ -> true¿
sometime, sometimes; nekad->sometime?
	kod Vas -> near You
	kod Moskve -> near Moskva
	kod njih ima beba -> near [of them]
per->by definitivno, ali tek posle commit
{all female} -> (all female)
	potražiti: {all, female, male, gender
I [I xyz] -> [I xyz], za mislim, sam
[s obzirom da]
[like what] vs. [what like]
[...kind...] -> da bi našao [this kind] vs. [like this]
let me tell you -> lemme tell you, da ne bude microaggression

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

Za pravljenje CD-ova:
for i in $(seq -f "%02g" 1 41); do mv $(cat ../app/src/dijaspora/assets/numbers | head -n $i | tail -n 1).mp3 $i.mp3; done

for f in $(cat ../app/src/gonzales/assets/numbers); do s3cmd get s3://mg94c18gonzales/$f.mp3; done
for i in $(seq -f "%02g" 1 41); do mv $(cat ../app/src/gonzales/assets/numbers | head -n $i | tail -n 1).mp3 $i.mp3; done

Slušač:

Još za učenje:
https://www.youtube.com/@HolaSpanish ali ima dosta koji izgledaju kao da imaju engleski prevod
https://www.youtube.com/@Linguriosa/videos možda bolje jer nema engleski, ali su teme dublje
http://164.92.78.19/cgi-bin/YouTube_Fetch.py?channel_id=

Ovaj ima kvalitetne titles: https://www.youtube.com/@bbcnewsnasrpskom

adb push ~/clones/youtube-dl/P01.mp3 /storage/emulated/0/Android/data/org.mg94c18.slusac.d/cache/B02.mp3
(da bi preimenovanje radilo, samo pogledam da nema nijedan fajl koji ima dva space-a)
Preimenovanje fajlova tako da bude broj-ID.mp4: for i in $(seq -f "%02g" 1 $(ls *.mp4 | wc -l)); do export first=$(ls *.mp4 | grep -vE "^[0-9][0-9]\-" | head -n 1) && echo mv -i \"${first}\" $i-$(echo ${first} | sed -e 's/.*\-//') > move.$i && sh move.$i; done
Umesto ls *.mp4, možda je bolje cat youtube-dl-output.txt
Ili pak za više od 100: for i in $(seq -f "%03g" 1 $(ls *.mp4 | wc -l)); do export first=$(ls *.mp4 | grep -vE "^[0-9][0-9][0-9]\-" | head -n 1) && echo mv -i \"${first}\" $i-$(echo ${first} | sed -e 's/.*\-//') > move.$i && sh move.$i; done
Dodavanje novih u assets: for i in $(seq -f "%02g" 1 49); do for a in dates titles numbers; do echo A$i >> app/src/slusac/assets/$a; done; done
Stavljanje tih u mp3 pa u aplikaciju: for n in $(cat app/src/slusac/assets/numbers | tail -n 49 | tr -d 'A'); do ffmpeg -i ~/clones/youtube-dl/Alo2/$n-*.mp4 A$n.mp3 && adb push A$n.mp3 /storage/emulated/0/Android/data/org.mg94c18.slusac.d/cache/; done

Odslušam sve, izbacim koji je loš zvuk, pa onda za kombinovanje na osnovu trajanja, a sa pamćenjem ID-ova:
export group=Alo2; export prefix=A; rm -f durations.csv && for a in $(cat app/src/slusac/assets/numbers | tail -n 49 | grep -vE "(24|31|02|30|38)" | tr -d ''${prefix}''); do echo -n ${prefix}${a}, >> durations.csv; ffprobe ${prefix}${a}.mp3 2>&1 | grep Duration | awk '{print $2}' | tr -d '\n' >> durations.csv; export id=$(ls ~/clones/youtube-dl/${group}/${a}-*.mp4 | sed -e 's|.*/[0-9][0-9]\-||' | sed -e 's/\.mp4//'); echo ${a}-${id}; cat ~/clones/youtube-dl/${group}/script | grep ${id} | sed -e 's|.*/watch?v=||' | tr -d '"'  >> durations.csv; done
pogledam da li durations.csv izgleda kako treba, popravim ID u .mp4 fajlu ako je suviše mali pa hvata više linkova
Odaberem grupe u durations.csv da bude ukupno oko pola sata, zatim:
Za concat: cat | tr '\n' '#' | sed -e 's/#/.mp3|/g'
Za orig IDs: cat | tr '\n' ','
    A: ffmpeg -i "concat:anketa2.mp3|A01.mp3|A04.mp3|A05.mp3|A06.mp3|A07.mp3|A08.mp3|A09.mp3|A10.mp3|A11.mp3|A48.mp3" -acodec copy anketaa2.mp3
    A: zNHbw9vWNTk, oB3Or31Topk, 8w6zJt6pf0E, OBVC-oqUrZo, x4Ty_h9iiwA, ngj2IJWH1ZE, TTwvfRKsLuU, WXUdSE3sLcA, H4EoVRbvS5g, zLCxPeCTH-4, Y2yvxWjB8eU
    B: ffmpeg -i "concat:A12.mp3|A13.mp3|A14.mp3|A15.mp3|A16.mp3|A17.mp3|A18.mp3|A19.mp3|A20.mp3|A21.mp3|A22.mp3|A23.mp3" -acodec copy anketab.mp3
    B: mvIVCDZN3NM, b5EIZ9AwGtM, C2ASXg1-vag, Mf6ro62GHY4, 0ZoNCyQmQzs, MjuHyV554IY, snRC42SLmAs, sBN17xbp-qY, Vf1HpUGGGZ4, DueeNx4Jd1E, 72vENpqf_C4, Cn9uJbhLWJY
    V: ffmpeg -i "concat:A25.mp3|A26.mp3|A27.mp3|A28.mp3|A29.mp3|A32.mp3|A33.mp3|A34.mp3|A35.mp3|A36.mp3|A37.mp3" -acodec copy anketav.mp3
    V: 0bqbiKcJ_Bc, l_CrBwn5HkQ, XvKR9YZruVM, HtDstEp3kMY, qBYzdyJp3DI, 3u9VZGR_u3M, phCXKOHrO4U, GdRcXBx6geM, OSCIoEU-zxI, NGnkEMjARek, Zi4ufMTFgtg
    G: ffmpeg -i "concat:A39.mp3|A40.mp3|A41.mp3|A42.mp3|A43.mp3|A44.mp3|A45.mp3|A46.mp3|A47.mp3|A49.mp3" -acodec copy anketag2.mp3
    G: Rmw9cVW6fsQ, gp9KbzouPc0, LaxDolG7Rxg, IuoOhHa8Ds0, evy-W-fByj8, zsFeRjp7CyM, dkpcxMGvdY8, h2GWB4ndExs, j8Zh1Vp7mmQ, piiouEVXi3A
for i in a b v g; do s3cmd put anketa${i}.mp3 s3://mg94c18dijaspora/ && s3cmd setacl --acl-public s3://mg94c18dijaspora/anketa${i}.mp3; done

-----------------------------------------
s3cmd setacl --acl-public --recursive s3://mg94c18gonzales

Za ostale tri pesme, najbolje da stavim što više minuta iz filmova jer to podstiče ljude da gledaju sami (na primer sa srpskim prevodom? hm, da li postoji srpski sub-titles? to zvuči glupo)
https://www.youtube.com/watch?v=lUi2xofN4zM

requestFocus() passing in your OnAudioFocusChangeListener.
Always call requestFocus() first, proceed only if focus is granted.
Slično tome, ako ima playback a onda zvoni alarm, ne pauzira

for i in {1..36}; do git mv app/src/dijaspora/assets/$i app/src/dijaspora/assets/$(cat app/src/dijaspora/assets/links | head -n $i | tail -n 1); done
for i in {1..10}; do git mv app/src/gonzales/assets/$i app/src/gonzales/assets/$(cat app/src/gonzales/assets/links | head -n $i | tail -n 1); done

Provera da li si linkovi i imena dobri:
for f in gonzales dijaspora; do rm -f numbers.$f && for n in $(cat app/src/$f/assets/numbers); do cat app/src/$f/assets/$n | head -n 1 | sed -e 's|.*/||' | sed -e 's/.mp3//' >> numbers.$f; done; done
for f in gonzales dijaspora; do diff numbers.$f app/src/$f/assets/numbers; done

Za duže linije:
cat app/src/dijaspora/assets/anketa2 | sed -e 's/\.  /.#/g' | tr '#' '\n'
Pa se vratim da vratim?  Ručno je OK
cat app/src/dijaspora/assets/anketa2 | grep -E "^[^\-]"

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
  - genitiv: od koga, od čega: of       | Boga  | njega | toga | mene
  - dativ: kome, čemu: to               | Bogu  | njemu | tom  | meni
  - akuzativ: koga, šta: bez predloga; mada... možda staviti da bude onto/into/for, jer uglavnom su završeci baš takvi?
  - vokativ: bez predloga (hey, )
  - instrumental: s kim, čim: with      | Bogom | njim  | time | mnom
  - lokativ: o kome, o čemu, gde: isti kao dativ, dok se ne dokaže suprotno
  - 'from' vs. 'of' za genitiv... of je bolje jer from uvek ima predlog
  - in vs. into: potražiti "into [" ili pak "in [^\[]"

Kod analize padeža stao kod: zapravo zarvšio :)
Spell check takođe završio
Oko 70 promenjenih fajlova

Kasnije:
    self, mnom, nama, ' o ', which, what
    one koje imaju zagrade ili male/female, možda treba da budu svuda tako označene
    Zlatibore pitaj Taru - začiniti lekcijom za padeže, gde se uvode svi predlozi from/to/"for"/with; plural... less important
    Kad zamirišu jorgovani - slično ubaciti neke pomagalice na primer "kakav stvarno ti si", "znaš u kakvoj smo akciji", "u ovakvom finalu"
    tod [long tailed] -> [for tod] [for long tailed] i još mnogo drugih propuštenih, ali ne pri početku dok se ne uvedu padeži
    tako gde su duže linije (anketa2, novak, zenidba) "Find in Files" ne prikazuje sve rezultate; potražiti u tim fajlovima ručno preko regex koji obuhvata sve one u [
    koljena vs. kolena provera
    {(male|female)} da bude u običnim zagradama

"ni" je prilično tricky:
    - nije ništa bolje očekivala                didn't expect any better
    - nije ništa bolje ni očekivala             ?
    - i onako nije ništa bolje ni očekivala     didn't expect any better anyway
    - nemam kuda da bežim                       I don't have anywhere to escape to
    - nemam kuda ni da bežim                    ?
    - čak nemam kuda ni da bežim                I don't even have anywhere to escape to
    - ne znaju bližnji                          close ones don't know
    - ne znaju ni bližnji                       ?
    - ne znaju čak ni bližnji                   not even close ones know

Slično kao napad na padeže, ali za španski:
Da svaki glagol ima italic za nastavke po licima i vremenima:
    prezent: o, as, a, emos, amos, an, eis,
    perfekat: ia, aba, ido, ado, ó, é, imos, iste,
    futur: ré, rás, remos, rán,
    uslovni: ría, rías

Sve tekstove da propustim kroz neki checker za španski, pogotovu da stavim akcenat za prošlo i buduće vreme.
    na primer, ako idem ručno, mogu da koristim Pages na Mac, evo vidim da podvlači čak i stvari tipa tu->tú
    mada, ne buni se ako promenim que->qué

https://www.reddit.com/r/learnspanish/comments/yiybd9/se_puede_usar_que_como_un_taquigrafia_de_porque/?rdt=38999
Su equivalente en inglés es: absolutamente nada.
"Su regresas" -> sebe vraćaš ili svoj vraćaš?

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
ffmpeg -i "concat:volvera.mp3|silence-1s.mp3|volverb.mp3" -acodec copy volverc.mp3 && ffmpeg -i volverc.mp3 -filter:a "volume=3.5" volver.mp3

Kengur++
    00:19:57-00:20:17   (00:20)
    00:29:06-00:29:55   (00:49)
    00:30:24-00:30:58   (00:35)

Uslovne: https://www.youtube.com/watch?v=sqm3HHxDWl0 Eh, da sam malo mlađi

Studio++
1: trenutni
2: ffmpeg -ss 58.75 -i studio2.mp3 -to 58 studio2g.mp3
3: ffmpeg -ss 26.75 -i studio3.mp3 -t 174 studio3g.mp3
4: ceo studio4.mp3 ali glasniji i odsecanje ostatka: ffmpeg -i studio4.mp3 -filter:a "volume=2.0" -t 43 studio4g.mp3
5: ceo studio5.mp3 ali sa 44100: ffmpeg -i studio5.mp3 -ar 44100 studio5g.mp3

Za ćirilicu treba da prevodi nemaju originalna imena tipa "Copa vacia" nego napisana po Vuku, tako može prevod da bude automatski
Verovatno će pesme gde se to javlja da se same jave prilikom prevoda, pa mogu njih detaljnije da pregledam

Unakrsna provera da [] stvari nisu protivurečne
Potražiti ' a]' da bu 'a' otišao sa imenicom u akuzativ ili dativ

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
cat Nazarin-8132428.srt | tr -d '\r' | grep -vE "^[0-9][0-9]?[0-9]?" | tr '\n' '#' | sed -e 's/##/\n\- /g' | tr '#' ' '| sed -e 's|<i>||g' | sed -e 's|</i>||g' > Nazarin.srt
Stavim ovo u fajl 'script':
cat "${1}" | tr -d '\r' | grep -vE "^[0-9][0-9]?[0-9]?" | tr '\n' '#' | sed -e 's/##/\n\- /g' | tr '#' ' '| sed -e 's|<i>||g' | sed -e 's|</i>||g' >> "${2}"
Zatim obradim Seranove SRT preko njega:
find ~/SRT/Seranovi/ -name \*.srt -print0 | xargs -0 -n 1 -I % /bin/bash script % %.txt

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
Nazarin:
    00:34:00 - 00:49:10
Macario: 00:23:20 - 00:37:20
Jedan ANR kod Makaria prilikom promene prevoda u horizontalnom položaju

konferencija za štampu: https://www.youtube.com/watch?v=STwb9uY7DzU
bez editovanja i efekata ali samo tri osobe: https://www.youtube.com/watch?v=iHFLp6J_vmk
ulična anketa ali sa dosta editovanja: https://www.youtube.com/watch?v=BAF7zNLtFfA

Manje editovanja su OK:
https://www.youtube.com/watch?v=RqBNeewzoZU
https://www.youtube.com/watch?v=TwwwHk7spas
https://www.youtube.com/watch?v=uYo-8kkoB58
https://www.youtube.com/watch?v=FM3zEzZIEjw
https://www.youtube.com/watch?v=V1A-2KNqLUo
https://www.youtube.com/watch?v=3q_UwHszP7M

cat Linkovi-Primarios.txt | sed -e 's|^|../youtube-dl -f best --no-check-certificate "|' | sed -e 's|$|"|' > script
ls ~/clones/youtube-dl/Primarios/??-*.txt | sed -e 's|.*/||' | sed -e 's/\-.*//' > app/src/slusac/assets/numbers
for a in titles dates; do cp app/src/slusac/assets/numbers app/src/slusac/assets/$a; done
for n in $(cat app/src/slusac/assets/numbers ); do echo > app/src/slusac/assets/$n && echo >> app/src/slusac/assets/$n && cat ~/clones/youtube-dl/Primarios/$n-*.txt >> app/src/slusac/assets/$n; done
for n in $(cat app/src/slusac/assets/numbers ); do ffmpeg -i ~/clones/youtube-dl/Primarios/$n-*.mp4 $n.mp3 && adb push $n.mp3 /storage/emulated/0/Android/data/org.mg94c18.slusac.d/cache/; done

Plan:
    De primaria - universitarios: 03-Universitarios1.txt, 06-Universitarios2.txt, 10-Universitarios3.txt (sve tri obrađene prvi put)
    De primaria - calle: 01-Calle1.txt, 02-Calle2.txt (obe obrađene prvi put)
    De primaria - futuros: 08-Profesores.txt (obrađeno prvi put), 14-Medicos.txt (obrađeno drugi put)
ffmpeg -i "concat:03.mp3|silence-1s.mp3|06.mp3|silence-1s.mp3|10.mp3" -acodec copy universitarios.mp3
ffmpeg -i "concat:01.mp3|silence-1s.mp3|02.mp3" -acodec copy calle.mp3
ffmpeg -i "concat:08.mp3|silence-1s.mp3|14.mp3" -acodec copy futuros.mp3

Meksiko: https://www.youtube.com/@TVGuanajuatoOrgullosamenteTuyo/search?query=sondeo (dosta politike, ali ima i dosta korisnih)
Plan:
    ima 5 komada za "transporte público"
    za dan nečega ima 7 komada: "Día del Niño", "Día de las Madres", "Día del amor", "Día de las Flores", "Día de las Madres", "Día de las Flores", "Día del Padre"
    za potrošačku korpu ima čak 11 komada, da nije neka politička propaganda?
    za pitanja o vodi ima 4 komada, može se dodati salud
    lo mejor y lo peor de su semana, ima 3 komada, može se dodati "por el partido o por la persona", "son los propósitos de año nuevo", "vasectomía" ili ostali da se popuni do 20-30 minuta
    Ciudadanos - transporte público
    Ciudadanos - día de...
    Ciudadanos - lo mejor y lo peor
Od dia, četiri su dovoljna
    hHdLL5aOccI->DD-01.mp3, xtki0uJc6po->DD-02.mp3, 1PM96-jqUvE->DD-03.mp3, IBHmenxTbps->DD-04.mp3
    ffmpeg -i "concat:DD-01.mp3|silence-1s.mp3|DD-02.mp3|silence-1s.mp3|DD-03.mp3|silence-1s.mp3|DD-04.mp3" -acodec copy diade.mp3
    IBHmenxTbps ima početak i kraj obrnut
Od transporte publico, već prvi ima više od 10 minuta, dodao samo još jedan pa ima više od 20.
    YbdxfNYVDCo->TP-01.mp3, GImpYPS6uK0->TP-02.mp3
    ffmpeg -i "concat:TP-01.mp3|silence-1s.mp3|TP-02.mp3" -acodec copy transporte.mp3
Od mejor/peor, ima tri kratka, dodajem dva propositos između, sve zajedno oko 20 minuta
    -rWuZ8bzyNE->MP-01.mp3, _M0oDhv5VqQ->MP-02.mp3, Jh8o8_7REb0->MP-03.mp3, 7TUXN-Q7UEk->MP-04.mp3, SoiZGtABr78->MP-05.mp3
    ffmpeg -i "concat:MP-01.mp3|silence-1s.mp3|MP-02.mp3|silence-1s.mp3|MP-03.mp3|silence-1s.mp3|MP-04.mp3|silence-1s.mp3|MP-05.mp3" -acodec copy mejorpeor.mp3
for f in universitarios calle futuros diade transporte mejorpeor; do echo "https://mg94c18gonzales.fra1.digitaloceanspaces.com/"${f}.mp3 > app/src/gonzales/assets/$f && echo >> app/src/gonzales/assets/$f && cp cs app/src/gonzales/assets/$f.bukvalno && cp cs app/src/gonzales/assets/$f.finalno && s3cmd put $f.mp3 s3://mg94c18gonzales/ && s3cmd setacl --acl-public s3://mg94c18gonzales/$f.mp3; done

Dodati i triton i holesterol (Catolica) na Universitarios

Bolji plan za Sondeo, jer ima boljih videa a neki od odabranih nisu dobri a neki dobri nisu odabrani.  Ovde ima dva sata, pa u četiri grupe:
	S17 definitivno
	S61 može veselo
	S46 dva dinara druže, može
	S84 može, učitelji
ffmpeg -i "concat:S17.mp3|S61.mp3|S46.mp3|S84.mp3" -acodec copy sondeoa.mp3
eXSyHHUYObk,u_112Off-kM,12co_wMp3YM,AwV0aYBZ7PY

	S48 definitivno
	S59 ne žale se fala Bogu
	S65 buduće generacije, treba
	S91 posetioci hoćemo
	S20 bolji za padre
ffmpeg -i "concat:S48.mp3|S59.mp3|S65.mp3|S91.mp3|S20.mp3" -acodec copy sondeob.mp3
WOIpU2BY3wc,51ymPKFogZw,XNMEWwyaYUQ,Bm1USgG44bY,d1inr183Xhs

	S72 GSP obavezno
	S56 Nova godina, dobro
	S70 škola treba
	S93 neki B&B, može, neki renteri, ilegalci
	S82 mejor peor, 82 pa mora
	S60 AI može
ffmpeg -i "concat:S72.mp3|S56.mp3|S70.mp3|S93.mp3|S82.mp3|S60.mp3" -acodec copy sondeov.mp3
3bmEipDunuc,ImLSdvVpO-s,RFzAGESELn4,tF9FJatMQLs,7TUXN-Q7UEk,J5HNZn1bLsc

	S75 požari i đubre hoćemo
	S89 treba GSP ali razmaknuto, takođe to je TP-02
	S86 može vraćanje u školu
	S69 mejor peor, dedica
ffmpeg -i "concat:S75.mp3|S89.mp3|S86.mp3|S69.mp3" -acodec copy sondeog.mp3
t2AvXAisWmI,GImpYPS6uK0,9Gy41LmajfE,SoiZGtABr78

ispod crte za aplikaciju, ali može da ostane Slušač:
	S21 možda, prilično dugačko
	S42 je ok neki prodavci i pandemija
	S43 je ok kupujmo domaće a ne iz Kine
	S45 može fino Uskrs
	S49 može GSP
	S53 Vaka sad je bolji, Zadušnice a i dugačko
	S55 može
	S57 možda kriminal
	S67 može
	S90 može GSP
	S92 može ali razmaknuto od S91
	S96 još jedan Materice, može
	S50 dia de algo: ni prvi ni poslednji nisu dobri, ali ostali može; po tome
	S51 po istom principu
	S58 mejor+peor može da ostane, ali su oko 20 minuta umesto 30, pa onda da ih razbijemo možda; pa po tome
	S12 isto
	S68 isto
	ovi svi zajedno, bez onih diade+transporte+mejorpeor, već imaju više od dva sata.

Entrevistas:
	najbolje da imam neke intervjue za koje imam i španski i engleski, tako mogu da potvrdim svoj prevod
	Shakira: https://www.youtube.com/watch?v=avMfRt64Kmg
	Christian Nodal: https://www.youtube.com/watch?v=S40cTWhvE84
	Peso Pluma: https://www.youtube.com/watch?v=KX9cq7pqARc

for i in {57..96}; do export first=$(ls *.mp4 | grep -vE "^[0-9][0-9]\-" | head -n 1) && echo mv \"${first}\" $i-$(echo ${first} | sed -e 's/.*\-//') > move1 && sh move1; done

al + infinitivo
Introduce una acción o un acontecimiento que se produce simultáneamente o inmediatamente antes que otro:
    * decidió marcharse al comprender lo que pasaba
    * al abrir los ojos, todo estaba oscuro
    * perdió el equipaje al cambiarse de tren.
    * dakle i glagoski prilog prošli i sadašnji

ir + gerundio
Indica que la acción que se expresa se está realizando progresivamente:
    * el enfermo va mejorando poco a poco.

a + infinitivo
    Introduce una orden o una advertencia: ¡a callar!; ¡todo el mundo a correr!; a partir de ahora, a escuchar.

a + infinitivo o a que
    Introduce la finalidad o la intención de alguna acción: he venido a tomar el sol; me quedaré a cenar; salió a que le diera el aire.

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

https://www.ingles.com/traductor/hechas
https://www.wordreference.com/conj/esverbs.aspx?v=dar

-----------------------------------------

Za Špance imamo https://www.youtube.com/watch?v=um6DhjyF5q8 kao prvu umesto ABVGD

-----------------------------------------
