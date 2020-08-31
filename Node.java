
public class Node<T> implements Cloneable {
	
	private T data;
	private Node<T> right, left;
	
	private void initNode(T data, Node<T> right, Node<T> left) {
		this.data = data;
		this.right = right;
		this.left = left;
	}
	
	public Node(T data) {
		this.initNode(data, null, null);
	}
	
	public Node(T data, Node<T> right, Node<T> left) {
		this.initNode(data, right, left);
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Node<T> getRight() {
		return right;
	}

	public void setRight(Node<T> right) {
		this.right = right;
	}
	
	public boolean hasRight() {
		return this.right != null;
	}

	public Node<T> getLeft() {
		return left;
	}

	public void setLeft(Node<T> left) {
		this.left = left;
	}
	
	public boolean hasLeft() {
		return this.left != null;
	}
	
	public boolean isLeaf() {
		return !this.hasLeft() && !this.hasRight();
	}
	
	public <E> boolean isDataInstanceOf(Class<E> cls) {
		return cls.isInstance(this.getData());
	}
	
	@Override
	protected Node<String> clone() throws CloneNotSupportedException {
		Node<String> clonedNode = new Node<String>(this.getData().toString());
		if (this.hasLeft())
			clonedNode.setLeft(this.getLeft().clone());
		if (this.hasRight())
			clonedNode.setRight(this.getRight().clone());
		return clonedNode;
	}
	
	@Override
	public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        print(buffer, "", "");
        return buffer.toString();
    }

    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(this.getData());
        buffer.append('\n');
        if (this.hasLeft())
        	this.getLeft().print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
        if (this.hasRight())
        	this.getRight().print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
    }
	
}
