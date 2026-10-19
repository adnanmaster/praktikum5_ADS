package ch.zhaw.ads;

import java.util.concurrent.atomic.AtomicInteger;

public class RankingTreeServer implements CommandExecutor {

    public Tree<Competitor> createTree(String rankingText) {
        Tree<Competitor> competitorTree = new SortedBinaryTree<>();
        String[] lines = rankingText.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split(";", -1);
            if (fields.length != 2 || fields[0].trim().isEmpty()
                    || !fields[1].trim().matches("\\d{2}:[0-5]\\d:[0-5]\\d")) {
                throw new IllegalArgumentException(
                        "Zeile " + (i + 1) + ": Erwartet wird Name;HH:mm:ss");
            }
            competitorTree.add(new Competitor(0, fields[0].trim(), fields[1].trim()));
        }
        return competitorTree;
    }

    public String createSortedText(Tree<Competitor> competitorTree) {
        AtomicInteger rank = new AtomicInteger(1);
        StringBuilder sb = new StringBuilder();
        competitorTree.traversal().inorder(c -> {
            c.setRank(rank.getAndIncrement());
            sb.append(c).append("\n");
        });
        return sb.toString();
    }

    public String execute(String rankingList) {
        Tree<Competitor> competitorTree = createTree(rankingList);
        return "Rangliste (Tree)\n" + createSortedText(competitorTree);
    }

    public static void main(String[] args) {
        String rangliste = "Mueller Stefan;02:31:14\n" +
                "Marti Adrian;02:30:09\n" +
                "Kiptum Daniel;02:11:31\n" +
                "Ancay Tarcis;02:20:02\n" +
                "Kreibuhl Christian;02:21:47\n" +
                "Ott Michael;02:33:48\n" +
                "Menzi Christoph;02:27:26\n" +
                "Oliver Ruben;02:32:12\n" +
                "Elmer Beat;02:33:53\n" +
                "Kuehni Martin;02:33:36\n";
        RankingTreeServer server = new RankingTreeServer();
        System.out.println(server.execute(rangliste));
    }
}
