import java.io.*;
import java.util.*;

// A node inside the tree has "max" as the biggest underneath it (maxinsubtree)
class Node {
    int max;
    Node left, right;

    Node(int max, Node left, Node right) {
        this.max = max;
        this.left = left;
        this.right = right;
    }
}

// PersistantArray is like versions/snapshots of all our trees
class PersistantArray {
    Node root;                  // null means empty array
    int height;

    PersistantArray(Node root, int height) {
        this.root = root;
        this.height = height;
    }
}

public class Main {

    // Value of node or -1 if there is no node
    static int maxOf(Node n) {
        if (n == null) return -1;
        return n.max;
    }

    static PersistantArray newArray() {
        return new PersistantArray(null, 0);
    }

    static PersistantArray set(PersistantArray a, int i, int value) {
        Node root = a.root;
        int height = a.height;

        // amount of index in tree: 0 to (2^height - 1). If i is bigger, the tree needs to grow, add a level on top (the old root becomes the left child of the new root)
        while ((i >> height) != 0) {
            root = new Node(maxOf(root), root, null);
            height++;
        }

        Node newRoot = setRecursion(root, i, value, height);
        return new PersistantArray(newRoot, height);
    }

    // wE go down the tree to place value at index i
    static Node setRecursion(Node node, int i, int value, int level) {
        if (level == 0) {                           // if we're in level 0 (bottom), we create a new leaf
            return new Node(value, null, null);
        }

        Node left = null;                           // temporary variables left and right, both null
        Node right = null;
        if (node != null) {
            left = node.left;
            right = node.right;
        }

        int bit = (i >> (level - 1)) & 1;
        if (bit == 0) {
            left = setRecursion(left, i, value, level - 1);
        } else {
            right = setRecursion(right, i, value, level - 1);
        }

        int newMax = Math.max(maxOf(left), maxOf(right));
        return new Node(newMax, left, right);
    }

    static int get(PersistantArray a, int i) {
        if ((i >> a.height) != 0) return 0;   // i is too big for this tree

        Node node = a.root;
        int level = a.height;
        while (node != null && level > 0) {
            int bit = (i >> (level - 1)) & 1;
            if (bit == 0) node = node.left;
            else node = node.right;             // choosing between left and right to reach level 0 and the leaf
            level--;
        }

        if (node == null) return 0;             // if the node is null (not set a value)
        return node.max;                        // othwerwise return value
    }

    static int maxininterval(PersistantArray a, int left, int right) {
        if (left > right) return 0;
        if ((left >> a.height) != 0) return 0;   // if interval is outside the tree

        // If right is too big, we isntead choose the biggest index we have in the tree
        if ((right >> a.height) != 0) {
            right = (1 << a.height) - 1;
        }

        int result = maxsegment(a.root, left, right, a.height);
        if (result == -1) return 0;             // if no node
        return result;
    }

    static int maxsegment(Node node, int left, int right, int level) {
        if (node == null) return -1;            // Fall 1 if empty node
        if (level == 0) return node.max;        // Fall 2 if we're at a leaf

        int leftBit = (left >> (level - 1)) & 1;
        int rightBit = (right >> (level - 1)) & 1;

        if (leftBit == 0 && rightBit == 0) {    // Fall 3, both nodes are in left subtree
            return maxsegment(node.left, left, right, level - 1);
        }
        if (leftBit == 1 && rightBit == 1) {    // Fall 4, both nodes are in right subtree
            return maxsegment(node.right, left, right, level - 1);
        }
        // Fall 5, left is in left subtree, right is in right subtree
        int a = maxrightsegment(node.left, left, level - 1);
        int b = maxleftsegment(node.right, right, level - 1);
        return Math.max(a, b);
    }

    static int maxrightsegment(Node node, int left, int level) {
        if (node == null) return -1;
        if (level == 0) return node.max;

        int bit = (left >> (level - 1)) & 1;
        if (bit == 0) {
            // 
            return Math.max(maxrightsegment(node.left, left, level - 1), maxOf(node.right));
        } else {
            // 
            return maxrightsegment(node.right, left, level - 1);
        }
    }

    // 
    static int maxleftsegment(Node node, int right, int level) {
        if (node == null) return -1;
        if (level == 0) return node.max;

        int bit = (right >> (level - 1)) & 1;
        if (bit == 1) {
            // 
            return Math.max(maxOf(node.left), maxleftsegment(node.right, right, level - 1));
        } else {
            // 
            return maxleftsegment(node.left, right, level - 1);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();

        // We store all versions/snapshots in this array lsit
        ArrayList<PersistantArray> versions = new ArrayList<>();
        versions.add(newArray());

        String text;
        while ((text = in.readLine()) != null) {
            String[] parts = text.trim().split(" ");
            PersistantArray current = versions.get(versions.size() - 1);

            if (parts[0].equals("set")) {
                int i = Integer.parseInt(parts[1]);
                int value = Integer.parseInt(parts[2]);
                versions.add(set(current, i, value));
            } else if (parts[0].equals("get")) {
                int i = Integer.parseInt(parts[1]);
                System.out.println(get(current, i));
            } else if (parts[0].equals("unset")) {
                if (versions.size() > 1) {
                    versions.remove(versions.size() - 1);
                }
            } else if (parts[0].equals("maxininterval")) {
                int left = Integer.parseInt(parts[1]);
                int right = Integer.parseInt(parts[2]);
                System.out.println(maxininterval(current, left, right));
            }
        }

        System.out.print(out);
    }
}