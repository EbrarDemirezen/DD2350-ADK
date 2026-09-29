import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

// A node in our persistent tree
class Node {
    int maxVal; // Stores the maximum value in the subtree rooted at this node
    Node left;
    Node right;

    public Node(int maxVal, Node left, Node right) {
        this.maxVal = maxVal;
        this.left = left;
        this.right = right;
    }
}

// A wrapper class to represent a specific version of our array
class PersistentArray {
    Node root;
    int level; // The current height of the tree

    // Create a new empty array
    public PersistentArray() {
        this.root = null;
        this.level = 0;
    }

    // Used internally to create new versions
    private PersistentArray(Node root, int level) {
        this.root = root;
        this.level = level;
    }

    public PersistentArray set(int index, int value) {
        Node currentRoot = this.root;
        int currentLevel = this.level;

        // Find out if we need to grow the tree height
        if (currentLevel == 0) {
            while ((index >> currentLevel) != 0) {
                currentLevel++;
            }
        } else {
            // Grow tree upwards if index requires more bits
            while ((index >> currentLevel) != 0) {
                int rootMax = (currentRoot != null) ? currentRoot.maxVal : 0;
                currentRoot = new Node(rootMax, currentRoot, null);
                currentLevel++;
            }
        }

        // Perform the persistent recursive insertion
        Node newRoot = setRec(currentRoot, index, value, currentLevel);
        return new PersistentArray(newRoot, currentLevel);
    }

    private Node setRec(Node node, int index, int value, int currentLevel) {
        if (currentLevel == 0) {
            // We reached the leaf, create a new leaf with the value
            return new Node(value, null, null);
        }

        int bit = (index >> (currentLevel - 1)) & 1;
        Node leftChild = (node != null) ? node.left : null;
        Node rightChild = (node != null) ? node.right : null;

        Node newLeft = leftChild;
        Node newRight = rightChild;

        // Traverse down the correct path and get a new node back
        if (bit == 0) {
            newLeft = setRec(leftChild, index, value, currentLevel - 1);
        } else {
            newRight = setRec(rightChild, index, value, currentLevel - 1);
        }

        // Calculate max in this new subtree
        int leftMax = (newLeft != null) ? newLeft.maxVal : 0;
        int rightMax = (newRight != null) ? newRight.maxVal : 0;
        int newMax = Math.max(leftMax, rightMax);

        // Return a completely new Node linking to the new/old children
        return new Node(newMax, newLeft, newRight);
    }

    public int get(int index) {
        return getRec(this.root, index, this.level);
    }

    private int getRec(Node node, int index, int currentLevel) {
        if (node == null) {
            // The path doesn't exist, meaning no value was ever assigned here
            return 0; 
        }
        if (currentLevel == 0) {
            // We reached the leaf, return the value
            return node.maxVal; 
        }
        
        // Extract the bit at (currentLevel - 1)
        int bit = (index >> (currentLevel - 1)) & 1;
        
        if (bit == 0) {
            return getRec(node.left, index, currentLevel - 1);
        } else {
            return getRec(node.right, index, currentLevel - 1);
        }
    }

    public int maxInInterval(int left, int right) {
        if (left > right) return 0;

        // The maximum index our current tree can hold based on its level
        long maxIndex = (1L << this.level) - 1;
        
        if (left > maxIndex) {
            return 0; // The entire interval is outside our tree
        }
        if (right > maxIndex) {
            right = (int) maxIndex; // Clamp the right bound to our tree's max capacity
        }

        int max = maxsegment(this.root, left, right, this.level);
        return max == -1 ? 0 : max; // Replace the -1 "empty" marker with 0
    }

    private int maxsegment(Node node, int left, int right, int currentLevel) {
        // Case A: Empty subtree
        if (node == null) return -1;
        
        // Case B: Reached a leaf
        if (currentLevel == 0) return node.maxVal;

        int leftBit = (left >> (currentLevel - 1)) & 1;
        int rightBit = (right >> (currentLevel - 1)) & 1;

        // Case C: Both in left subtree
        if (leftBit == 0 && rightBit == 0) {
            return maxsegment(node.left, left, right, currentLevel - 1);
        }
        // Case D: Both in right subtree
        else if (leftBit == 1 && rightBit == 1) {
            return maxsegment(node.right, left, right, currentLevel - 1);
        }
        // Case E: Split interval
        else {
            int maxLeftPart = maxrightsegment(node.left, left, currentLevel - 1);
            int maxRightPart = maxleftsegment(node.right, right, currentLevel - 1);
            return Math.max(maxLeftPart, maxRightPart);
        }
    }

    private int maxrightsegment(Node node, int left, int currentLevel) {
        if (node == null) return -1;
        if (currentLevel == 0) return node.maxVal;

        int bit = (left >> (currentLevel - 1)) & 1;
        if (bit == 0) {
            // 'left' is in the left subtree. 
            // So ALL elements in the right subtree are >= 'left' and are valid!
            int leftMax = maxrightsegment(node.left, left, currentLevel - 1);
            int rightMax = (node.right != null) ? node.right.maxVal : -1;
            return Math.max(leftMax, rightMax);
        } else {
            // 'left' is in the right subtree. We can ignore the left subtree completely.
            return maxrightsegment(node.right, left, currentLevel - 1);
        }
    }

    private int maxleftsegment(Node node, int right, int currentLevel) {
        if (node == null) return -1;
        if (currentLevel == 0) return node.maxVal;

        int bit = (right >> (currentLevel - 1)) & 1;
        if (bit == 1) {
            // 'right' is in the right subtree.
            // So ALL elements in the left subtree are <= 'right' and are valid!
            int leftMax = (node.left != null) ? node.left.maxVal : -1;
            int rightMax = maxleftsegment(node.right, right, currentLevel - 1);
            return Math.max(leftMax, rightMax);
        } else {
            // 'right' is in the left subtree. We can ignore the right subtree completely.
            return maxleftsegment(node.left, right, currentLevel - 1);
        }
    }
}

public class Lab1 {
    public static void main(String[] args) throws IOException {
        // Using BufferedReader for fast I/O (important for Kattis)
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = null;
        
        // This is where the magic of persistence shines: 
        // We just keep a list of our roots!
        List<PersistentArray> history = new ArrayList<>();
        history.add(new PersistentArray()); 

        String line;
        while ((line = br.readLine()) != null) {
            st = new StringTokenizer(line);
            if (!st.hasMoreTokens()) continue;
            
            String command = st.nextToken();
            PersistentArray current = history.get(history.size() - 1);
            
            if (command.equals("set")) {
                int index = Integer.parseInt(st.nextToken());
                int value = Integer.parseInt(st.nextToken());
                history.add(current.set(index, value));
            } 
            else if (command.equals("get")) {
                int index = Integer.parseInt(st.nextToken());
                System.out.println(current.get(index));
            } 
            else if (command.equals("unset")) {
                if (history.size() > 1) {
                    history.remove(history.size() - 1);
                }
            } 
            else if (command.equals("maxininterval")) {
                int left = Integer.parseInt(st.nextToken());
                int right = Integer.parseInt(st.nextToken());
                System.out.println(current.maxInInterval(left, right));
            }
        }
    }
}
