import ch.zhaw.ads.Competitor;
import ch.zhaw.ads.RankingTreeServer;
import ch.zhaw.ads.SortedBinaryTree;
import ch.zhaw.ads.Tree;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Independent comparison against the actual exercise classes. */
public final class CrossCheck {
    static long checks;

    static final class CountedKey implements Comparable<CountedKey> {
        static long comparisons;
        final int value;
        CountedKey(int value) { this.value = value; }
        public int compareTo(CountedKey other) {
            comparisons++;
            return Integer.compare(value, other.value);
        }
    }

    static void check(int[] order) {
        BstExperiment.Observation expected = new BstExperiment.Simulation(order.length).build(order);
        SortedBinaryTree<CountedKey> actual = new SortedBinaryTree<>();
        CountedKey.comparisons = 0;
        for (int value : order) actual.add(new CountedKey(value));
        if (actual.height() != expected.height() || actual.size() != order.length
                || CountedKey.comparisons != expected.comparisons()) {
            throw new AssertionError("Model differs from supplied SortedBinaryTree");
        }
        List<Integer> visited = new ArrayList<>();
        actual.traversal().inorder(key -> visited.add(key.value));
        int[] expectedSorted = order.clone();
        Arrays.sort(expectedSorted);
        if (!Arrays.equals(expectedSorted, visited.stream().mapToInt(Integer::intValue).toArray())) {
            throw new AssertionError("Inorder result is incorrect");
        }
        checks++;
    }

    static void exhaustive(int[] order, int position) {
        if (position == order.length) {
            check(order);
            return;
        }
        for (int i = position; i < order.length; i++) {
            int tmp = order[i]; order[i] = order[position]; order[position] = tmp;
            exhaustive(order, position + 1);
            tmp = order[i]; order[i] = order[position]; order[position] = tmp;
        }
    }

    public static void main(String[] args) throws Exception {
        Path input = Path.of("src/ch/zhaw/ads/RangZuerich.csv");
        int[] original = BstExperiment.readSeconds(input);
        check(new int[0]);
        check(new int[]{1});
        check(new int[]{1, 1, 1, 1, 1});
        check(original);
        int[] sorted = original.clone();
        Arrays.sort(sorted);
        check(sorted);
        int[] descending = sorted.clone();
        for (int i = 0; i < descending.length / 2; i++) {
            int tmp = descending[i];
            descending[i] = descending[descending.length - 1 - i];
            descending[descending.length - 1 - i] = tmp;
        }
        check(descending);
        int[] median = new int[original.length];
        BstExperiment.medianFirst(sorted, 0, sorted.length - 1, median, 0);
        check(median);
        Random random = new Random(BstExperiment.SEED);
        for (int i = 0; i < 100; i++) {
            int[] shuffled = original.clone();
            BstExperiment.shuffle(shuffled, random);
            check(shuffled);
        }
        // Distinct-key tree shape depends only on relative key ranks.
        exhaustive(new int[]{0, 1, 2, 3, 4, 5, 6, 7}, 0);

        RankingTreeServer server = new RankingTreeServer();
        Tree<Competitor> actualRanking = server.createTree(Files.readString(input, StandardCharsets.UTF_8));
        if (actualRanking.size() != 3166 || actualRanking.height() != 42) {
            throw new AssertionError("Actual Competitor tree differs from original simulation");
        }
        String[] output = server.createSortedText(actualRanking).lines().toArray(String[]::new);
        if (output.length != original.length) throw new AssertionError("Lost ranking rows");
        int previous = -1;
        for (int i = 0; i < output.length; i++) {
            String expectedRank = (i + 1) + " ";
            if (!output[i].startsWith(expectedRank)) throw new AssertionError("Incorrect rank");
            String[] time = output[i].substring(output[i].length() - 8).split(":");
            int seconds = Integer.parseInt(time[0]) * 3600 + Integer.parseInt(time[1]) * 60
                    + Integer.parseInt(time[2]);
            if (seconds < previous) throw new AssertionError("Ranking output not sorted");
            previous = seconds;
        }
        String report = "PASS: " + checks + " model/production comparisons (height, size, insert comparisons, inorder).\n"
                + "Includes all 40,320 distinct-key n=8 permutations, 100 seeded full-CSV random permutations,\n"
                + "4 full-CSV references and 3 edge cases.\n"
                + "PASS: actual RankingTreeServer with actual Competitor CSV input: size=3166, height=42;\n"
                + "3166 output rows sorted by time, ranks=1..3166.\n";
        Files.writeString(Path.of("experiment/results/crosscheck.txt"), report, StandardCharsets.UTF_8);
        System.out.print(report);
    }
}
