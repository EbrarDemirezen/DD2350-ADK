import java.io.*;
import java.util.*;

// One node in the tree. In a leaf, "max" is the stored value.
// In an inner node, "max" is the biggest value anywhere below it (maxinsubtree).
class Node {
    int max;
    Node left, right;

    Node(int max, Node left, Node right) {
        this.max = max;
        this.left = left;
        this.right = right;
    }
}

// One version of the array = a root and the height of the tree.
class PersistantArray {
    Node root;   // null means "nothing stored yet"
    int height;  // 0 means the root is a leaf

    PersistantArray(Node root, int height) {
        this.root = root;
        this.height = height;
    }
}

public class Main {

    // Biggest value in a node, or -1 if there is no node (-1 = "nothing here")
    static int maxOf(Node n) {
        if (n == null) return -1;
        return n.max;
    }

    static PersistantArray newarray() {
        return new PersistantArray(null, 0);
    }

    static PersistantArray set(PersistantArray a, int i, int value) {
        Node root = a.root;
        int height = a.height;

        // Does i fit in the tree? If not, add a level on top
        // (the old root becomes the left child of the new root).
        while ((i >> height) != 0) {
            root = new Node(maxOf(root), root, null);
            height++;
        }

        Node newRoot = setRec(root, i, value, height);
        return new PersistantArray(newRoot, height);
    }

    // Copies the nodes on the path to index i, everything else is shared.
    static Node setRec(Node node, int i, int value, int level) {
        if (level == 0) {
            return new Node(value, null, null);   // new leaf
        }

        Node left = null;
        Node right = null;
        if (node != null) {
            left = node.left;
            right = node.right;
        }

        int bit = (i >> (level - 1)) & 1;
        if (bit == 0) {
            left = setRec(left, i, value, level - 1);
        } else {
            right = setRec(right, i, value, level - 1);
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
            else node = node.right;
            level--;
        }

        if (node == null) return 0;   // never assigned
        return node.max;
    }

    static int maxininterval(PersistantArray a, int left, int right) {
        if (left > right) return 0;
        if ((left >> a.height) != 0) return 0;   // whole interval is outside the tree

        // If right is too big, shrink it to the biggest index the tree can hold
        // (all ones in the lowest "height" bits).
        if ((right >> a.height) != 0) {
            right = ~(-1 << a.height);
        }

        int result = maxsegment(a.root, left, right, a.height);
        if (result == -1) return 0;
        return result;
    }

    // Biggest value in indices left..right, inside the tree with this root.
    static int maxsegment(Node node, int left, int right, int level) {
        if (node == null) return -1;            // Case A
        if (level == 0) return node.max;        // Case B

        int leftBit = (left >> (level - 1)) & 1;
        int rightBit = (right >> (level - 1)) & 1;

        if (leftBit == 0 && rightBit == 0) {    // Case C
            return maxsegment(node.left, left, right, level - 1);
        }
        if (leftBit == 1 && rightBit == 1) {    // Case D
            return maxsegment(node.right, left, right, level - 1);
        }
        // Case E: left is in the left subtree, right is in the right subtree
        int a = maxrightsegment(node.left, left, level - 1);
        int b = maxleftsegment(node.right, right, level - 1);
        return Math.max(a, b);
    }

    // Biggest value at indices >= left inside this subtree.
    static int maxrightsegment(Node node, int left, int level) {
        if (node == null) return -1;
        if (level == 0) return node.max;

        int bit = (left >> (level - 1)) & 1;
        if (bit == 0) {
            // left is in the left subtree, so the WHOLE right subtree counts
            return Math.max(maxrightsegment(node.left, left, level - 1), maxOf(node.right));
        } else {
            // left is in the right subtree, the left subtree is too small to count
            return maxrightsegment(node.right, left, level - 1);
        }
    }

    // Biggest value at indices <= right inside this subtree.
    static int maxleftsegment(Node node, int right, int level) {
        if (node == null) return -1;
        if (level == 0) return node.max;

        int bit = (right >> (level - 1)) & 1;
        if (bit == 1) {
            // right is in the right subtree, so the WHOLE left subtree counts
            return Math.max(maxOf(node.left), maxleftsegment(node.right, right, level - 1));
        } else {
            // right is in the left subtree, the right subtree is too big to count
            return maxleftsegment(node.left, right, level - 1);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder out = new StringBuilder();

        // Every version ever made. The last one is the current one.
        ArrayList<PersistantArray> versions = new ArrayList<>();
        versions.add(newarray());

        String line;
        while ((line = in.readLine()) != null) {
            String[] parts = line.trim().split(" ");
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