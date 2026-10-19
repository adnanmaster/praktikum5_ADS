package ch.zhaw.ads;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @(#)TreeTest.java
 *
 *
 * @author K Rege
 * @version 1.00 2018/3/17
 * @version 1.01 2021/8/1
 */
public class ADS5_2_test {
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
    public void testInorder() {
        Visitor<String> v = new MyVisitor<>();
        tree.traversal().inorder(v);
        assertEquals("ABCD", v.toString(), "inorder");
    }

    @Test
    public void testPreorder() {
        Visitor<String> v = new MyVisitor<>();
        tree.traversal().preorder(v);
        assertEquals("BACD", v.toString(), "preorder");
    }

    @Test
    public void testPostorder() {
        Visitor<String> v = new MyVisitor<>();
        tree.traversal().postorder(v);
        assertEquals("ADCB", v.toString(), "postorder");
    }

    @Test
    public void testLevelorder() {
        Visitor<String> v = new MyVisitor<>();
        tree.traversal().levelorder(v);
        assertEquals("BACD", v.toString(), "levelorder");
    }

    @Test
    public void testMixed() {
        Tree<String> tree = new SortedBinaryTree<>();
        List<String> list = new LinkedList<>();
        for (int i = 0; i < 1000; i++) {
            char c = (char) ('A' + (Math.random() * 26));
            int op = (int) (Math.random() * 2);
            switch (op) {
                case 0:
                    list.add(Character.toString(c));
                    tree.add(Character.toString(c));
                    break;
                case 1:
                    list.remove(Character.toString(c));
                    tree.remove(Character.toString(c));
                    break;
            }
        }
        Collections.sort(list);
        String expected = String.join("", list);
        Visitor<String> v = new MyVisitor<>();
        tree.traversal().inorder(v);
        assertEquals(expected, v.toString(), "mixed");
    }
}
