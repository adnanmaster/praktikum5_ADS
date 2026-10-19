# LaTeX-Präsentation

Die Präsentation behandelt nur Aufgabe 3, mit einem auf Aufgabe 4 aufbauenden Experiment. Acht Folien sind für ungefähr 6 Minuten 30 Sekunden ausgelegt.

## Dateien

- `main.tex`: Einstieg, Beamer-Madrid-Thema, Farben, Typografie und Namen.
- `slides.tex`: alle acht Folien mit eingebetteten Beamer-Sprechernotizen.
- `results.tex`: ausgewählte Messwerte aus der Experimentauswertung.
- `images/`: Vektorgrafiken des Experiments als PDF.
- `Rangliste.pdf`: fertig kompilierte Präsentation.
- `SPRECHTEXT.md`: ausformulierter Text für zwei Personen und Antworten auf Rückfragen.

## Anpassen und kompilieren

Ersetzt in `main.tex` die Platzhalter `Name 1` und `Name 2` in `\author{...}`. Der gesamte Ordner kann als ZIP in Overleaf hochgeladen werden. Wählt `main.tex` als Hauptdatei und pdfLaTeX als Compiler.

Lokal in diesem Ordner:

```powershell
pdflatex -interaction=nonstopmode -halt-on-error -no-shell-escape main.tex
pdflatex -interaction=nonstopmode -halt-on-error -no-shell-escape main.tex
```

Das erzeugt `main.pdf`; die mitgelieferte Version trägt zur besseren Erkennbarkeit den Namen `Rangliste.pdf`. Benötigt werden die üblichen Pakete `beamer`, `babel` mit Deutsch, `lmodern`, `amsmath`, `booktabs`, `listings`, `pgf/tikz` und `graphicx`. Es sind keine externen Programme für Syntaxhervorhebung nötig.

## Gestaltungsgrundlage

Die Vorlage des Nutzers bleibt als 16:9-Beamer-Präsentation mit Madrid erhalten. Die Farben, Kopf- und Fusszeilen sind für eine ruhige Darstellung angepasst. Baumdiagramme sind editierbarer TikZ-Code. Die Datenplots sind Vektor-PDFs; ihre Daten und der bearbeitbare Matplotlib-Code liegen im Experimentordner.

## Fachliche Einordnung

Nur acht verschiedene Zeiten werden vollständig permutiert. Die vollständige CSV wird mit 10’000 reproduzierbaren Zufallsreihenfolgen untersucht. „Median zuerst“ ist ein flacher Referenzfall und kein bewiesenes globales Optimum der CSV mit gleichen Zeiten. Gemessen werden Höhe und Schlüsselvergleiche, keine Millisekunden. Die klassischen asymptotischen Average-Case-Aussagen setzen verschiedene zufällig angeordnete Schlüssel voraus.
