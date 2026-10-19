package ch.zhaw.ads;

/** interface of Traversal ADT */
public interface Traversal<T> {
    /** traverse elements of tree in preorder */
    void preorder(Visitor<T> vistor);
    /** traverse elements of tree in inorder */
    void inorder(Visitor<T> vistor);
    /** traverse elements of tree in postorder */
    void postorder(Visitor<T> vistor);
    /** traverse elements of tree in levelorder */
    void levelorder(Visitor<T> vistor);
    /** traverse elements of tree interval */
    void interval(T min, T max, Visitor<T> vistor);
}
