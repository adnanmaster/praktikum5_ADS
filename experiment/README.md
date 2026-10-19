# Experiment: Einfügereihenfolge, Baumform und Sortieraufwand

Dieses Experiment erweitert Aufgabe 4 für die Präsentation von Aufgabe 3. Es misst, wie die Reihenfolge derselben Teilnehmer den **unbalancierten** Suchbaum und damit den Aufwand zum Erstellen der Rangliste verändert. Es erzeugt echte Messdaten; die Diagramme sind aus den mitgelieferten Rohdaten berechnet.

## Was genau wird gemessen?

1. **Baumhöhe h in Ebenen:** Ein leerer Baum hat Höhe 0, ein einzelner Knoten Höhe 1. Ein Pfad aus n Knoten hat Höhe n.
2. **Einfügevergleiche C:** Für jeden bereits vorhandenen Knoten auf dem Einfügepfad wird ein Schlüsselvergleich gezählt. Der erste eingefügte Wert benötigt keinen Vergleich. Damit gilt C = Summe aller Einfügetiefen.
3. **Inorder-Besuche:** Nach dem Aufbau besucht Inorder jeden Teilnehmer genau einmal, also stets n Werte. Diese n Besuche sind **nicht** Teil von C.

Für die gesamte Sortierung bedeutet das im üblichen Modell mit konstanten Kosten pro Schlüsselvergleich und Wertverarbeitung: Aufwand Θ(C+n). Die Zeichenverarbeitung der Eingabe und der Ausgabe kommt hinzu; bei variablen Textlängen muss auch deren Gesamtlänge berücksichtigt werden.

**Es wurden keine Millisekunden gemessen.** `Competitor.compareTo()` parst in der Vorlage die Zeiten bei jedem Vergleich erneut. Das Experiment parst die Zeiten einmal in Sekunden und untersucht die Vergleichszahl sowie die Baumstruktur. Es kann deshalb keine Aussage wie «119-mal schneller» begründen. Korrekt ist «etwa 119-mal so viele Einfügevergleiche».

## Daten und Identität

- Datei: `src/ch/zhaw/ads/RangZuerich.csv`, UTF-8 ohne Kopfzeile.
- 3'166 CSV-Zeilen, 2'459 unterschiedliche Zeiten, 707 zusätzliche Vorkommen bereits vorhandener Zeiten.
- Höchste Häufigkeit einer Zeit: 5.
- Kleinste/grösste Zeit: 02:11:31 / 05:33:39.
- Eine CSV-Zeile kommt zweimal exakt identisch vor; insgesamt 3'165 verschiedene Zeileninhalte.
- SHA-256: `8083c6d3897d5f3b3add6041f9437e010d99cf5e9d3ffe9989c418a8ba126a9d`.

Die Eingabe wird weder dedupliziert noch verändert. Jeder Datensatz wird als eigener Knoten eingefügt. Gleiche Zeiten gehen wie in `SortedBinaryTree.add()` **nach links**: `neu <= aktuell -> links`, `neu > aktuell -> rechts`. Der Array-Simulator bildet genau diese Struktur ab. Er balanciert den Baum nicht und fasst keine Duplikate zusammen.

## Warum nicht alle Reihenfolgen der gesamten CSV?

Für 3'166 mit einer Zeilen-ID unterscheidbare Datensätze gibt es

`3166! ≈ 6.174 × 10^9709` Permutationen.

Wegen der einen exakt doppelten CSV-Zeile existieren `3166! / 2` verschiedene Folgen der tatsächlichen Zeileninhalte. Wenn nur die Zeiten betrachtet werden, gibt es `3166! / Produkt(m_t!)` verschiedene Schlüsselfolgen, wobei m_t die Häufigkeit einer Zeit bezeichnet. Deren Zehnerlogarithmus beträgt noch etwa 9472.849. Auch diese Zahl ist nicht vollständig durchrechenbar.

Deshalb enthält das Experiment zwei sauber getrennte Teile:

### A: Vollständige Auswertung einer Teilmenge

Wir wählen **acht unterschiedliche Zeiten** aus der CSV. Zuerst werden alle unterschiedlichen Zeiten aufsteigend sortiert. Dann werden die Indizes `floor(i*(d-1)/7)` für i=0,…,7 genommen. Das ergibt über die Zeitspanne verteilte Werte:

`02:11:31, 03:10:05, 03:25:15, 03:37:11, 03:49:16, 04:02:37, 04:23:29, 05:33:39`.

Für diese acht Werte werden **alle 8! = 40'320 Reihenfolgen** durch rekursives Vertauschen genau einmal aufgezählt. Für die Baumform zählt bei unterschiedlichen Schlüsseln nur die relative Ordnung, nicht der zeitliche Abstand. Daher hätte jede andere Auswahl von acht verschiedenen Zeiten dieselbe Verteilung.

| Messgrösse | Best / Minimum | Exakter Mittelwert | Worst / Maximum |
|---|---:|---:|---:|
| Höhe in Ebenen | 4 | 5.0174603 | 8 |
| Einfügevergleiche | 13 | 16.9214286 | 28 |

«Average» ist hier das arithmetische Mittel bei Gleichgewichtung **aller Reihenfolgen**. Es ist kein einzelner Baum und kein Konfidenzintervall. Alle Permutationen sind vollständig erfasst.

Die Minima von Höhe und Vergleichszahl sind verschiedene Zielgrössen: Nicht jeder Baum minimaler Höhe hat automatisch die minimale Summe der Knotentiefen. Im n=8-Experiment haben 11'360 Reihenfolgen die Höhe 4, aber nur 1'680 die minimale Vergleichszahl 13.

Plausibilitätskontrollen:

- Minimale Höhe: `ceil(log2(8+1)) = 4`.
- Minimale Vergleichszahl: `0*1 + 1*2 + 2*4 + 3*1 = 13`.
- Maximale Vergleichszahl: `0+1+…+7 = 8*7/2 = 28`.
- Theorie für zufällige Permutationen verschiedener Schlüssel: `E[C_n] = 2(n+1)H_n - 4n`, mit `H_n = Summe(1/k, k=1…n)`. Für n=8 ergibt das ebenfalls 16.9214286.

### B: Monte-Carlo-Stichprobe für die gesamte CSV

Wir ziehen 10'000 pseudozufällige Reihenfolgen aller 3'166 Datensätze. Vor jedem Durchlauf wird die ursprüngliche Reihenfolge kopiert und mit Fisher-Yates gemischt. Verwendet wird `java.util.Random` mit **Seed 20261019**. Die Ziehungen erfolgen mit Zurücklegen zwischen Durchläufen; eine bestimmte Reihenfolge könnte theoretisch erneut auftreten.

Fisher-Yates behandelt alle markierten Zeilenpermutationen gleich. Bei identischen Zeitwerten besitzt jede unterschiedliche Schlüsselfolge gleich viele markierte Urbilder; dadurch entsteht auch für die Schlüsselfolgen eine Gleichverteilung im idealisierten Zufallsmodell. Der dokumentierte Pseudozufallsgenerator ermöglicht eine exakte Wiederholung der erzeugten Stichprobe.

| Messgrösse | Stichprobenmittel | 2.5.–97.5. Perzentil | Kleinster / grösster beobachteter Wert |
|---|---:|---:|---:|
| Höhe | 26.6472 | 23–31 | 22 / 38 |
| Einfügevergleiche | 42'043.5089 | 38'944.975–46'675.1 | 37'135 / 55'792 |

Die Perzentile beschreiben die Streuung der Einzeldurchläufe. Sie sind **kein** Konfidenzintervall für das Mittel. Wegen diskreter Höhen enthält der Bereich 23–31 nicht zwingend exakt 95% der Stichproben.

Für die Monte-Carlo-Schätzung des Mittels wird separat ein approximatives 95%-Konfidenzintervall berechnet: `Mittel ± 1.96 * Stichprobenstandardabweichung / sqrt(10000)`.

- Höhe: **[26.6077; 26.6867]**.
- Vergleiche: **[42'004.18; 42'082.83]**.

Diese Intervalle quantifizieren die Stichprobenunsicherheit unter dem Zufallsmodell; sie sagen nichts über systematische Modellfehler aus. Die kleinsten/grössten Werte der 10'000 Zufallsfolgen sind keine bewiesenen globalen Best-/Worst-Cases.

### Konstruierte Vergleichsfälle derselben vollständigen CSV

| Reihenfolge | Höhe | Einfügevergleiche | Bedeutung |
|---|---:|---:|---|
| Median zuerst | 14 | 30'840 | Flache Referenz; kein bewiesenes globales Optimum |
| Zufällig, Mittel von 10'000 Folgen | 26.6472 | 42'043.5089 | Geschätzter Average Case für diese feste CSV |
| CSV-Original | 42 | 75'599 | Tatsächliche Eingabereihenfolge |
| Aufsteigend | 2'459 | 3'822'922 | Sehr ungünstig; wegen Duplikaten keine reine Kette |
| Absteigend | 3'166 | 5'010'195 | Erreicht die allgemeine obere Schranke, also echter Worst Case |

Bei «Median zuerst» werden die sortierten Werte rekursiv vom Median aus eingefügt. Falls mehrere gleiche Werte an dieser Position stehen, wird die **letzte gleiche Position innerhalb des Teilbereichs** gewählt. So bleiben rechts nur echt grössere Schlüssel, wie es die Implementierung verlangt. Dies produziert eine flache Referenz, beweist aber keine globale Optimalität unter der Duplikatregel. Die allgemeine Höhenuntergrenze eines Binärbaums mit 3'166 Knoten ist 12; unsere Referenz hat Höhe 14.

Absteigende Daten laufen auch bei gleichen Zeiten immer nach links und erzeugen einen Pfad. Der k-te eingefügte Datensatz hat k−1 Vorgänger auf seinem Pfad. Deshalb gilt exakt `C = n(n−1)/2 = 5'010'195`. Aufsteigende Daten mit Duplikaten erzeugen dagegen eine rechte Hauptkette verschiedener Zeiten mit linken Duplikatästen. Deshalb erreichen sie hier eine andere Höhe und Vergleichszahl als die absteigende Folge.

Der absteigende Fall braucht etwa **119.17-mal so viele Einfügevergleiche** wie das gemessene Zufallsmittel. In allen Fällen kommen genau 3'166 Inorder-Besuche dazu.

## Verbindung zur Laufzeittheorie

- Einfügen eines Werts: Aufwand proportional zu seiner Einfügetiefe, höchstens O(h).
- Flacher Baum: `h = Θ(log n)`, Aufbau `Θ(n log n)`.
- Zufällige Reihenfolge **verschiedener Schlüssel**: erwarteter Aufbau `Θ(n log n)`.
- Kette: `h=n`, Aufbau `Θ(n²)`.
- Inorder: immer `Θ(n)` Wertbesuche, unabhängig von der Baumform; zusätzlicher Rekursionsspeicher `O(h)`.
- Gesamtsortierung: Aufbau plus Inorder. Best Case `Θ(n log n)`, Average Case unter dem genannten Zufallsmodell `Θ(n log n)`, Worst Case `Θ(n²)`.

Die durchschnittliche `Θ(n log n)`-Aussage benötigt das Zufallsmodell und passende Schlüsselannahmen. Bei n gleichen Werten erzeugt die Regel «gleich nach links» bei **jeder** Permutation eine Kette. Zufälliges Mischen ersetzt keine Balancierung. In der vorliegenden CSV ist die maximale Zeit-Häufigkeit 5; das Experiment untersucht genau diese feste Verteilung.

## Reproduktion

Alle folgenden Befehle werden im **Projektordner** ausgeführt, nicht im Unterordner `experiment`.

```powershell
# Java 21, Simulation und Rohdaten (ohne Abhängigkeiten auf Gradle)
javac -encoding UTF-8 -d experiment/classes experiment/BstExperiment.java
java -cp experiment/classes BstExperiment

# JSON-Auswertung und alle PDF-/PNG-Grafiken (Python mit Matplotlib)
python experiment/analyze.py

# Alternative: nur summary.json, braucht keine externen Python-Pakete
python experiment/analyze.py --summary-only

# Produktive Java-Klassen kompilieren und Modell dagegen prüfen
.\gradlew.bat classes
javac -encoding UTF-8 -cp 'build/classes/java/main;experiment/classes' -d experiment/classes experiment/CrossCheck.java
java -Xss16m -cp 'build/classes/java/main;experiment/classes' CrossCheck
```

Die Simulation nimmt optional drei Parameter: Eingabepfad, Ergebnisordner und Anzahl Zufallsversuche. Die mitgelieferten Diagramme und Texte verwenden die dokumentierten Standardwerte. `analyze.py` liest die Standard-Ergebnisdateien; bei geändertem Ausgabeordner muss sein `RESULTS`-Pfad angepasst werden.

Der grössere Stack bei `CrossCheck` ist nur für die rekursiven Produktivklassen bei einer absichtlich degenerierten Kette vorgesehen. Die Simulation selbst fügt iterativ ein und benötigt diesen Schalter nicht.

Verwendete Umgebung: Temurin/OpenJDK 21.0.4+7, Python 3.13.5, Matplotlib 3.10.8. Die Rohdaten und numerische Zusammenfassung benötigen nur Java beziehungsweise die Python-Standardbibliothek. Die Java-Zufallsfolge ist durch Generator, Seed und Schleifenfolge festgelegt. Die Bundled-Python-Installation dieser Sitzung hatte kein Matplotlib; die Grafiken wurden mit der vorhandenen lokalen Python-Installation erzeugt.

## Validierung

`CrossCheck.java` vergleicht das Array-Modell mit der **echten** `SortedBinaryTree` und `TreeTraversal`:

- sämtliche 40'320 Permutationen von acht verschiedenen Schlüsseln,
- die ersten 100 mit demselben Seed erzeugten Voll-CSV-Zufallsfolgen,
- die vier Voll-CSV-Referenzen,
- leerer Baum, einzelner Wert und fünf gleiche Werte.

Alle **40'427 Prüfungen** vergleichen Höhe, Grösse, gezählte `compareTo()`-Aufrufe und die vollständige Inorder-Ausgabe. Ergebnis: bestanden.

Zusätzlich wird die Original-CSV mit dem echten `RankingTreeServer` und `Competitor` verarbeitet. Ergebnis: 3'166 Knoten, Höhe 42, 3'166 ausgegebene Zeilen, monoton sortierte Zeiten und fortlaufende Ränge 1 bis 3'166. Dadurch wird auch die echte Zeitvergleichslogik geprüft. Protokoll: `results/crosscheck.txt`.

## Dateien und Grafiken

| Datei | Inhalt |
|---|---|
| `BstExperiment.java` | Reproduzierbarer Simulator und Datenexport |
| `CrossCheck.java` | Prüfung gegen die produktiven Aufgabenklassen |
| `analyze.py` | Statistik, interne Plausibilitätskontrollen und Diagramme |
| `results/full_random.csv` | 10'000 Rohmessungen der ganzen CSV |
| `results/exact8.csv` | 40'320 Rohmessungen aller Teilmengenpermutationen |
| `results/references.csv` | Vier konstruierte/ursprüngliche Voll-CSV-Fälle |
| `results/selected8.csv` | Verwendete acht Zeiten |
| `results/scaling.csv` | Ergänzung: 100 Zufallsfolgen je n=64,128,256,512,1024,2048,3166 |
| `results/summary.json` | Datenprofil, Definitionen, Verteilungen, Mittelwerte, Quantile und Konfidenzintervalle |
| `results/crosscheck.txt` | Validierungsprotokoll |
| `figures/exact8_distribution.pdf` | Hauptgrafik: exakte Verteilung und Best/Average/Worst für n=8 |
| `figures/full_csv_main.pdf` | Hauptgrafik: Voll-CSV-Höhenverteilung und getrennte Referenzkosten |
| `figures/full_height_distribution.pdf` | Detailgrafik: Höhenverteilung, Referenzhöhen und Mittelwert-KI |
| `figures/full_comparison_cases.pdf` | Detailgrafik: Vergleichszahlen der Voll-CSV-Fälle |
| `figures/scaling.pdf` | Zusatzgrafik: Wachstum bei steigender Datensatzanzahl |

Alle Diagramme liegen zusätzlich als PNG vor. Die Hauptgrafik trennt die Zufallsverteilung von den konstruierten Extremfällen, damit die Höhe 3'166 nicht die Verteilung um 27 unlesbar zusammendrückt. Bei den Vergleichszahlen ist die logarithmische Achse ausdrücklich beschriftet.

Für die Skalierung werden jeweils die ersten n Originalzeilen gewählt und danach 100-mal gemischt, Seed 20261020 in einer fortlaufenden Zufallsfolge. Die zusätzlich eingezeichnete exakte Erwartungsformel gilt für **verschiedene** Schlüssel und ist als solche beschriftet; die CSV enthält Duplikate. Eine endliche Kurve beweist keine asymptotische Schranke. Diese Zusatzgrafik ist für den Anhang vorgesehen.
