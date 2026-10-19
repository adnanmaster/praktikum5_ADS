package ch.zhaw.ads;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @(#)TreeTest.java
 *
 *
 * @author K Rege
 * @version 1.00 2018/3/17
 * @version 1.01 2021/8/1
 */
public class ADS5_5_test {
    Tree<String> tree;

    @BeforeEach
    public void setUp() {
        tree = new SortedBinaryTree<>();
        tree.add("B");
        tree.add("A");
        tree.add("C");
        tree.add("D");
    }

    @Test
    public void testInterval() {
        char left = 'K';
        char right = 'O';

        for (int i = 0; i < 200; i++) {
            char c = (char) ('A' + (Math.random() * 26));
            tree.add(Character.toString(c));
        }
        // get all elements with inorder
        Visitor<String> v = new MyVisitor<>();
        tree.traversal().inorder(v);
        int count = 0;
        String s = v.toString();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= left && s.charAt(i) <= right) count++;
        }
        // now interval
        v = new MyVisitor<>();
        tree.traversal().interval(Character.toString(left), Character.toString(right), v);
        s = v.toString();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            assertTrue(c >= left && c <= right, c + " in interval "+left+" "+right);
        }
        assertEquals(count, s.length(), "size");
    }
}
