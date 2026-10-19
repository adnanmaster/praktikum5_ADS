# Sprechtext für zwei Personen

Geplant sind acht Folien und ungefähr 6 Minuten 30 Sekunden. Person 1 übernimmt Folien 1–4, Person 2 Folien 5–8. Die Texte sind eine Formulierungshilfe. Sprecht frei und zeigt jeweils auf den gerade erklärten Code oder die Grafik.

## Person 1: Theorie, Implementierung und Laufzeit

### Folie 1 — Rangliste mit einem binären Suchbaum (25 Sekunden)

„Wir haben Aufgabe 3 gelöst: Aus einer Datei mit Teilnehmernamen und Laufzeiten erstellen wir eine Rangliste. Dafür verwenden wir einen binären Suchbaum. Anschliessend untersuchen wir, welchen Einfluss die Reihenfolge beim Einfügen auf den Aufwand hat. Unsere Datei enthält 3’166 Teilnehmer.“

### Folie 2 — Suchbaum und sortierte Ausgabe (50 Sekunden)

„Jeder Knoten enthält einen Teilnehmer. Die Laufzeit bestimmt seine Position. Im linken Teilbaum liegen kleinere oder gleiche Zeiten, im rechten grössere. Diese Regel gilt für jeden Knoten.

Für die Ausgabe verwenden wir Inorder: zuerst links, dann den aktuellen Knoten, dann rechts. Hier bekommen wir deshalb 2:10, 2:20, 2:25, 2:30 und 2:40. Die Teilnehmer erscheinen also nach Laufzeit aufsteigend. Wichtig ist: Ein geordneter Suchbaum ist nicht automatisch ausgeglichen. Seine Form hängt davon ab, welche Werte zuerst eingefügt werden.“

### Folie 3 — Implementierung: Einfügen und Visitor (65 Sekunden)

„Links sehen wir den Kern von `createTree`. Wir zerlegen jede CSV-Zeile in Name und Zeit, erzeugen einen `Competitor` und fügen ihn ein. Die vollständige Methode behandelt auch Leerzeilen und überprüft das Eingabeformat.

Rechts erstellen wir die Rangliste. Die Traversierung liefert die Teilnehmer in sortierter Reihenfolge. Das Lambda ist unser Visitor. Es legt fest, was bei jedem Teilnehmer passiert: den nächsten Rang setzen und eine Textzeile anhängen.

`getAndIncrement()` liefert zuerst den aktuellen Rang und erhöht ihn danach. `AtomicInteger` dient hier als veränderlicher Zähler im Lambda; wir verwenden keine parallele Verarbeitung. Auch gleiche Zeiten erhalten fortlaufende Rangnummern, so wie es die Vorlage und die Tests erwarten.“

### Folie 4 — Laufzeit von Aufbau und Ausgabe (50 Sekunden)

„Einfügen folgt einem Pfad durch den Baum und kostet daher höchstens O von h, wobei h die Höhe ist. In einem flachen Baum sind die Wege logarithmisch lang. Für alle Einfügungen ergibt das n log n. Entsteht dagegen eine Kette, wächst der gesamte Aufwand quadratisch.

Die Inorder-Ausgabe bleibt immer linear: Jeder Teilnehmer wird genau einmal besucht. Der Unterschied entsteht also beim Aufbau. Der klassische Average Case setzt verschiedene Schlüssel in zufälliger Reihenfolge voraus. Unsere CSV enthält auch gleiche Zeiten. Deren tatsächlichen Einfluss untersuchen wir im Experiment.

Damit übergebe ich an meine Teamkollegin beziehungsweise meinen Teamkollegen.“

## Person 2: Experiment, Ergebnisse und Ursachen

### Folie 5 — Experiment: vollständig und als Stichprobe (55 Sekunden)

„Wir erweitern Aufgabe 4: Für jede Reihenfolge bauen wir einen neuen Baum und messen seine Höhe. Zusätzlich zählen wir die Vergleiche beim Einfügen.

Für acht verschiedene Zeiten aus der CSV untersuchen wir tatsächlich alle 40’320 Reihenfolgen. Für 3’166 Datensätze ist das nicht machbar. Die Zahl der möglichen Reihenfolgen hat ungefähr 9’710 Dezimalstellen. Deshalb verwenden wir für die ganze Datei 10’000 Zufallsreihenfolgen mit einem festen Seed. Zusätzlich testen wir gezielt die Originaldatei, Median zuerst und sortierte Eingaben.

Wir zählen Vergleiche statt Millisekunden. Das vermeidet Einflüsse durch den Rechner oder das Aufwärmen der Java-Laufzeit. Unser Messmodell wurde gegen den echten Java-Baum geprüft.“

### Folie 6 — Acht Teilnehmer: die vollständige Verteilung (45 Sekunden)

„Links sehen wir die Baumhöhen über alle Reihenfolgen. Vier Ebenen sind das Minimum: Drei Ebenen haben nur sieben Plätze. Acht Ebenen bedeuten eine Kette.

Rechts sehen wir die Einfügevergleiche. Im besten Fall sind es 13, im exakten Mittel 16,92 und im schlechtesten Fall 28. Der Best Case und der Worst Case sind hier vollständig erfasst. Es handelt sich also um die genaue Verteilung für diese acht verschiedenen Zeiten, nicht um eine Schätzung.“

### Folie 7 — Die ganze CSV: Höhe und Einfügeaufwand (55 Sekunden)

„Bei 10’000 Zufallsreihenfolgen beträgt die mittlere Höhe ungefähr 26,65. Die eingezeichneten Perzentile reichen von 23 bis 31 Ebenen.

Rechts vergleichen wir die Zahl der Vergleiche auf einer logarithmischen Achse. Zufällig benötigen wir im Mittel rund 42’044 Vergleiche. Absteigend sind es gut fünf Millionen, etwa 119-mal so viele. Die Originaldatei liegt bei 75’599.

Median zuerst erzeugt einen flachen Baum mit Höhe 14. Wegen der gleichen Zeiten ist damit aber kein globales Optimum für diese CSV bewiesen. Der absteigende Fall ist dagegen ein exakter Worst Case: Er bildet eine Kette mit 3’166 Ebenen.“

### Folie 8 — Warum diese Fälle entstehen (45 Sekunden)

„Mittlere Schlüssel zuerst verteilen die Werte auf beide Seiten und erzeugen kurze Wege. Bei zufälliger Reihenfolge entstehen unterschiedliche Teilungen, deshalb streuen die Höhen.

Bei absteigender Reihenfolge muss jeder neue Wert dagegen denselben Ast entlang. So werden die Einfügewege immer länger: null plus eins plus zwei bis n minus eins. Das ergibt n mal n minus eins durch zwei und damit quadratischen Aufwand.

Die Rangliste ist in allen Fällen korrekt sortiert. Die Einfügereihenfolge bestimmt aber, wie viel Arbeit ihr Aufbau benötigt. Ein selbstbalancierender Baum wäre eine mögliche Weiterentwicklung.“

## Antworten auf wahrscheinliche Rückfragen

**Was genau bedeutet Höhe?** Hier zählen wir Ebenen: leerer Baum 0, einzelner Knoten 1. Manche Bücher zählen Kanten; ihre Werte sind für nichtleere Bäume um eins kleiner.

**Wieso habt ihr nicht alle 3’166! Reihenfolgen getestet?** Schon acht Werte erzeugen 40’320 Reihenfolgen. Für die ganze CSV sind es etwa 6,17 × 10^9709 Reihenfolgen markierter Datensätze. Eine exakt doppelte Zeile halbiert die Zahl inhaltlich verschiedener Zeilenfolgen, macht sie aber nicht berechenbar. Darum verbinden wir eine vollständige kleine Analyse mit einer reproduzierbaren Stichprobe der gesamten Datei.

**Sind die 10’000 Reihenfolgen garantiert alle verschieden?** Wir ziehen pseudorandom Permutationen mit Zurücklegen. Duplikate werden nicht gezielt ausgeschlossen. Der mögliche Raum ist extrem gross. Fisher-Yates ist bei ideal unabhängigen gleichverteilten Zufallszahlen gleichverteilt; im Programm verwenden wir den reproduzierbaren Generator `java.util.Random` mit Seed `20261019`.

**Warum reicht die Baumhöhe nicht als Messgrösse?** Die Höhe zeigt nur den längsten Pfad. Die Summe aller Einfügetiefen bestimmt die gesamte Anzahl an Vergleichen. Zwei Bäume können gleich hoch sein und trotzdem unterschiedlich viele Vergleiche benötigen.

**Was bedeutet „Median zuerst“?** Wir sortieren die Eingabewerte zur Vorbereitung der Referenz, nehmen einen mittleren Wert und gehen rekursiv mit den übrigen Teilbereichen weiter. Bei gleichen Zeiten wählen wir das letzte gleiche Element im Bereich, damit rechts nur grössere Zeiten liegen. Die Vorbereitung ist kein Teil der gemessenen BST-Einfügevergleiche. Es ist eine gezielt erzeugte Eingabereihenfolge, keine zusätzliche Optimierung unserer Abgabelösung.

**Warum ist Höhe 14 kein bewiesener Best Case?** Für beliebige Binärbäume mit 3’166 Knoten ist die Untergrenze 12 Ebenen. Gleiche Werte müssen in unserer Implementierung nach links. Unsere Medianstrategie findet einen flachen Baum, durchsucht aber nicht alle zulässigen Bäume. Deshalb nennen wir ihn einen flachen Referenzfall. Im Achtwerteexperiment kennen wir die exakten Extrema.

**Wieso ist aufsteigend nicht genauso schlecht wie absteigend?** Die CSV hat 2’459 unterschiedliche Zeiten und 707 weitere Einträge mit bereits vorkommenden Zeiten. Aufsteigend wachsen die unterschiedlichen Zeiten nach rechts, während gleiche Zeiten links angehängt werden. Es entsteht keine reine Kette. Absteigend geht dagegen jeder neue Wert, auch ein gleicher, nach links.

**Was bedeuten 23–31 Ebenen?** Das sind das 2,5. und das 97,5. Perzentil der beobachteten Höhen, also ein Bereich für die Streuung einzelner Versuche. Das approximative 95%-Konfidenzintervall des geschätzten Mittelwerts ist viel enger: 26,608 bis 26,687. Es beschreibt die Monte-Carlo-Unsicherheit des Mittelwerts. Wegen diskreter Höhen enthält der inklusive Perzentilbereich nicht notwendigerweise genau 95% der Versuche.

**Beweist das Experiment O(n log n)?** Ein Experiment mit endlichen Daten beweist keine asymptotische Laufzeit. Die Laufzeit folgt aus der Algorithmusanalyse. Die gemessenen Verteilungen veranschaulichen den Einfluss der Reihenfolge und passen zu dieser Erklärung. Zusätzliche Skalierungsdaten liegen im Experimentordner.

**Wird die echte Implementierung gemessen?** Das Messmodell verwendet vorberechnete Sekunden und exakt dieselbe Links-rechts-Regel. Für 40’427 Fälle wurden Höhe, Grösse, Vergleichsanzahl und Inorder-Ausgabe gegen `SortedBinaryTree` geprüft. Zusätzlich wurde die Originaldatei mit `RankingTreeServer` und dem unveränderten `Competitor` verarbeitet. Es ist ein Struktur- und Vergleichsexperiment, kein Laufzeitbenchmark.

**Wieso `AtomicInteger`?** Java verlangt, dass im Lambda verwendete lokale Variablen final oder effektiv final sind. Die Referenz auf das `AtomicInteger` bleibt gleich; der gespeicherte Wert kann sich ändern. Eine normale lokale Variable `int rank` könnte im Lambda nicht mit `rank++` verändert werden.

**Balanciert `balanced()` den Baum?** Die Methode prüft nur, ob jeder Knoten Teilbäume hat, deren Höhen sich um höchstens eins unterscheiden. Sie führt keine Rotation durch. Auch eine korrekte Suchbaumordnung garantiert keine Balance.

**Was passiert bei gleichen Zeiten?** Alle Teilnehmer bleiben erhalten. Gleiche Zeiten werden links eingefügt. Die Ausgabe vergibt fortlaufende Ränge; es gibt keine gemeinsame Platzierung für Zeitgleichheit. Die relative Reihenfolge gleicher Zeiten ist nicht stabil zur Eingabe.

## Quellen und Reproduktion

- Eigene Implementierung unter `src/ch/zhaw/ads/` und eigene Messdaten unter `experiment/results/`.
- [Sedgewick/Wayne: Binary Search Trees](https://algs4.cs.princeton.edu/32bst/) für die höhenabhängigen Operationen und den klassischen Zufallsfall.
- [Sedgewick/Flajolet: Trees](https://aofa.cs.princeton.edu/60trees/) für den exakten Erwartungswert der Einfügevergleiche bei verschiedenen Schlüsseln: `2(n+1)H_n - 4n`.
- Das vollständige Versuchsprotokoll und die Startbefehle stehen in `experiment/README.md`.
