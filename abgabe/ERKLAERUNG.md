# Praktikum 05: Binärbäume verstehen und erklären

Dieses Dokument erklärt die Aufgaben 1–5. Die Präsentation konzentriert sich auf Aufgabe 3 und verwendet die Messmethoden aus Aufgabe 4 für ein zusätzliches Experiment. Die vollständigen Java-Dateien im Abgabepaket sind zum Kopieren gedacht; hier geht es darum, ihre Entscheidungen nachvollziehen und mündlich begründen zu können.

## 1. Das gemeinsame Grundprinzip

Ein binärer Suchbaum besteht aus Knoten. Jeder Knoten hat einen Wert und höchstens zwei Kinder. In dieser Vorlage gilt für **alle Werte der jeweiligen Teilbäume**:

```text
links: Wert <= Knotenwert       rechts: Wert > Knotenwert
```

Die Ungleichheit ist links absichtlich nicht streng: `insertAt()` schickt auch gleiche Werte nach links. Ein zweiter Teilnehmer mit gleicher Laufzeit bekommt daher einen eigenen Knoten im linken Teilbaum. Gleiche Werte werden in diesem Praktikum nicht zu einem gemeinsamen Knoten zusammengefasst.

`TreeNode` besitzt zwar eine Liste `values`. Diese ist laut Kommentar bereits für eine spätere AVL-Variante vorbereitet. Im hier verwendeten `SortedBinaryTree` enthält jeder neu erzeugte Knoten genau einen Wert. Die Traversierungen laufen trotzdem über die Liste, damit sie zur vorhandenen Datenstruktur passen.

Die Klasse ist generisch:

```java
SortedBinaryTree<T extends Comparable<T>>
```

`T` ist der Typ der gespeicherten Daten, beispielsweise `String`, `Integer` oder `Competitor`. `extends Comparable<T>` verlangt, dass diese Daten über `compareTo()` miteinander verglichen werden können. Der Baum muss dadurch weder Namen noch Laufzeiten kennen. Er verwendet nur das Ergebnis des Vergleichs:

| Ergebnis von `a.compareTo(b)` | Bedeutung |
|---|---|
| kleiner als 0 | `a` kommt vor `b` |
| gleich 0 | `a` und `b` haben denselben Sortierwert |
| grösser als 0 | `a` kommt nach `b` |

**Wichtig:** Ein Vergleich muss nicht genau `-1`, `0` oder `1` liefern. Deshalb prüft der Code `< 0`, `== 0` oder `> 0`.

### Warum die Einfügereihenfolge wichtig ist

Beim Einfügen wird an der Wurzel begonnen. Jeder Vergleich entscheidet, in welchem Teilbaum weitergesucht wird. Sobald die passende Kinderreferenz `null` ist, entsteht dort ein neuer Knoten.

```java
node.left = insertAt(node.left, x);
```

Der rekursive Aufruf liefert die neue Wurzel des linken Teilbaums zurück. Die Zuweisung verbindet diesen Teilbaum wieder mit dem aktuellen Knoten. Beim Einfügen in einen leeren Baum gilt das gleiche Prinzip für `root = insertAt(root, x)`.

Ein gewöhnlicher Suchbaum verändert seine Form danach nicht automatisch. Insbesondere gibt es hier keine AVL-Rotationen. Deshalb können dieselben Daten je nach Einfügereihenfolge einen flachen Baum oder eine lange Kette erzeugen.

## 2. Aufgabe 1: einen vollständigen geordneten Baum zeichnen

Die Werte sind `4, 6, 7, 8, 10, 12, 14, 17`. Ein passender vollständiger Suchbaum ist:

```text
             10
           /    \
          7      14
         / \    /  \
        6   8  12  17
       /
      4
```

Dieser Baum erfüllt zwei Anforderungen gleichzeitig:

1. **Geordnet:** Die Inorder-Reihenfolge lautet `4, 6, 7, 8, 10, 12, 14, 17`.
2. **Vollständig:** Alle Ebenen oberhalb der letzten sind vollständig besetzt. Die letzte Ebene wird von links nach rechts ohne Lücke gefüllt.

Eine Einfügereihenfolge, die genau diesen Baum erzeugt, ist:

```text
10, 7, 14, 6, 8, 12, 17, 4
```

Sie entspricht seiner Levelorder-Reihenfolge. Zum Beispiel wird `4` zuerst mit `10`, dann mit `7` und schliesslich mit `6` verglichen. Es landet links unter `6`.

Die Höhe beträgt **4 Ebenen**. Drei Ebenen reichen für höchstens `1 + 2 + 4 = 7` Knoten; bei acht Knoten ist eine vierte Ebene unvermeidlich. Der Baum hat somit die kleinstmögliche Höhe.

### Begriffe sauber trennen

| Begriff | Bedeutung |
|---|---|
| binär | höchstens zwei Kinder pro Knoten |
| geordnet / Suchbaum | die Werte erfüllen die Suchbaumordnung |
| vollständig | alle Ebenen bis auf eventuell die letzte sind voll; die letzte ist linksbündig gefüllt |
| perfekt | sämtliche Ebenen sind vollständig gefüllt |
| voll / strikt | jeder Knoten hat entweder kein Kind oder genau zwei Kinder |
| höhenbalanciert | bei jedem Knoten unterscheiden sich die Höhen seiner Teilbäume höchstens um 1 |

Der gezeichnete Baum ist vollständig und höhenbalanciert. Er ist nicht perfekt und nicht voll/strikt, denn `6` hat genau ein Kind. Da diese Begriffe in Lehrunterlagen gelegentlich anders verwendet werden, sollte man beim Erklären die jeweilige Eigenschaft dazusagen.

**Mündliche Erklärung:** „Wir wählen eine Wurzel und die folgenden Werte so, dass sich die Daten auf die Teilbäume verteilen. Die Reihenfolge 10, 7, 14, 6, 8, 12, 17, 4 erzeugt einen vollständigen Baum mit minimaler Höhe vier.“

## 3. Aufgabe 2: Traversierung und Visitor

### Die Aufgabenteilung

Die **Traversierung** legt fest, wann welcher Wert an die Reihe kommt. Der **Visitor** legt fest, was mit diesem Wert geschieht.

```java
public interface Visitor<T> {
    void visit(T obj);
}
```

`visit()` erhält genau einen Datenwert. Der Visitor kann diesen Wert beispielsweise ausgeben, in einer Liste sammeln oder ihm einen Rang zuweisen. Er muss dafür die Baumstruktur nicht kennen.

Die Vorlage verwendet eine einfache Visitor-Schnittstelle, die technisch wie ein Callback funktioniert. Beim klassischen Visitor-Entwurfsmuster findet man oft zusätzlich `accept()`-Methoden und verschiedene `visit()`-Überladungen für unterschiedliche Knotentypen. Das wird hier nicht benötigt. Die zentrale Idee bleibt: Die Aktion auf einem Element wird von der Struktur und dem Durchlauf getrennt.

```java
MyVisitor<String> visitor = new MyVisitor<>();
tree.traversal().inorder(visitor);
System.out.println(visitor);
```

Dieser Ablauf bedeutet:

1. Ein Visitor wird erzeugt. `MyVisitor` enthält einen `StringBuilder`.
2. `tree.traversal()` erzeugt ein `TreeTraversal`-Objekt mit der aktuellen Wurzel.
3. `inorder(visitor)` geht durch den Baum.
4. Für jeden Datenwert wird `visitor.visit(value)` aufgerufen.
5. `MyVisitor.visit()` hängt den Wert an seinen `StringBuilder` an.

Die Verbindung zwischen Baum und Traversierung lautet:

```java
public Traversal<T> traversal() {
    return new TreeTraversal<>(root);
}
```

`TreeTraversal` erhält eine Referenz auf die Wurzel. Der Baum wird dafür nicht kopiert.

### Öffentliche Methode und rekursive Hilfsmethode

Die öffentliche Methode ist der bequeme Einstieg:

```java
public void inorder(Visitor<T> vis) {
    inorder(root, vis);
}
```

Von aussen wird nur der Visitor übergeben. Die Hilfsmethode erhält zusätzlich den Knoten, dessen Teilbaum sie gerade bearbeitet:

```java
private void inorder(TreeNode<T> node, Visitor<T> vis)
```

Die beiden Methoden dürfen denselben Namen haben, weil sie unterschiedliche Parameterlisten besitzen. Das ist **Überladen**. Die Rekursion entsteht erst dadurch, dass die Hilfsmethode sich selbst mit einem Kindknoten aufruft.

```java
if (node == null) {
    return;
}
```

Dies ist der Abbruchfall: Ein leerer Teilbaum enthält nichts, das verarbeitet werden könnte. `return` beendet nur **diesen** Methodenaufruf. Danach läuft der vorherige Aufruf an der Stelle weiter, an der er auf die Rückkehr gewartet hat.

Alle rekursiven Aufrufe erhalten denselben Visitor. Dadurch werden sämtliche Ergebnisse in demselben Objekt gesammelt. Ein neuer Visitor pro Aufruf würde den gemeinsam aufgebauten Zustand verlieren.

### Inorder: links, Knoten, rechts

```text
inorder(linker Teilbaum)
Werte des aktuellen Knotens besuchen
inorder(rechter Teilbaum)
```

Beim Beispielbaum

```text
    B
   / \
  A   C
       \
        D
```

läuft die Rekursion so ab:

```text
inorder(B)
  inorder(A)
    inorder(null) -> kehrt zurück
    visit(A)
    inorder(null) -> kehrt zurück
  visit(B)
  inorder(C)
    inorder(null) -> kehrt zurück
    visit(C)
    inorder(D)
      inorder(null) -> kehrt zurück
      visit(D)
      inorder(null) -> kehrt zurück
```

Das Ergebnis ist `ABCD`. Der Aufruf für `B` verschwindet beim Aufruf für `A` nicht. Java merkt sich, dass es anschliessend mit `visit(B)` weitergeht. Dieser Speicher für noch nicht abgeschlossene Methodenaufrufe heisst **Aufrufstapel** beziehungsweise Callstack.

Warum ist Inorder sortiert? Im linken Teilbaum liegen nur kleinere oder gleiche Werte. Danach kommt der aktuelle Wert. Im rechten Teilbaum liegen nur grössere Werte. Wenn dieses Argument rekursiv für jeden Teilbaum gilt, ist die gesamte Ausgabe aufsteigend sortiert.

### Preorder und Postorder

Die Rekursion bleibt gleich. Nur die Position des Besuchs wird verschoben:

| Traversierung | Reihenfolge | Ergebnis im Beispiel |
|---|---|---|
| Preorder | Knoten, links, rechts | `BACD` |
| Inorder | links, Knoten, rechts | `ABCD` |
| Postorder | links, rechts, Knoten | `ADCB` |

„Knoten besuchen“ bedeutet in der Vorlage immer, seine Werte mit `vis.visit(value)` an den Visitor weiterzugeben. Der Besuch ist keine automatische Ausgabe; die konkrete Visitor-Implementierung entscheidet über die Aktion.

### Levelorder: eine Queue merkt sich die nächsten Knoten

Levelorder arbeitet Ebene für Ebene von links nach rechts. Dafür verwendet die Implementierung eine Queue. **FIFO** bedeutet „First In, First Out“: Der zuerst eingefügte Knoten wird zuerst wieder entnommen.

Der Ablauf ist:

1. Bei einem leeren Baum sofort zurückkehren.
2. Die Wurzel in die Queue legen.
3. Solange die Queue nicht leer ist, den vordersten Knoten entnehmen.
4. Dessen Werte besuchen.
5. Sein vorhandenes linkes und danach sein vorhandenes rechtes Kind hinten einreihen.

Für den Beispielbaum ergibt sich:

| Schritt | Queue vor dem Entnehmen | Besuch | Queue danach |
|---|---|---|---|
| 1 | `[B]` | B | `[A, C]` |
| 2 | `[A, C]` | A | `[C]` |
| 3 | `[C]` | C | `[D]` |
| 4 | `[D]` | D | `[]` |

Das Ergebnis ist `BACD`. Dass es hier mit Preorder übereinstimmt, liegt nur an diesem kleinen Baum. Bei grösseren Bäumen unterscheiden sich die beiden Reihenfolgen normalerweise.

`Queue<TreeNode<T>>` speichert Knoten, weil später deren Kinder benötigt werden. Der Visitor erhält dagegen die Datenwerte vom Typ `T`.

### Aufwand der Traversierungen

Bei `n` gespeicherten Werten wird jeder Wert genau einmal besucht. Bei konstanter Arbeit in `visit()` brauchen alle vier Traversierungen **Θ(n)** Zeit.

Für Preorder, Inorder und Postorder benötigt die Rekursion zusätzlich **O(h)** Platz, wobei `h` die Baumhöhe ist. Levelorder benötigt eine Queue mit **O(w)** Einträgen, wobei `w` die grösste Anzahl Knoten auf einer Ebene ist; im Worst Case ist das O(n).

Bei einem ausgeglichenen Baum ist der Rekursionsstapel logarithmisch tief. Bei einer Kette kann er `n` Aufrufe tief werden. Sehr grosse degenerierte Bäume können deshalb bei einer rekursiven Implementierung einen `StackOverflowError` auslösen.

## 4. Aufgabe 3: eine Rangliste mit dem Suchbaum erstellen

### Die Verarbeitungskette

```text
CSV-Text
  -> Zeilen und Felder zerlegen
  -> Competitor-Objekte erzeugen
  -> nach Laufzeit in den Suchbaum einfügen
  -> Inorder durchlaufen
  -> fortlaufende Ränge vergeben und Text erzeugen
```

Die Datei verwendet ein Semikolon als Feldtrenner:

```text
Mueller Stefan;02:31:14
Marti Adrian;02:30:09
Kiptum Daniel;02:11:31
```

Es handelt sich für diese Aufgabe um ein einfaches Format mit zwei Feldern pro Datenzeile. Die Implementierung ist kein allgemeiner CSV-Parser für beliebige Anführungszeichen, eingebettete Trennzeichen und mehrzeilige Felder.

### `createTree()`: aus Text wird ein Suchbaum

```java
Tree<Competitor> competitorTree = new SortedBinaryTree<>();
```

Die Variable wird über das Interface `Tree<Competitor>` verwendet; tatsächlich erzeugt wird ein `SortedBinaryTree<Competitor>`. Das Interface beschreibt die angebotenen Operationen, die Klasse implementiert sie.

Die Methode zerlegt den Text in Zeilen und jede relevante Zeile am Semikolon in Name und Laufzeit. Daraus entsteht beispielsweise:

```java
new Competitor(0, name, time)
```

Der Anfangsrang `0` ist ein Platzhalter. Vor dem Sortieren steht der endgültige Rang noch nicht fest. Anschliessend fügt `competitorTree.add(...)` das Objekt ein.

Die Reihenfolge der CSV-Zeilen ist somit die **Einfügereihenfolge**. Es gibt vor dem Einfügen keinen separaten Sortieraufruf.

Die einzelnen Schritte beim Einlesen sind:

| Code | Warum er verwendet wird |
|---|---|
| `rankingText.split("\\R")` | Trennt an Zeilenumbrüchen, unter anderem Windows-CRLF und Unix-LF. Im Java-String muss der Rückstrich doppelt geschrieben werden. |
| `lines[i].trim()` | Entfernt Leerraum vor und nach einer Zeile. |
| `if (line.isEmpty()) continue;` | Überspringt leere Zeilen; `continue` beginnt die nächste Schleifenrunde. |
| `line.split(";", -1)` | Trennt Felder; `-1` erhält auch ein leeres letztes Feld, damit eine fehlende Zeit erkannt wird. |
| `fields.length != 2` | Verlangt genau die zwei vorgesehenen Felder. |
| `fields[0].trim().isEmpty()` | Erkennt einen fehlenden Namen. |
| `matches("\\d{2}:[0-5]\\d:[0-5]\\d")` | Verlangt zwei Stundenziffern und Minuten/Sekunden im Bereich 00–59. |
| `throw new IllegalArgumentException(...)` | Meldet ein ungültiges Format zusammen mit der Zeilennummer. |
| `competitorTree.add(new Competitor(...))` | Erzeugt und fügt einen Teilnehmer mit Anfangsrang 0 ein. |
| `return competitorTree` | Liefert den aufgebauten Baum an den Aufrufer zurück. |

Die Stundenprüfung verlangt zwei Ziffern, beschränkt diese aber nicht zusätzlich auf 00–23. Die mitgelieferten Laufzeiten liegen in einem passenden Bereich. Die Formatprüfung ist keine vollständige Neuentwicklung der vorgegebenen Zeitverarbeitung.

`execute()` verbindet anschliessend die beiden Arbeitsschritte: Es ruft zuerst `createTree(rankingList)` auf und hängt danach den von `createSortedText(...)` erzeugten Text an die Überschrift `Rangliste (Tree)` an.

### `Competitor.compareTo()`: der Sortierschlüssel ist die Laufzeit

Der Baum vergleicht Teilnehmer über deren `compareTo()`. Die vorhandene Vorlage wandelt die Zeittexte mit `SimpleDateFormat("HH:mm:ss")` in Zeitwerte um und bildet deren Differenz.

Eine kürzere Laufzeit erzeugt beim Vergleich mit einer längeren Laufzeit ein negatives Ergebnis. Sie wird deshalb links eingeordnet. Die Namen und die zunächst gesetzten Ränge beeinflussen die Sortierreihenfolge nicht.

Die bestehende Zeitverarbeitung wurde für die Praktikumsaufgabe beibehalten. Sie verarbeitet Uhrzeittexte mit `DateFormat`; sie wurde nicht in eine neue Dauerklasse oder einen einmal berechneten Sekundenschlüssel umgebaut. In einer weiterentwickelten Anwendung könnte man eine Laufzeit einmal beim Einlesen als Sekunden speichern und danach direkt diese Zahl vergleichen. Das wäre eine mögliche Optimierung und ist nicht der hier behauptete Implementierungsstand.

### `createSortedText()`: Inorder liefert die richtige Reihenfolge

Der zentrale Code lautet:

```java
AtomicInteger rank = new AtomicInteger(1);
StringBuilder sb = new StringBuilder();

competitorTree.traversal().inorder(c -> {
    c.setRank(rank.getAndIncrement());
    sb.append(c).append("\n");
});

return sb.toString();
```

Jede Zeile hat eine klare Aufgabe:

| Ausdruck | Wirkung |
|---|---|
| `new AtomicInteger(1)` | Der nächste zu vergebende Rang beginnt bei 1. |
| `new StringBuilder()` | Ein veränderbarer Puffer sammelt den Ausgabetext. |
| `competitorTree.traversal()` | Erzeugt den Baumdurchlauf ab der Wurzel. |
| `.inorder(...)` | Übergibt die Teilnehmer nach aufsteigender Laufzeit an die Aktion. |
| `c -> { ... }` | Beschreibt die `visit(c)`-Aktion des Visitors als Lambda. |
| `rank.getAndIncrement()` | Liefert den bisherigen Rang und erhöht den Zähler danach um 1. |
| `c.setRank(...)` | Speichert diesen Rang im gerade besuchten Teilnehmer. |
| `sb.append(c)` | Hängt die Textdarstellung `c.toString()` an. |
| `.append("\n")` | Schliesst die Zeile mit einem Zeilenumbruch ab. |
| `sb.toString()` | Liefert den fertigen Text als `String`. |

Der Baum ist nach der Laufzeit geordnet. Eine Änderung von `rank` verändert daher nicht seine Suchbaumordnung.

### Warum darf hier ein Lambda verwendet werden?

`Visitor<T>` hat genau eine abstrakte Methode: `void visit(T obj)`. Deshalb kann Java den Lambda-Block als Implementierung dieser Methode auffassen. Ausführlicher geschrieben würde die Aktion so aussehen:

```java
Visitor<Competitor> visitor = new Visitor<Competitor>() {
    @Override
    public void visit(Competitor c) {
        c.setRank(rank.getAndIncrement());
        sb.append(c).append("\n");
    }
};

competitorTree.traversal().inorder(visitor);
```

Es entsteht nicht für jeden Knoten ein neuer Visitor. Dieselbe Aktion wird wiederholt mit dem jeweils nächsten Teilnehmer ausgeführt.

### Warum `AtomicInteger` und kein normales `int`?

Eine lokale Variable, die in einem Java-Lambda verwendet wird, muss `final` oder **effektiv final** sein. Effektiv final heisst: Nach ihrer Initialisierung wird ihr kein neuer Wert zugewiesen.

Folgendes würde deshalb nicht kompilieren:

```java
int rank = 1;
// Im Lambda: c.setRank(rank++);  // rank würde neu zugewiesen.
```

Bei `AtomicInteger` bleibt die Referenz `rank` gleich. Verändert wird nur der Zustand des referenzierten Objekts. Das ist erlaubt. Das gleiche Prinzip gilt für den `StringBuilder`: Die Variable `sb` zeigt weiterhin auf dasselbe Objekt, dessen Inhalt wächst.

Hier wird `AtomicInteger` als veränderbarer Zähler verwendet. Für die Aufgabe laufen keine Threads parallel; seine Fähigkeit zu atomaren Operationen wird dafür nicht benötigt. Eine eigene Visitor-Klasse mit einem normalen `int`-Feld wäre ebenfalls möglich.

### Beispiel: drei Teilnehmer von Anfang bis Ende

Einfügereihenfolge:

```text
Mueller  02:31:14
Marti    02:30:09
Kiptum   02:11:31
```

Mueller wird Wurzel. Marti ist schneller und kommt links darunter. Kiptum ist schneller als beide und wird links unter Marti eingefügt:

```text
      Mueller
      /
    Marti
    /
 Kiptum
```

Inorder geht zuerst bis ganz nach links. Die Besuche sind:

```text
visit(Kiptum)  -> Rang 1 -> erste Ausgabezeile
visit(Marti)   -> Rang 2 -> zweite Ausgabezeile
visit(Mueller) -> Rang 3 -> dritte Ausgabezeile
```

Die Ausgabe ist sortiert, obwohl die Struktur hier ungünstig hoch ist. **Korrektheit der Reihenfolge und Effizienz der Baumform sind zwei unterschiedliche Eigenschaften.**

### Gleiche Laufzeiten

Bei gleicher Laufzeit ergibt `compareTo()` den Wert `0`. Wegen `<= 0` beim Einfügen gelangt der neue Teilnehmer in den linken Teilbaum. Alle Teilnehmer bleiben erhalten.

Die Implementierung und die bereitgestellten Tests vergeben fortlaufende Ränge `1, 2, 3, ...`. Es gibt keine zusätzliche Regel wie „gleiche Zeit bedeutet gleicher Rang“. Unter Teilnehmern mit gleichem Sortierwert garantiert diese Variante zudem keine stabile Beibehaltung ihrer ursprünglichen Reihenfolge. Das sollte bei einer Nachfrage als bestehende Aufgabenentscheidung erklärt werden.

### Laufzeit: Aufbau und Durchlauf getrennt betrachten

Sei `n` die Anzahl Teilnehmer und `h` die aktuelle Baumhöhe.

Eine einzelne Einfügung folgt einem Pfad von der Wurzel zu einer leeren Stelle. Ihre Kosten sind proportional zur Länge dieses Pfads, im schlechtesten Fall **O(h)**.

| Schritt | günstige / im Mittel zufällige Struktur | ungünstigste Struktur |
|---|---|---|
| einmal einfügen | O(log n) | O(n) |
| alle `n` Werte einfügen | Θ(n log n) | Θ(n²) |
| Inorder und Ränge zuweisen | Θ(n) | Θ(n) |
| gesamtes TreeSort-Verfahren | Θ(n log n) | Θ(n²) |

Die günstige Θ(n log n)-Aussage der klassischen Tabelle bezieht sich auf Schlüssel, die sich zu einem flachen Baum anordnen lassen, insbesondere verschiedene Schlüssel. Für die Average-Case-Aussage setzt die Standardanalyse **verschiedene Schlüssel und gleichwahrscheinliche zufällige Einfügereihenfolgen** voraus. Die konkrete CSV enthält auch gleiche Laufzeiten. Die gemessenen Mittelwerte dieser Datei sind deshalb eine empirische Aussage für diesen Datensatz, keine unveränderte Anwendung aller Formeln für verschiedene Schlüssel. Bei ausschliesslich gleichen Schlüsseln erzeugt diese Duplikatregel beispielsweise in jeder Einfügereihenfolge eine Kette.

Im günstigen Fall ist die Tiefe der meisten Einfügungen logarithmisch. Insgesamt fallen in der Grössenordnung `n · log n` Vergleiche an.

Im Worst Case werden verschiedene Werte bereits sortiert eingefügt. Es entsteht eine Kette. Der erste Wert benötigt keinen Vergleich, der zweite einen, der dritte zwei und so weiter:

```text
0 + 1 + 2 + ... + (n - 1) = n(n - 1) / 2
```

Das wächst quadratisch. Für die Regel „gleiche Werte nach links“ erzeugt eine nach Laufzeit **absteigend** sortierte Eingabe auch mit gleichen Laufzeiten eine linke Kette. Bei aufsteigender Eingabe sind gleiche Werte eine Besonderheit: Sie gehen links und verhindern deshalb unter Umständen eine einzige reine rechte Kette.

**O** beschreibt eine asymptotische obere Schranke. **Θ** beschreibt die genaue asymptotische Grössenordnung durch eine obere und eine untere Schranke. In der Präsentation könnt ihr die geläufigen Bezeichnungen O(n log n) und O(n²) verwenden; bei der präzisen Erklärung nennt ihr die Voraussetzungen und die Θ-Aussagen aus der Tabelle.

Die Tabelle zählt algorithmische Arbeit, insbesondere Schlüsselvergleiche. Bei den festen Zeitformaten werden Vergleiche als konstante Arbeit modelliert. Tatsächliche Millisekunden enthalten zusätzlich das Parsen der Zeiten, Speicherverwaltung, JVM-Effekte und die Textausgabe. Die Länge des erzeugten Textes spielt bei genauer Betrachtung ebenfalls eine Rolle.

### Speicherbedarf

Der Baum enthält `n` Teilnehmerknoten: **Θ(n)** Speicher. Die rekursive Inorder-Traversierung benötigt zusätzlich **O(h)** Stapelplatz. Der Ausgabetext belegt Speicher proportional zu seiner Zeichenanzahl.

Bei gleich beschränkter Zeilenlänge kann man den gesamten Speicherbedarf als O(n) zusammenfassen. Die Aussage „Inorder braucht O(h) zusätzlichen Speicher“ bezieht sich nur auf den Durchlauf, nicht auf Baum und Ergebnistext zusammen.

## 5. Aufgabe 4: Höhe und Grösse rekursiv bestimmen

### Welche Höhe wird gezählt?

Die Tests verwenden die Anzahl Ebenen beziehungsweise Knoten auf dem längsten Wurzel-Blatt-Pfad:

```text
leerer Baum: 0
ein einzelner Knoten: 1
```

Andere Bücher zählen die Kanten und geben einem einzelnen Knoten Höhe 0. Beide Definitionen sind möglich. Hier verwenden wir konsequent die Definition der Vorlage.

### Höhe: das Maximum der beiden Teilbaumhöhen

```java
if (node == null) {
    return 0;
}
return 1 + Math.max(calcHeight(node.left), calcHeight(node.right));
```

Der linke und der rechte Teilbaum melden jeweils ihre Höhe zurück. Für den längsten Weg wird der grössere Wert benötigt. Die `1` zählt den aktuellen Knoten hinzu.

Beim Beispielbaum aus Aufgabe 2:

```text
height(A) = 1 + max(0, 0) = 1
height(D) = 1 + max(0, 0) = 1
height(C) = 1 + max(0, 1) = 2
height(B) = 1 + max(1, 2) = 3
```

Die Berechnung steigt also zuerst in die Teilbäume hinab. Die Zahlen werden beim Zurückkehren der Methodenaufrufe von unten nach oben zusammengesetzt.

Die öffentliche Methode `height()` ruft die Hilfsmethode mit `root` auf. Das in `TreeNode` vorhandene Feld `height` ist für eine spätere AVL-Aufgabe vorgesehen. Diese Aufgabe berechnet die Höhe rekursiv aus der tatsächlichen Struktur.

### Grösse: beide Teilbaumgrössen addieren

Bei einem leeren Teilbaum ist die Grösse `0`. Sonst werden die Knoten des linken Teilbaums, der aktuelle Knoten und die Knoten des rechten Teilbaums gezählt. Im verwendeten `SortedBinaryTree` liegt genau ein Wert pro Knoten, deshalb stimmen Elementzahl und Knotenzahl überein.

```text
size(Knoten) = size(linker Teilbaum) + 1 + size(rechter Teilbaum)
```

Die konkrete Hilfsmethode verwendet genau diese Rechnung:

```java
protected int calcSize(TreeNode<T> p) {
    if (p == null) {
        return 0;
    }
    return 1 + calcSize(p.left) + calcSize(p.right);
}
```

Beim Beispielbaum sind die Teilgrössen `size(A) = 1`, `size(D) = 1`, `size(C) = 2` und `size(B) = 4`.

Der Unterschied zur Höhe ist entscheidend: Für die Höhe interessiert nur der **längere** Weg, deshalb `max`. Für die Grösse interessieren **alle** Knoten, deshalb Addition.

### Aufwand und Aussage der Messung

Sowohl `height()` als auch `size()` müssen jeden Knoten ansehen: **Θ(n)** Zeit und **O(h)** zusätzlicher Rekursionsspeicher. Dass die berechnete Höhe beispielsweise O(log n) ist, macht ihre vollständige Berechnung nicht zu O(log n).

Für die mitgelieferte Datei `RangZuerich.csv` ergibt die Originalreihenfolge **3 166 Knoten und Höhe 42**. Der Baum ist damit deutlich höher als die allgemeine Mindesthöhe von 12 für diese Knotenzahl. Das zeigt, dass ein gewöhnlicher Suchbaum die Balance nicht garantiert. Die konkrete Eingabereihenfolge und gleiche Schlüssel beeinflussen seine Form.

Für `n` Knoten beträgt die allgemeine Untergrenze der Höhe:

```text
h_min = ceil(log2(n + 1))
```

Diese Grenze folgt daraus, dass `h` vollständig gefüllte Ebenen höchstens `2^h - 1` Knoten aufnehmen. Bei vorgeschriebener Duplikatbehandlung ist zusätzlich zu prüfen, ob diese minimale Höhe für die konkreten Schlüssel erreichbar ist. Ein Vergleich mit der Grenze ist ein sinnvoller Massstab, aber kein Beweis, dass jede konkrete Eingabe diese Grenze erreichen kann.

### Ergänzung der Vorlage: `balanced()`

Die zuvor leere Balanceprüfung ist ebenfalls vervollständigt. Sie prüft für **jeden** Knoten, ob sich linke und rechte Teilbaumhöhe höchstens um 1 unterscheiden. Es reicht nicht, nur an der Wurzel nachzusehen.

Die Hilfsmethode `balancedHeight()` liefert entweder eine nichtnegative Höhe oder den Sonderwert `-1`, wenn irgendwo im Teilbaum ein Ungleichgewicht gefunden wurde. Dieses Ergebnis wird nach oben weitergegeben. Dadurch werden Höhe und Balance in einem Durchlauf berechnet: O(n) Zeit und O(h) zusätzlicher Stapelspeicher.

`balanced()` **prüft** nur die Form. Es fügt keine Rotationen hinzu und verändert den Baum nicht. Ausserdem wurde `printTree()` für den leeren Baum abgesichert; es liefert dann einen leeren Text.

## 6. Aufgabe 5: einen Wertebereich gezielt durchsuchen

`interval(min, max, visitor)` soll alle Werte aus dem **abgeschlossenen** Intervall `[min, max]` in sortierter Reihenfolge besuchen. Beide Grenzen zählen mit.

Die Grundstruktur entspricht Inorder. Der Unterschied ist, dass die Ordnung des Suchbaums genutzt wird, um sicher ungeeignete Teilbäume zu überspringen.

Sei `x` der Wert des aktuellen Knotens:

| Bedingung | Folgerung |
|---|---|
| `x < min` | Links liegen nur Werte `<= x`, also ebenfalls unter `min`: links überspringen. |
| `x > max` | Rechts liegen nur Werte `> x`, also ebenfalls über `max`: rechts überspringen. |
| `min <= x && x <= max` | Den aktuellen Wert besuchen. |

Für die rekursiven Aufrufe ergeben sich die präzisen Bedingungen:

```text
Links besuchen, wenn x >= min.
Aktuellen Knoten besuchen, wenn min <= x <= max.
Rechts besuchen, wenn x < max.
```

### Warum links `>=`, rechts aber `<`?

Bei `x == min` können links weitere gleiche Werte liegen. Diese gehören zum Intervall und dürfen nicht verloren gehen. Die Bedingung für links muss deshalb die Gleichheit einschliessen.

Bei `x == max` kann rechts kein weiterer passender Wert liegen, weil sämtliche rechten Werte **streng grösser** sind. Rechts darf daher bereits bei Gleichheit übersprungen werden.

Für `[B, C]` im Baum aus Aufgabe 2 läuft die Suche so:

1. Bei `B` wird links geprüft, weil auch weitere `B`-Werte vorkommen könnten. `A` liegt unter der Grenze und wird nicht besucht.
2. `B` liegt im Intervall und wird besucht.
3. Rechts wird `C` erreicht und besucht.
4. `C == max`, deshalb wird dessen rechter Teilbaum mit `D` übersprungen.

Das Ergebnis ist `BC`. Für `[B, B]` müssen alle vorhandenen `B`-Werte ausgegeben werden. Ein umgekehrtes Intervall mit `min > max` enthält keine Werte.

### Aufwand

Mit `k` ausgegebenen Werten und Höhe `h` gilt für diese Bereichssuche **O(h + k)**. Die Methode muss zu den Grenzen gelangen und die passenden Werte ausgeben. Ganze sicher ungeeignete Teilbäume werden ausgelassen.

In einem ausgeglichenen Baum ergibt das O(log n + k). In einem degenerierten Baum kann die Suche weiterhin O(n) benötigen. Eine Suche, die zuerst den ganzen Baum mit Inorder besucht und im Visitor filtert, wäre zwar inhaltlich möglich, würde aber die gewünschte Optimierung verfehlen: Sie braucht unabhängig von der Ergebnisgrösse immer Θ(n) Besuche.

## 7. Das Experiment: welche Frage wird untersucht?

Die Präsentation untersucht: **Wie verändert die Einfügereihenfolge derselben Teilnehmer den Aufwand des Baumaufbaus und die Höhe des entstehenden Baums?**

Die sortierte Rangliste soll dabei gleich bleiben, abgesehen von der Reihenfolge unter gleichen Laufzeiten. Variiert wird die Reihenfolge, in der dieselben Datensätze eingefügt werden.

### Warum nicht alle Reihenfolgen der ganzen Datei?

Für `n` unterscheidbare Datensätze existieren `n!` mögliche Reihenfolgen. Schon bei 10 Datensätzen sind das `3 628 800`, bei 20 ungefähr `2,43 · 10^18`. Eine vollständige Aufzählung der grossen Rangliste ist praktisch nicht durchführbar.

Auch gleiche Zeiten machen zwei Teilnehmerdatensätze nicht zu derselben Person. Betrachtet man nur die Zeitfolgen, reduziert sich deren Zahl um die Fakultäten der Häufigkeiten gleicher Zeiten; für die ganze Datei bleibt die Zahl trotzdem astronomisch.

Ein nachvollziehbares Experiment kombiniert daher:

1. **Vollständige Aufzählung einer kleinen Teilmenge:** Alle Einfügereihenfolgen dieser ausdrücklich angegebenen Teilmenge werden untersucht.
2. **Zufallsstichprobe für die gesamte Datei:** Viele zufällig gemischte Reihenfolgen ergeben eine empirische Verteilung.
3. **Gezielt konstruierte Referenzen:** Eine günstige Reihenfolge und eine garantiert degenerierende absteigende Reihenfolge veranschaulichen die Extremfälle.
4. **Die Originalreihenfolge:** Sie zeigt, wo die tatsächlich gelieferte CSV gegenüber den Zufallsmischungen liegt.

Die konkrete Durchführung verwendet **alle 40 320 Permutationen von acht verschiedenen Laufzeiten** und **10 000 Zufallsmischungen der gesamten Datei**. Der Zufallsgenerator ist `java.util.Random` mit Seed `20261019`, gemischt wird mit Fisher–Yates. Der feste Seed erlaubt es, dieselben Versuchsfolgen erneut zu erzeugen.

Die acht Zeiten wurden über gleichmässig verteilte Positionen in der sortierten Liste der verschiedenen Laufzeiten gewählt:

```text
02:11:31, 03:10:05, 03:25:15, 03:37:11,
03:49:16, 04:02:37, 04:23:29, 05:33:39
```

Die vollständige CSV enthält **3 166 Zeilen, 2 459 verschiedene Laufzeiten und eine exakt doppelte Zeile**. Die höchste Häufigkeit einer einzelnen Laufzeit beträgt fünf. Man muss gleiche Laufzeiten deshalb im Experiment erhalten und korrekt behandeln.

Für markierte Zeilenpositionen wären `3166! ≈ 6,17 · 10^9709` Permutationen zu untersuchen. Weil eine CSV-Zeile zweimal identisch vorkommt, gibt es `3166! / 2` unterschiedliche vollständige CSV-Zeilenfolgen. Beide Zahlen sind praktisch unbeherrschbar. Zahlen aus den 10 000 Zufallsmischungen sind deshalb ausdrücklich eine Stichprobe.

Die Rohdaten und Zusammenfassung liegen in `experiment/results/`, insbesondere in `summary.json`, `exact8.csv`, `full_random.csv` und `references.csv`. Die Anleitung im Experimentordner enthält die Reproduktionsbefehle.

### Was soll gemessen werden?

- **Baumhöhe:** Wie lang ist der längste Suchpfad?
- **Anzahl Schlüsselvergleiche beim Aufbau:** Wie viel Sucharbeit wurde über sämtliche Einfügungen geleistet?

Die Vergleichszahl ist für die Erklärung der Laufzeit besonders nützlich. Sie hängt nicht vom zufälligen Zustand des Rechners oder dem Aufwärmen der JVM ab. Sie ist eine **Operationszählung**, keine gemessene Zeit in Millisekunden.

Für die vielen Wiederholungen arbeitet der Experimentcode mit einer instrumentierten iterativen Baumdarstellung und vorab in Sekunden umgewandelten Zeiten. Er verwendet exakt dieselbe Entscheidungsregel `<=` nach links und `>` nach rechts. Dadurch wird derselbe strukturelle Aufbauaufwand gezählt, ohne für jeden Vergleich erneut `DateFormat` aufzurufen und ohne eine Rekursionstiefe von 3 166 für die Worst-Case-Kette zu benötigen. Die Zahlen sind dadurch kein Benchmark der gesamten Java-Anwendung inklusive Parsing und Ausgabe.

Die Baumhöhe allein beschreibt nicht alle Kosten: Zwei Bäume können dieselbe maximale Höhe, aber unterschiedliche Verteilungen der übrigen Knotentiefen haben. Die Summe der Einfügetiefen entspricht bei der hier verwendeten Einfügung der Anzahl der Schlüsselvergleiche. Diese Summe ist deshalb direkter mit dem Gesamtaufwand verknüpft.

Ein Histogramm zeigt, wie häufig bestimmte Kosten oder Höhen in der Stichprobe vorkommen. Markierungen können den Mittelwert der Zufallsdurchläufe, die Originalreihenfolge und die konstruierten Referenzen zeigen. Ein beobachtetes Minimum ist zunächst nur das beste Ergebnis der betrachteten Stichprobe; die Bezeichnung „globaler Best Case“ verlangt eine zusätzliche Begründung.

### Tatsächliche Resultate: alle Permutationen von acht Werten

| Messgrösse | Best Case | exakter Mittelwert | Worst Case |
|---|---:|---:|---:|
| Vergleiche beim Aufbau | 13 | 16,9214 | 28 |
| Baumhöhe | 4 | 5,0175 | 8 |

Hier sind „Best“, „Average“ und „Worst“ **exakt für die ausgewählten acht verschiedenen Werte**, weil alle 8! Reihenfolgen untersucht wurden. Der Mittelwert gewichtet jede Einfügereihenfolge gleich, nicht jede mögliche Baumform gleich.

Die besten Vergleichskosten lassen sich auch an einem möglichst flachen Baum ablesen: Ein Knoten auf Tiefe 0, zwei auf Tiefe 1, vier auf Tiefe 2 und einer auf Tiefe 3 ergeben `0 + 2 · 1 + 4 · 2 + 1 · 3 = 13`. Der Worst Case ergibt `8 · 7 / 2 = 28`.

### Tatsächliche Resultate: gesamte Datei

| Einfügereihenfolge | Baumhöhe | Vergleiche beim Aufbau |
|---|---:|---:|
| Median zuerst, flache Referenz | 14 | 30 840 |
| 10 000 Zufallsmischungen: Mittelwert | 26,6472 | 42 043,5089 |
| Originalreihenfolge der CSV | 42 | 75 599 |
| nach Laufzeit aufsteigend | 2 459 | 3 822 922 |
| nach Laufzeit absteigend: garantierter Worst Case | 3 166 | 5 010 195 |

Die absteigende Folge erreicht genau `3166 · 3165 / 2 = 5 010 195` Vergleiche. Mehr kann ein Aufbau mit dieser Einfügemethode nicht benötigen, weil beim `i`-ten Einfügen höchstens alle `i - 1` vorhandenen Knoten verglichen werden. Damit ist diese Referenz ein nachgewiesener Worst Case.

Die Medianreferenz ist eine gezielt flache Reihenfolge. Wegen gleicher Laufzeiten wird sie korrekt als **günstige Referenz**, nicht als bewiesenes globales Optimum bezeichnet. Ihre Konstruktion verwendet vorab sortierte Daten; diese Vorbereitung wird nicht zu den angegebenen Baumaufbauvergleichen gezählt. Sie ist ein kontrollierter Versuchsfall, keine Behauptung, dass man die Vorsortierung kostenlos als Beschleunigung des eigentlichen Algorithmus erhält.

Die Originaldatei benötigt etwa **1,80-mal** so viele Aufbauvergleiche wie die Zufallsmischungen im Mittel. Die Worst-Case-Folge benötigt ungefähr **119-mal** so viele. Inorder besucht anschliessend in jedem vollständigen Versuch dieselben **3 166 Teilnehmer**.

### Verteilung und Unsicherheit auseinanderhalten

In den 10 000 Zufallsversuchen lagen die Höhen zwischen **22 und 38**. Das empirische 2,5%-Quantil beträgt **23**, das 97,5%-Quantil **31**. Diese Quantile beschreiben die **Streuung einzelner Versuchsergebnisse**; wegen der ganzzahligen Höhe und Häufungen an den Grenzen muss der inklusive Anteil im Intervall nicht exakt 95 % betragen.

Das näherungsweise 95%-Monte-Carlo-Konfidenzintervall für die **erwartete Höhe** lautet dagegen **[26,6077; 26,6867]**. Es ist viel schmaler, weil 10 000 Wiederholungen den Mittelwert relativ genau schätzen. Es wird als `Mittelwert ± 1,96 · Stichprobenstandardabweichung / sqrt(Anzahl Versuche)` berechnet.

Das entsprechende Intervall für die mittleren Aufbauvergleiche beträgt **[42 004,18; 42 082,83]**. Diese Intervalle quantifizieren die Monte-Carlo-Unsicherheit unter dem verwendeten Zufallsmodell. Sie erfassen weder eine andere Datenbasis noch ein anderes Einfügeverfahren und bedeuten nicht, dass 95 % aller Einzelbäume innerhalb dieses schmalen Intervalls liegen.

Für eine kurze Präsentation reicht die Formulierung: „Typische einzelne Zufallsbäume liegen ungefähr zwischen Höhe 23 und 31; ihre mittlere Höhe schätzen wir auf 26,65.“ Die ausführlichen statistischen Begriffe dienen der Vorbereitung auf Nachfragen.

### Warum entstehen die Fälle?

**Günstiger Fall:** Die frühen Werte teilen die verbleibenden Werte ungefähr in zwei Hälften. Dadurch bleiben die Pfade kurz. Eine passende Medianreihenfolge zeigt diese Idee für verschiedene Schlüssel; bei gleichen Zeiten muss die Regel „gleich nach links“ berücksichtigt werden.

**Zufälliger Fall:** Zufälliges Mischen vermeidet eine systematisch monotone Reihenfolge. Viele unterschiedlich geformte Bäume entstehen, häufig mit relativ kurzen Pfaden. Das ist keine Balancegarantie für jeden einzelnen Versuch.

**Ungünstigster Fall:** Jeder neue Wert wird immer weiter an dieselbe Seite angehängt. Bei absteigender Reihenfolge entsteht aufgrund von `<=` einschliesslich gleicher Werte eine linke Kette. Die Einfügekosten summieren sich zu `n(n - 1)/2`.

Der Inorder-Durchlauf bleibt in allen Fällen linear. Die grosse Veränderung betrifft den **Aufbau des Suchbaums**.

## 8. Die Präsentation zu zweit erklären

Für 5–8 Minuten ist eine Zielzeit von ungefähr 6½ Minuten sinnvoll. Der Hauptteil sollte eine klar erkennbare Geschichte erzählen: Problem und Baumidee, konkrete Implementierung, Laufzeiterwartung, Experiment, Erklärung der Resultate.

Eine mögliche Aufteilung ist:

| Teil | Person | Richtzeit |
|---|---|---|
| Problem, Suchbaumordnung, Visitor | Person 1 | 1:30 min |
| Implementierung der Rangliste | Person 1 | 1:30 min |
| Laufzeit und Experimentaufbau | Person 2 | 1:15 min |
| Resultate und Erklärung der Fälle | Person 2 | 2:00 min |
| gemeinsame Schlussaussage | Person 2 | 0:15 min |

Auf den Folien reichen kurze Aussagen und gezielte Codeausschnitte. Die Details dieses Dokuments dienen der Vorbereitung auf Nachfragen.

### Formulierungen, die ihr frei erklären können solltet

**Suchbaum:** „Jeder Vergleich entscheidet zwischen links und rechts. Links liegen bei unserer Vorlage kleinere oder gleiche Laufzeiten, rechts grössere. Die erste eingefügte Person wird die Wurzel.“

**Visitor:** „Der Durchlauf bestimmt die Reihenfolge. Unser Visitor führt pro Teilnehmer die eigentliche Arbeit aus: Rang setzen und Ausgabezeile anhängen.“

**Implementierung:** „Wir lesen Name und Laufzeit aus jeder Zeile, erzeugen einen Teilnehmer und fügen ihn ein. Weil der Baum nach Laufzeit geordnet ist, liefert Inorder die Teilnehmer vom schnellsten zum langsamsten.“

**Laufzeit:** „Das Einfügen hängt von der Baumhöhe ab. Bei kurzen Pfaden kostet der gesamte Aufbau n log n, bei einer Kette n Quadrat. Das anschliessende Ausgeben bleibt linear, weil jeder Teilnehmer genau einmal besucht wird.“

**Experiment:** „Wir verändern nur die Einfügereihenfolge derselben Daten. Für eine kleine Teilmenge prüfen wir alle Permutationen; für die ganze Datei verwenden wir reproduzierbare Zufallsmischungen und konstruierte Referenzen. Gemessen werden Höhe und Schlüsselvergleiche.“

**Interpretation:** „Die Reihenfolge der Ausgabe ist weiterhin korrekt. Die Einfügereihenfolge bestimmt jedoch die Form des Baums und damit die Arbeit, die vorher beim Aufbau anfällt.“

### Typische Nachfragen

**Warum nicht einfach eine sortierte Liste verwenden?**

Die Aufgabe untersucht das Sortieren über einen Suchbaum und die Trennung von Traversierung und Verarbeitung. Für eine praktische reine Sortieraufgabe wäre eine Bibliothekssortierung eine naheliegende Alternative. Der normale Suchbaum garantiert hier keine logarithmische Höhe.

**Ist ein binärer Suchbaum automatisch ausgeglichen?**

Nein. „Geordnet“ beschreibt die Beziehung der Schlüssel, „ausgeglichen“ beschreibt seine Form. Selbstbalancierende Varianten wie AVL-Bäume verwenden zusätzliche Regeln und Rotationen; dieser Baum tut das nicht.

**Warum ist Inorder auch bei einer Kette nur O(n)?**

Weil jeder Knoten nur einmal besucht wird. Beim Aufbau wird dagegen für jede neue Einfügung erneut ein immer längerer Pfad durchlaufen. Die hohe Rekursionstiefe der Kette beeinflusst den zusätzlichen Stapelspeicher, aber nicht die lineare Zahl der Inorder-Besuche.

**Kann ich `preorder()` für die Rangliste verwenden?**

Es besucht ebenfalls alle Teilnehmer, liefert sie aber im Allgemeinen nicht nach Laufzeit sortiert. Die Suchbaumordnung garantiert die sortierte Folge bei Inorder.

**Was passiert bei gleicher Zeit?**

Beide Teilnehmer bleiben erhalten. Der später eingefügte gleiche Schlüssel geht links. Die Aufgabe vergibt fortlaufende Rangnummern; gleiche Zeiten erhalten nicht automatisch denselben Rang.

**Beweist das Histogramm O(n log n)?**

Ein Histogramm für eine feste Datenmenge zeigt die Unterschiede zwischen Einfügereihenfolgen bei genau diesem `n`. Die asymptotische Aussage beschreibt das Wachstum bei zunehmendem `n` und wird theoretisch begründet. Die Messung veranschaulicht diese Theorie, ersetzt aber keinen Beweis über alle Eingabegrössen.

**Ist der Stichprobenmittelwert der mathematische Average Case?**

Er schätzt den erwarteten Aufwand unter der verwendeten Zufallsverteilung für diesen Datensatz. Die theoretische Standardaussage benötigt klar benannte Voraussetzungen, insbesondere verschiedene Schlüssel und gleichwahrscheinliche Permutationen.

**Wurde tatsächlich Zeit gemessen?**

Die Grafiken mit Schlüsselvergleichen zählen Arbeitsschritte. Solche Werte sind reproduzierbarer und für die Erklärung von O-Notation direkter als Millisekunden. Vergleichszahlen sollten deshalb auch als Vergleichszahlen beschriftet werden.

**Warum heisst die Medianreferenz nicht einfach Best Case?**

Die ausgewählten acht verschiedenen Werte wurden vollständig untersucht, deshalb kennen wir dort den echten Best Case. Für alle 3 166 Datensätze mit gleichen Laufzeiten wurde kein globales Minimum bewiesen. Median zuerst ist eine gute konstruierte Reihenfolge, erreicht hier aber Höhe 14 gegenüber der allgemeinen Höhenuntergrenze 12.

**Was wurde am Code überprüft?**

Der Gradle-Testlauf bestand alle **21 Tests**: die zehn vorgegebenen Tests und elf zusätzliche Tests. Dabei werden unter anderem die Traversierungen, Rangliste, Grössen-/Höhenberechnung sowie Randfälle wie leere Bäume, gleiche Werte und Intervallgrenzen geprüft. Der Testlauf bestätigt die geprüften Fälle; er ersetzt nicht die oben erklärte algorithmische Begründung.

## 9. Quellen und Geltungsbereich

- Aufgabenstellung „Praktikum 05 – Binärbaum“ und mitgelieferte Java-Vorlage; insbesondere `TreeNode`, `SortedBinaryTree`, `Visitor`, `Competitor` sowie die Tests `ADS5_2_test` bis `ADS5_5_test`.
- Robert Sedgewick / Kevin Wayne, Princeton, [Algorithms, 3.2 Binary Search Trees](https://algs4.cs.princeton.edu/32bst/): Suchbaumordnung, Inorder und Abhängigkeit der Operationen von der Baumform.
- Robert Sedgewick / Philippe Flajolet, Princeton, [An Introduction to the Analysis of Algorithms, Chapter 6: Trees](https://aofa.cs.princeton.edu/60trees/): Analyse zufälliger Suchbäume.

Die konkrete Behandlung gleicher Laufzeiten, die Höhenkonvention und die Rangvergabe ergeben sich aus dieser Vorlage und ihren Tests. Das Experiment untersucht die mitgelieferten Daten und ist keine Behauptung über alle denkbaren CSV-Dateien.
