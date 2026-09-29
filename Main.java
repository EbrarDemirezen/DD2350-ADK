import java.util.ArrayDeque;

public class Main {

    static class Node {
        final Node left;
        final Node right;
        final int max;

        Node(Node left, Node right, int max) {
            this.left = left;
            this.right = right;
            this.max = max;
        }
    }

    static class Version {
        final Node root;
        final int level;

        Version(Node root, int level) {
            this.root = root;
            this.level = level;
        }
    }

    static int bit(int index, int bitNumber) {
        return (index >> bitNumber) & 1;
    }

    static int maxOf(Node node) {
        if (node == null) {
            return -1;
        }
        return node.max;
    }

    static int requiredLevel(int index) {
        if (index == 0) {
            return 1;
        }
        // Java integers have 32 bits
        return 32 - Integer.numberOfLeadingZeros(index);
    }

    
    public static void main(String[] args) {
        ArrayDeque<Version> versions = new ArrayDeque<>();

        // Version 0: empty array
        versions.push(new Version(null, 1));
    }
}