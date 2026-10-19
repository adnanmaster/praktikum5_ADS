package ch.zhaw.ads;

import java.util.ArrayDeque;
import java.util.Queue;

public class TreeTraversal<T extends Comparable<T>> implements Traversal<T> {
    private final TreeNode<T> root;

    public TreeTraversal(TreeNode<T> root) {
        this.root = root;
    }

    @Override
    public void inorder(Visitor<T> vis) {
        inorder(root, vis);
    }

    private void inorder(TreeNode<T> node, Visitor<T> vis) {
        if (node == null) {
            return;
        }

        inorder(node.left, vis);

        for (T value : node.values) {
            vis.visit(value);
        }

        inorder(node.right, vis);

    }

    @Override
    public void preorder(Visitor<T> vis) {
        preorder(root, vis);
    }

    private void preorder(TreeNode<T> node, Visitor<T> vis) {
        if (node == null) {
            return;
        }
        for (T value : node.values) {
            vis.visit(value);
        }
        preorder(node.left, vis);
        preorder(node.right, vis);
    }

    @Override
    public void postorder(Visitor<T> vis) {
        postorder(root, vis);
    }

    private void postorder(TreeNode<T> node, Visitor<T> vis) {
        if (node == null) {
            return;
        }
        postorder(node.left, vis);
        postorder(node.right, vis);
        for (T value : node.values) {
            vis.visit(value);
        }
    }

    @Override
    public void levelorder(Visitor<T> vis) {
        if (root == null) {
            return;
        }
        Queue<TreeNode<T>> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TreeNode<T> node = queue.remove();
            for (T value : node.values) {
                vis.visit(value);
            }
            if (node.left != null) {
                queue.add(node.left);
            }
            if (node.right != null) {
                queue.add(node.right);
            }
        }
    }

    @Override
    public void interval(T min, T max, Visitor<T> vis) {
        if (min.compareTo(max) > 0) {
            return;
        }
        interval(root, min, max, vis);
    }

    private void interval(TreeNode<T> node, T min, T max, Visitor<T> vis) {
        if (node == null) {
            return;
        }
        int comparedToMin = node.getValue().compareTo(min);
        int comparedToMax = node.getValue().compareTo(max);

        // Equal values can also occur in the left subtree.
        if (comparedToMin >= 0) {
            interval(node.left, min, max, vis);
        }
        if (comparedToMin >= 0 && comparedToMax <= 0) {
            for (T value : node.values) {
                vis.visit(value);
            }
        }
        // Values in the right subtree are strictly greater than this node.
        if (comparedToMax < 0) {
            interval(node.right, min, max, vis);
        }
    }
}
