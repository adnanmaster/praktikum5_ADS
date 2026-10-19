import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Reproducible structural experiment for the supplied unbalanced binary tree.
 * Keys are parsed once into seconds. Each existing-node comparison counts once.
 * The insertion rule is exactly: key <= node.key -> left; otherwise -> right.
 * No rotations, duplicate aggregation or wall-clock timing are used.
 */
public final class BstExperiment {
    static final long SEED = 20261019L;
    static final int DEFAULT_TRIALS = 10_000;

    record Observation(int height, long comparisons) { }

    static final class Simulation {
        final int[] keys;
        final int[] left;
        final int[] right;

        Simulation(int capacity) {
            keys = new int[capacity];
            left = new int[capacity];
            right = new int[capacity];
        }

        Observation build(int[] order) {
            int height = 0;
            long comparisons = 0;
            for (int i = 0; i < order.length; i++) {
                keys[i] = order[i];
                left[i] = -1;
                right[i] = -1;
                int depth = 0;
                if (i > 0) {
                    int current = 0;
                    while (true) {
                        comparisons++;
                        depth++;
                        boolean toLeft = keys[i] <= keys[current];
                        int next = toLeft ? left[current] : right[current];
                        if (next == -1) {
                            if (toLeft) left[current] = i;
                            else right[current] = i;
                            break;
                        }
                        current = next;
                    }
                }
                height = Math.max(height, depth + 1);
            }
            return new Observation(height, comparisons);
        }
    }

    static int[] readSeconds(Path csv) throws IOException {
        List<Integer> values = new ArrayList<>();
        int lineNumber = 0;
        for (String raw : Files.readAllLines(csv, StandardCharsets.UTF_8)) {
            lineNumber++;
            String line = raw.replace("\uFEFF", "").trim();
            if (line.isEmpty()) continue;
            String[] fields = line.split(";", -1);
            if (fields.length != 2) {
                throw new IllegalArgumentException("Invalid CSV line " + lineNumber);
            }
            String[] parts = fields[1].trim().split(":", -1);
            if (parts.length != 3) throw new IllegalArgumentException("Invalid time");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            if (hours < 0 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59) {
                throw new IllegalArgumentException("Invalid time on line " + lineNumber);
            }
            values.add(hours * 3600 + minutes * 60 + seconds);
        }
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    // Each labelled row permutation has probability 1/n! under the PRNG model.
    static void shuffle(int[] values, Random random) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
    }

    static int medianFirst(int[] sorted, int lo, int hi, int[] order, int position) {
        if (lo > hi) return position;
        int middle = (lo + hi) >>> 1;
        // Equal keys must be in the left subtree under the supplied insert rule.
        while (middle < hi && sorted[middle + 1] == sorted[middle]) middle++;
        order[position++] = sorted[middle];
        position = medianFirst(sorted, lo, middle - 1, order, position);
        return medianFirst(sorted, middle + 1, hi, order, position);
    }

    private static void writeObservation(PrintWriter out, String id, Observation obs) {
        out.printf("%s,%d,%d%n", id, obs.height(), obs.comparisons());
    }

    static long enumerate(int[] values, int start, Simulation simulation,
                          PrintWriter out, long count) {
        if (start == values.length) {
            Observation obs = simulation.build(values);
            writeObservation(out, Long.toString(count), obs);
            return count + 1;
        }
        for (int i = start; i < values.length; i++) {
            int tmp = values[start]; values[start] = values[i]; values[i] = tmp;
            count = enumerate(values, start + 1, simulation, out, count);
            tmp = values[start]; values[start] = values[i]; values[i] = tmp;
        }
        return count;
    }

    public static void main(String[] args) throws IOException {
        Path input = args.length > 0 ? Path.of(args[0])
                : Path.of("src/ch/zhaw/ads/RangZuerich.csv");
        Path output = args.length > 1 ? Path.of(args[1]) : Path.of("experiment/results");
        int trials = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_TRIALS;
        Files.createDirectories(output);
        int[] original = readSeconds(input);
        if (original.length < 8) throw new IllegalArgumentException("Need at least 8 rows");
        Simulation simulation = new Simulation(original.length);
        int[] sorted = original.clone();
        Arrays.sort(sorted);
        int[] reversed = sorted.clone();
        for (int i = 0; i < reversed.length / 2; i++) {
            int tmp = reversed[i];
            reversed[i] = reversed[reversed.length - 1 - i];
            reversed[reversed.length - 1 - i] = tmp;
        }
        int[] median = new int[original.length];
        medianFirst(sorted, 0, sorted.length - 1, median, 0);
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(
                output.resolve("references.csv"), StandardCharsets.UTF_8))) {
            out.println("order,height,comparisons");
            writeObservation(out, "original", simulation.build(original));
            writeObservation(out, "ascending", simulation.build(sorted));
            writeObservation(out, "descending", simulation.build(reversed));
            writeObservation(out, "median_first", simulation.build(median));
        }

        Random random = new Random(SEED);
        int[] permutation = new int[original.length];
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(
                output.resolve("full_random.csv"), StandardCharsets.UTF_8))) {
            out.println("trial,height,comparisons");
            for (int trial = 0; trial < trials; trial++) {
                System.arraycopy(original, 0, permutation, 0, original.length);
                shuffle(permutation, random);
                writeObservation(out, Integer.toString(trial), simulation.build(permutation));
            }
        }

        int[] distinct = Arrays.stream(sorted).distinct().toArray();
        if (distinct.length < 8) throw new IllegalArgumentException("Need 8 different keys");
        int[] small = new int[8];
        for (int i = 0; i < small.length; i++) {
            small[i] = distinct[i * (distinct.length - 1) / (small.length - 1)];
        }
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(
                output.resolve("selected8.csv"), StandardCharsets.UTF_8))) {
            out.println("index,seconds,time");
            for (int i = 0; i < small.length; i++) {
                out.printf("%d,%d,%02d:%02d:%02d%n", i, small[i], small[i] / 3600,
                        small[i] / 60 % 60, small[i] % 60);
            }
        }
        long count;
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(
                output.resolve("exact8.csv"), StandardCharsets.UTF_8))) {
            out.println("permutation,height,comparisons");
            count = enumerate(small, 0, new Simulation(8), out, 0);
        }
        if (count != 40_320) throw new AssertionError("Incorrect enumeration count");

        Random scalingRandom = new Random(SEED + 1);
        int[] sizes = {64, 128, 256, 512, 1024, 2048, original.length};
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(
                output.resolve("scaling.csv"), StandardCharsets.UTF_8))) {
            out.println("n,trial,height,comparisons");
            for (int n : sizes) {
                int[] subset = Arrays.copyOf(original, n);
                for (int trial = 0; trial < 100; trial++) {
                    permutation = subset.clone();
                    shuffle(permutation, scalingRandom);
                    Observation obs = simulation.build(permutation);
                    out.printf("%d,%d,%d,%d%n", n, trial, obs.height(), obs.comparisons());
                }
            }
        }
        System.out.printf("Completed: n=%d, trials=%d, exact8=%d, seed=%d%n",
                original.length, trials, count, SEED);
    }
}
