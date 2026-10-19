package ch.zhaw.ads;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BinaryTreeEdgeCasesTest {
    @Test
    void emptyTreeDoesNotVisitAnything() {
        SortedBinaryTree<Integer> tree = new SortedBinaryTree<>();
        List<Integer> visited = new ArrayList<>();
        Traversal<Integer> traversal = tree.traversal();

        traversal.inorder(visited::add);
        traversal.preorder(visited::add);
        traversal.postorder(visited::add);
        traversal.levelorder(visited::add);
        traversal.interval(1, 10, visited::add);

        assertTrue(visited.isEmpty());
        assertEquals(0, tree.height());
        assertEquals(0, tree.size());
        assertTrue(tree.balanced());
        assertEquals("", tree.printTree());
    }

    @Test
    void traversalsUseTheirOwnOrderOnBothSubtrees() {
        Tree<Integer> tree = integerTree(4, 2, 1, 3, 6, 5, 7);
        List<Integer> visited = new ArrayList<>();

        tree.traversal().inorder(visited::add);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7), visited);
        visited.clear();
        tree.traversal().preorder(visited::add);
        assertEquals(Arrays.asList(4, 2, 1, 3, 6, 5, 7), visited);
        visited.clear();
        tree.traversal().postorder(visited::add);
        assertEquals(Arrays.asList(1, 3, 2, 5, 7, 6, 4), visited);
        visited.clear();
        tree.traversal().levelorder(visited::add);
        assertEquals(Arrays.asList(4, 2, 6, 1, 3, 5, 7), visited);
    }

    @Test
    void intervalIncludesBothBoundsAndEveryDuplicate() {
        Tree<Integer> tree = integerTree(5, 3, 7, 3, 5, 7, 2, 8);
        List<Integer> visited = new ArrayList<>();

        tree.traversal().interval(3, 7, visited::add);
        assertEquals(Arrays.asList(3, 3, 5, 5, 7, 7), visited);

        visited.clear();
        tree.traversal().interval(5, 5, visited::add);
        assertEquals(Arrays.asList(5, 5), visited);
    }

    @Test
    void disjointAndInvertedIntervalsAreEmpty() {
        Tree<Integer> tree = integerTree(2, 1, 3);
        List<Integer> visited = new ArrayList<>();

        tree.traversal().interval(4, 8, visited::add);
        tree.traversal().interval(-3, 0, visited::add);
        tree.traversal().interval(3, 1, visited::add);

        assertTrue(visited.isEmpty());
    }

    @Test
    void intervalSkipsUnrelatedSubtrees() {
        Tree<MeasuredValue> tree = new SortedBinaryTree<>();
        int[] insertionOrder = {4, 2, 6, 1, 3, 5, 7};
        for (int value : insertionOrder) {
            tree.add(new MeasuredValue(value));
        }
        MeasuredValue.comparedValues.clear();
        List<Integer> visited = new ArrayList<>();

        tree.traversal().interval(new MeasuredValue(7), new MeasuredValue(7),
                value -> visited.add(value.value));

        assertEquals(Arrays.asList(7), visited);
        assertFalse(MeasuredValue.comparedValues.contains(1));
        assertFalse(MeasuredValue.comparedValues.contains(2));
        assertFalse(MeasuredValue.comparedValues.contains(3));
        assertFalse(MeasuredValue.comparedValues.contains(5));
    }

    @Test
    void heightSizeAndBalanceHandleSingleNodesAndChains() {
        Tree<Integer> single = integerTree(5);
        assertEquals(1, single.height());
        assertEquals(1, single.size());
        assertTrue(single.balanced());

        Tree<Integer> chain = integerTree(1, 2, 3, 4);
        assertEquals(4, chain.height());
        assertEquals(4, chain.size());
        assertFalse(chain.balanced());

        Tree<Integer> duplicates = integerTree(5, 5, 5);
        assertEquals(3, duplicates.size());
        assertEquals(3, duplicates.height());
    }

    @Test
    void balanceIsCheckedAtEveryNode() {
        Tree<Integer> tree = integerTree(8, 4, 12, 2, 1, 10, 14);
        // The root differs in height by one, but node 4 is unbalanced.
        assertFalse(tree.balanced());
        assertTrue(integerTree(4, 2, 6, 1, 3, 5, 7).balanced());
    }

    @Test
    void rankingIgnoresBlankLinesAndTrimsCrLfInput() {
        RankingTreeServer server = new RankingTreeServer();
        Tree<Competitor> tree = server.createTree(
                "\r\n Langsam;02:31:14 \r\n\r\n Schnell ; 02:11:31\r\n");

        assertEquals(2, tree.size());
        String expected = "1 Schnell 02:11:31\n2 Langsam 02:31:14\n";
        assertEquals(expected, server.createSortedText(tree));
        assertEquals(expected, server.createSortedText(tree));
    }

    @Test
    void rankingKeepsCompetitorsWithEqualTimes() {
        RankingTreeServer server = new RankingTreeServer();
        Tree<Competitor> tree = server.createTree("Anna;02:00:00\nBeat;02:00:00\n");

        assertEquals(2, tree.size());
        assertEquals("1 Beat 02:00:00\n2 Anna 02:00:00\n", server.createSortedText(tree));
    }

    @Test
    void emptyRankingProducesEmptyText() {
        RankingTreeServer server = new RankingTreeServer();
        Tree<Competitor> tree = server.createTree("\r\n \r\n");
        assertTrue(tree.isEmpty());
        assertEquals("", server.createSortedText(tree));
    }

    @Test
    void malformedRankingHasAUsefulError() {
        RankingTreeServer server = new RankingTreeServer();
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> server.createTree("\nAnna ohne Zeit\n"));
        assertTrue(error.getMessage().contains("Zeile 2"));
        assertThrows(IllegalArgumentException.class,
                () -> server.createTree("Anna;02:75:00"));
    }

    private Tree<Integer> integerTree(int... values) {
        Tree<Integer> tree = new SortedBinaryTree<>();
        for (int value : values) {
            tree.add(value);
        }
        return tree;
    }

    private static class MeasuredValue implements Comparable<MeasuredValue> {
        static final List<Integer> comparedValues = new ArrayList<>();
        final int value;

        MeasuredValue(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(MeasuredValue other) {
            comparedValues.add(value);
            return Integer.compare(value, other.value);
        }
    }
}
