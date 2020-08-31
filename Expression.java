import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Expression implements Cloneable {

	// Consents for operation priority
	private static final Integer HIGH_PRIORITY = 3;
	private static final Integer MEDIUM_PRIORITY = 2;
	private static final Integer LOW_PRIORITY = 1;
	private static final Integer ZERO_PRIORITY = 0;

	// Constants for known functions and operators
	private static final String[] FUNCTIONS = { "sqrt", "sin", "cos", "tan", "asin", "acos", "atan", "ln", "max", "min", "log", "pi", "e" };
	private static final String[] SINGLE_OPERAND_FUNCTIONS = { "sin", "cos", "tan", "asin", "acos", "atan", "sqrt", "ln" };
	private static HashMap<String, Integer> PRECEDENCE_MAP = new HashMap<String, Integer>();
	static {
		PRECEDENCE_MAP.put("sqrt", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("ln", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("max", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("min", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("log", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("sin", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("cos", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("tan", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("asin", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("acos", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("atan", HIGH_PRIORITY);
		PRECEDENCE_MAP.put("^", MEDIUM_PRIORITY);
		PRECEDENCE_MAP.put("*", LOW_PRIORITY);
		PRECEDENCE_MAP.put("/", LOW_PRIORITY);
		PRECEDENCE_MAP.put("+", ZERO_PRIORITY);
		PRECEDENCE_MAP.put("-", ZERO_PRIORITY);
	}

	// A regular expression for numbers
	private static final String NUMBER_PATTERN = "-?[0-9]+(\\.[0-9]*)?";

	// A list for variables
	private LinkedHashSet<String> variables;

	// Storing the original expression and its infix version
	private String originalExpression;
	private Queue<String> postfixExpression;

	// The binary expression tree root used for evaluating the expression
	private Node<String> treeRoot;

	// A constructor to initialize a new expression
	public Expression(String expression) throws Exception {
		this.variables = new LinkedHashSet<String>();
		this.originalExpression = expression;
		this.postfixExpression = this.toPostfix(expression);
		this.treeRoot = this.buildTree(this.getPostfixExpression());
	}

	// A private constructor used for cloning
	private Expression(LinkedHashSet<String> variables, String originalExpression, Queue<String> postfixExpression, Node<String> treeRoot) throws CloneNotSupportedException {
		this.variables = variables;
		this.originalExpression = originalExpression;
		this.postfixExpression = postfixExpression;
		this.treeRoot = treeRoot.clone();
	}

	// Getters for the original expression and its postfix version
	public String getOriginalExpression() {
		return originalExpression;
	}

	public Queue<String> getPostfixExpression() {
		return postfixExpression;
	}

	// Getter for the list of variables
	public LinkedHashSet<String> getVariables() {
		return this.variables;
	}

	// Converting a expression to postfix using the shunting-yard algorithm
	// Source: https://en.wikipedia.org/wiki/Shunting-yard_algorithm
	private Queue<String> toPostfix(String expression) throws Exception {
		// Initialize the output queue and the operator stack
		Queue<String> postfix = new LinkedList<String>();
		Stack<String> operators = new Stack<String>();
		// Create temporary variables for storing the current and previous char
		char prevChar = ' ', prefix = ' ';
		// Remove spaces from the expression to make it easier to parse
		expression = expression.replace(" ", "");
		outer_loop: for (int i = 0; i < expression.length(); i++) {
			char c = expression.charAt(i);
			// Check if the token is a negative
			if ((prevChar == ' ' || this.isOprator(prevChar + "")) && c == '-') {
				prefix = '-';
				continue;
			}
			// If the token is a number read it
			if (Character.isDigit(c)) {
				StringBuilder numberBuilder = new StringBuilder();
				// If its negative add a minus sign to it
				if (prefix != ' ') {
					numberBuilder.append(prefix);
					prefix = ' ';
				}
				// Read all digits of the number
				boolean notSeenDot = true;
				while (Character.isDigit(c) || (c == '.' && notSeenDot)) {
					numberBuilder.append(c);
					if (c == '.')
						notSeenDot = false;
					if (i + 1 < expression.length())
						c = expression.charAt(++i);
					else
						break;
				}
				postfix.add(numberBuilder.toString());
			}
			// Check if the current token is a function(sin, cos, etc...) or constant(e, pi,
			// etc...)
			for (String func : FUNCTIONS)
				if (expression.startsWith(func, i)) {
					if (prefix == '-') {
						postfix.add("-1");
						operators.add("*");
					}
					if (!func.equals("pi") && !func.equals("e"))
						operators.push(func);
					else if (func.equals("pi"))
						postfix.add(Math.PI + "");
					else if (func.equals("e"))
						postfix.add(Math.E + "");
					i += func.length() - 1;
					continue outer_loop;
				}
			// Check if there's a variable and if yes read its name
			if (Character.isLetter(c)) {
				StringBuilder varName = new StringBuilder();
				// Add a minus sign if the variable has one
				if (prefix != ' ') {
					varName.append(prefix);
					prefix = ' ';
				}
				// Read all of the variables name
				while (Character.isLetter(c) || Character.isDigit(c)) {
					varName.append(c);
					if (i + 1 < expression.length())
						c = expression.charAt(++i);
					else
						break;
				}
				postfix.add(varName.toString());
				this.variables.add(varName.toString());
			}
			// Check if it's a operator
			if (PRECEDENCE_MAP.containsKey(c + "")) {
				while (!operators.isEmpty() && !operators.peek().equals("(")
						&& (PRECEDENCE_MAP.get(c + "") < PRECEDENCE_MAP.get(operators.peek())
								|| (PRECEDENCE_MAP.get(c + "") == PRECEDENCE_MAP.get(operators.peek()) && c != '^')))
					postfix.add(operators.pop());
				operators.add(c + "");
				// Check if it's left parenthesis
			} else if (c == '(')
				operators.add("(");
			// Check if it's right parenthesis
			else if (c == ')') {
				while (!operators.peek().equals("("))
					postfix.add(operators.pop());
				if (operators.peek().equals("("))
					operators.pop();
			}
		}
		// If any operators are left put them all in the output
		while (!operators.isEmpty()) {
			if (operators.peek().equals(")") || operators.peek().equals("("))
				throw new Exception("Mismatched parenthesis!");
			postfix.add(operators.pop());
		}
		return postfix;
	}

	// Checks if the token sent is a operator
	private boolean isOprator(String token) {
		return PRECEDENCE_MAP.containsKey(token);
	}

	// Builds the expression tree used for evaluation
	private Node<String> buildTree(Queue<String> postfixExpression) {
		// A stack of nodes to buffer tree building
		Stack<Node<String>> bufferStack = new Stack<Node<String>>();
		// We loop over each token in the postfix expression
		for (String token : postfixExpression) {
			// If its a number we create a new node containing it and push it to the stack
			if (token.matches(NUMBER_PATTERN) || this.variables.contains(token))
				bufferStack.push(new Node<String>(token));
			// If it a operator
			if (this.isOprator(token)) {
				// If it needs a single value
				if (this.isSingleValueOperator(token))
					bufferStack.push(new Node<String>(token, bufferStack.pop(), null));
				// If it needs two
				else
					bufferStack.push(new Node<String>(token, bufferStack.pop(), bufferStack.pop()));
			}
		}
		// Then we get the tree root
		return bufferStack.pop();
	}

	// Evaluation method for the expression tree to solve the expression
	public Double evaluate() throws Exception {
		// We check if we have variables and we were'nt given values for them we throw
		// an error
		// Because we can't complete the evaluation
		if (!this.variables.isEmpty())
			throw new Exception("Missing value for variables!: " + this.variables);
		return this.evaluate(this.treeRoot, new HashMap<String, Double>());
	}

	public Double evaluate(Double... variableValues) throws Exception {
		// We map out each variable to its corresponding value by the order
		// The values are given, and by the order the variables were discovered
		// e.g: for x + y + z
		// The first given value will go to x
		// The second for y
		// The third for z
		// And so on
		HashMap<String, Double> variablesValueMap = new HashMap<String, Double>();
		Iterator<String> variableIterator = this.variables.iterator();
		for (int i = 0; i < variableValues.length && variableIterator.hasNext(); i++)
			variablesValueMap.put(variableIterator.next(), variableValues[i]);
		if (variableIterator.hasNext()) {
			String missingValueVars = "[";
			while (variableIterator.hasNext())
				missingValueVars += variableIterator.next() + ',';
			missingValueVars = missingValueVars.substring(0, missingValueVars.length() - 1) + "]";
			throw new Exception("Missing values for variables!: " + missingValueVars);
		}
		return this.evaluate(this.treeRoot, variablesValueMap);
	}

	// The evaluation method we look at a node of the tree
	// If it contains a operator we execute the operator on it's children
	// If it contains a number we return that number
	// If it contains a variable we return it's value
	private Double evaluate(Node<String> ptr, HashMap<String, Double> variableValues) {
		// The current node's contents
		String data = ptr.getData();
		// Check if it's a number
		if (data.matches(NUMBER_PATTERN))
			return Double.parseDouble(ptr.getData());
		// Check if it's a variable
		if (variableValues.containsKey(data))
			return variableValues.get(data);
		// Check if it need's 1 or 2 values for the operator and execute it
		if (this.isSingleValueOperator(ptr.getData())) {
			Double value = this.evaluate(ptr.getRight(), variableValues);
			return this.doCalculation(data, value);
		} else {
			Double value = this.evaluate(ptr.getRight(), variableValues);
			Double value1 = this.evaluate(ptr.getLeft(), variableValues);
			return this.doCalculation(data, value, value1);
		}
	}

	// A method to check if a token is a single value operator
	private boolean isSingleValueOperator(String token) {
		for (String operation : SINGLE_OPERAND_FUNCTIONS)
			if (operation.equals(token))
				return true;
		return false;
	}

	// A utility function to do a calculation for a given operator and values
	private Double doCalculation(String operator, Double... values) {
		if (operator.equals("cos"))
			return Math.cos(values[0]);
		else if (operator.equals("sin"))
			return Math.sin(values[0]);
		else if (operator.equals("asin"))
			return Math.asin(values[0]);
		else if (operator.equals("cos"))
			return Math.cos(values[0]);
		else if (operator.equals("acos"))
			return Math.acos(values[0]);
		else if (operator.equals("tan"))
			return Math.tan(values[0]);
		else if (operator.equals("atan"))
			return Math.atan(values[0]);
		else if (operator.equals("sqrt"))
			return Math.sqrt(values[0]);
		else if (operator.equals("ln"))
			return Math.log10(values[0]) / Math.log10(Math.E);
		else if (operator.equals("log"))
			return Math.log10(values[0]) / Math.log10(values[1]);
		else if (operator.equals("max"))
			return Math.max(values[0], values[1]);
		else if (operator.equals("min"))
			return Math.min(values[0], values[1]);
		else if (operator.equals("+"))
			return values[0] + values[1];
		else if (operator.equals("-"))
			return values[0] - values[1];
		else if (operator.equals("/"))
			return values[0] / values[1];
		else if (operator.equals("*"))
			return values[0] * values[1];
		else if (operator.equals("^"))
			return Math.pow(values[0], values[1]);
		return null;
	}
	
	// A method to approximate the area under the expression graph in a given range 
	// Using a Riemann-Sum with a given interval, the smaller the interval the more accurate the result
	public Double approximateAreaUnderTheGraph(Double startPoint, Double endPoint, Double intreval) throws Exception {
		if (this.getVariables().size() > 1)
			throw new Exception("This method only works for single variable expresisons!");
		Double integralSum = 0.0;
		// We sum up the area of the rectangles that are approximately equal to the area under a small portion of the graph
		for (Double i = startPoint; i <= endPoint; i += intreval) {
			Double currentRectangleArea = this.evaluate(i) * intreval;
			if (!Double.isInfinite(currentRectangleArea))
				integralSum += Math.abs(currentRectangleArea);
		}
		return integralSum;
	}
	
	// A way to minimize parts of the expression to speed up the evaluation process and take up less space
	// For example: x + 5 + 4 * 1 will turn into x + 9
	// Returns a minimized expression
	public Expression minimizeExpression() throws Exception {
		Node<String> ptr = this.treeRoot.clone();
		this.minimizeExpression(ptr);
		// After the minimization we create a new Expression and return it
		String minimizedStr = this.reconstructExpressionFromTree(ptr);
		if (minimizedStr.charAt(0) == '(')
			minimizedStr = minimizedStr.substring(1, minimizedStr.length() - 1);
		return new Expression(this.getVariables(), minimizedStr, this.toPostfix(minimizedStr), ptr);
	}
	
	// The recursive implementation of the minimization process
	private void minimizeExpression(Node<String> ptr) {
		// Recurse left if possible
		if (ptr.hasLeft())
			this.minimizeExpression(ptr.getLeft());
		// Recurse right if possible
		if (ptr.hasRight())
			this.minimizeExpression(ptr.getRight());
		if (this.isSingleValueOperator(ptr.getData()) && ptr.getRight().getData().matches(NUMBER_PATTERN)) {
			Double rightValue = Double.parseDouble(ptr.getRight().getData());
			ptr.setData(this.doCalculation(ptr.getData(), rightValue) + "");
			ptr.setRight(null);
		}
		// Check if i have two children and that they are not operators
		if (ptr.hasLeft() && ptr.hasRight()) {
			// Check both are numbers
			String rightStr = ptr.getRight().getData();
			String leftStr = ptr.getLeft().getData();
			if (!rightStr.matches(NUMBER_PATTERN) || !leftStr.matches(NUMBER_PATTERN)) {
				// Checks if we have operator chaining for example 5 * x * 2 that should be 10 * x
				if (this.isOprator(ptr.getData())) {
					boolean modificationMade = false;
					// Checks for case one, right child is a number and we have a operator chain
					if (ptr.getData().equals(ptr.getLeft().getData()) && ptr.getRight().getData().matches(NUMBER_PATTERN)) {
						String leftLeftChild = ptr.getLeft().getLeft().getData();
						String leftRightChild = ptr.getLeft().getRight().getData();
						if (leftLeftChild.matches(NUMBER_PATTERN)) {
							Double val = Double.parseDouble(leftLeftChild);
							Double val1 = Double.parseDouble(ptr.getRight().getData());
							ptr.getLeft().getLeft().setData(this.doCalculation(ptr.getData(), val ,val1) + "");
							modificationMade = true;
						}
						else if (leftRightChild.matches(NUMBER_PATTERN)) {
							Double val = Double.parseDouble(leftRightChild);
							Double val1 = Double.parseDouble(ptr.getRight().getData());
							ptr.getLeft().getRight().setData(this.doCalculation(ptr.getData(), val ,val1) + "");
							modificationMade = true;
						}
						if (modificationMade) {
							ptr.setData(ptr.getLeft().getData());
							ptr.setRight(ptr.getLeft().getRight());
							ptr.setLeft(ptr.getLeft().getLeft());
						}
					}
					// Checks for case two, left child is a number and we have a operator chain
					else if (ptr.getData().equals(ptr.getRight().getData()) && ptr.getLeft().getData().matches(NUMBER_PATTERN)) {
						String rightLeftChild = ptr.getRight().getLeft().getData();
						String rightRightChild = ptr.getRight().getRight().getData();
						if (rightLeftChild.matches(NUMBER_PATTERN)) {
							Double val = Double.parseDouble(rightLeftChild);
							Double val1 = Double.parseDouble(ptr.getLeft().getData());
							ptr.getRight().getLeft().setData(this.doCalculation(ptr.getData(), val ,val1) + "");
							modificationMade = true;
						}
						else if (rightRightChild.matches(NUMBER_PATTERN)) {
							Double val = Double.parseDouble(rightRightChild);
							Double val1 = Double.parseDouble(ptr.getLeft().getData());
							ptr.getRight().getRight().setData(this.doCalculation(ptr.getData(), val ,val1) + "");
							modificationMade = true;
						}
						if (modificationMade) {
							ptr.setData(ptr.getRight().getData());
							ptr.setLeft(ptr.getRight().getLeft());
							ptr.setRight(ptr.getRight().getRight());
						}
					}
				}
				rightStr = ptr.getRight().getData();
				leftStr = ptr.getLeft().getData();
				if (ptr.getRight().isLeaf() || ptr.getLeft().isLeaf()) {
					// Check for zero's or one's because they are a special cases that allows us to ignore big parts of the expression
					// Check the right child for zero's
					boolean foundZero = false;
					if (rightStr.matches("-?0.0") || rightStr.equals("0")) {
						if (ptr.getData().equals("+") || ptr.getData().equals("-")) {
							ptr.setData(ptr.getLeft().getData());
							ptr.setRight(ptr.getLeft().getRight());
							ptr.setLeft(ptr.getLeft().getLeft());
							return;
						} else if (ptr.getData().equals("/"))
							ptr.setData("Infinity");
						foundZero = true;
					}
					// Check the right child for one's
					else if ((rightStr.equals("1.0") || rightStr.equals("1")) && (ptr.getData().equals("^") || ptr.getData().equals("*"))) {
						ptr.setData(ptr.getLeft().getData());
						ptr.setRight(ptr.getLeft().getRight());
						ptr.setLeft(ptr.getLeft().getLeft());
					}
					else if ((leftStr.equals("1.0") || leftStr.equals("1")) && ptr.getData().equals("*")) {
						ptr.setData(ptr.getRight().getData());
						ptr.setLeft(ptr.getRight().getLeft());
						ptr.setRight(ptr.getRight().getRight());
					}
					// Check the left child for one's
					else if ((leftStr.equals("1.0") || leftStr.equals("1")) && ptr.getData().equals("^")) {
						ptr.setData("1.0");
						ptr.setRight(null);
						ptr.setLeft(null);
					}
					// Check the left child for zero's
					if (leftStr.matches("-?0.0") || leftStr.equals("0")) {
						if (ptr.getData().equals("+") || ptr.getData().equals("-")) {
							ptr.setData(ptr.getRight().getData());
							ptr.setLeft(ptr.getRight().getLeft());
							ptr.setRight(ptr.getRight().getRight());
							return;
						} else if (ptr.getData().equals("/"))
							ptr.setData("0.0");
						foundZero = true;
					}
					// The general case of what to do if we see a zero
					if (foundZero) {
						if (ptr.getData().equals("*"))
							ptr.setData("0.0");
						ptr.setLeft(null);
						ptr.setRight(null);
						return;
					}
				}
			} 
			else {
				// If both are numbers we minimize by changing to node to the result of its
				// operation on it's children
				Double val1 = Double.valueOf(rightStr);
				Double val2 = Double.valueOf(leftStr);
				ptr.setData(this.doCalculation(ptr.getData(), val1, val2) + "");
				// Delete the children we minimized
				ptr.setLeft(null);
				ptr.setRight(null);
			}
		}
	}
	
	// A method to rebuild the original expression from the tree
	private String reconstructExpressionFromTree(Node<String> ptr) {
		// We check if the node is a leaf we return its data
		if (ptr.isLeaf())
			return ptr.getData();
		// Otherwise we recurse were possible
		else {
			String right = ptr.hasRight() ? this.reconstructExpressionFromTree(ptr.getRight()) : "";
			String left = ptr.hasLeft() ? this.reconstructExpressionFromTree(ptr.getLeft()) : "";
			// Check if extra brackets are needed
			if (this.isSingleValueOperator(ptr.getData()) && !right.contains(" "))
				right = "( " + right + " )";
			return "( " + left + " " + ptr.getData() + " " + right + " )";
		}
	}
	
	// A method to calculate the derivative of this expression
	public Expression findDerivative(String byWhatVar) throws Exception {
		Expression derivative  = new Expression(this.findDerivative(this.treeRoot, byWhatVar));
		return derivative.minimizeExpression();
	}
	
	public String findDerivativeString(String byWhatVar) throws Exception {
		return this.findDerivative(this.treeRoot, byWhatVar);
	}
	
	// We calculate the derivative by looking for known patterns and recursing
	private String findDerivative(Node<String> ptr, String byWhatVar) {
		// Check is its variable were deriving from
		if (ptr.getData().equals(byWhatVar))
			return "1.0";
		// Check if its a number or a variable were not deriving by
		if (ptr.getData().matches(NUMBER_PATTERN) || this.getVariables().contains(ptr.getData()))
			return "0.0";
		// Look for common patters f + g / f * g / f / g / cos(f) ....
		if (ptr.getData().equals("+") || ptr.getData().equals("-"))
			return "( " + this.findDerivative(ptr.getLeft(), byWhatVar) + ") " + ptr.getData() + " (" + this.findDerivative(ptr.getRight(), byWhatVar) + " )";
		String leftExp = ptr.hasLeft() ? this.reconstructExpressionFromTree(ptr.getLeft()) : "";
		String rightExp = ptr.hasRight() ? this.reconstructExpressionFromTree(ptr.getRight()) : "";
		if (ptr.getData().equals("^")) {
			if (ptr.getRight().getData().matches(NUMBER_PATTERN))
				return ptr.getRight().getData() + " * " + leftExp +" ^ " + (Double.parseDouble(ptr.getRight().getData()) - 1) + " * ( " + this.findDerivative(ptr.getLeft(), byWhatVar) + " )";
			else 
				return "( " + leftExp + " ^ " + rightExp + " ) * ( ( " + this.findDerivative(ptr.getLeft(), byWhatVar) + " / " + leftExp + " ) * " + rightExp + " + " + this.findDerivative(ptr.getRight(), byWhatVar) + " * ln( " + leftExp + " ) )";
		}
		if (ptr.getData().equals("*"))
			return "(" + this.findDerivative(ptr.getRight(), byWhatVar) + " ) * " + leftExp + " + ( " + this.findDerivative(ptr.getLeft(), byWhatVar) + " ) * " + rightExp;
		if (ptr.getData().equals("/"))
			return "( ( " + this.findDerivative(ptr.getRight(), byWhatVar) + " ) * " + leftExp + " - ( " + this.findDerivative(ptr.getLeft(), byWhatVar) + " ) * " + this.findDerivative(ptr.getRight(), byWhatVar) + ") / ( " + rightExp + " ^ 2 )";
		if (ptr.getData().equals("log"))
			return "( " + this.findDerivative(ptr.getLeft(), byWhatVar) + " ) / ( ( " + leftExp + " ) * ln( " + rightExp + " ) )";
		if (ptr.getData().equals("sin"))
			return "( " + this.findDerivative(ptr.getRight(), byWhatVar) + " ) * cos( " + rightExp + " ) ";
		if (ptr.getData().equals("asin"))
			return "( " + this.findDerivative(ptr.getRight(), byWhatVar) + " ) / sqrt( 1 - (" + rightExp + ") ^ 2 ) ";
		if (ptr.getData().equals("cos"))
			return "( " + this.findDerivative(ptr.getRight(), byWhatVar) + " ) * -1 * sin( " + rightExp + " ) ";
		if (ptr.getData().equals("acos"))
			return "-1 * ( " + this.findDerivative(ptr.getRight(), byWhatVar) + " ) / sqrt( 1 - (" + rightExp + ") ^ 2 ) ";
		if (ptr.getData().equals("tan"))
			return "( " + this.findDerivative(ptr.getRight(), byWhatVar) + " ) / ( cos( " + ptr.getRight() + " ) ^ 2 )";
		if (ptr.getData().equals("atan"))
			return "( " + this.findDerivative(ptr.getRight(), byWhatVar) + " ) / ( ( " + rightExp + " ^ 2 ) + 1 )";
		if (ptr.getData().equals("sqrt"))
			return "( " + this.findDerivative(ptr.getRight(), byWhatVar) + " ) / ( 2 * sqrt( " + rightExp + " ) )";
		if (ptr.getData().equals("ln"))
			return "( " + this.findDerivative(ptr.getRight(), byWhatVar) + " ) / ( " + rightExp + " )";
		return "";
	}

	@Override
	public int hashCode() {
		return this.getOriginalExpression().hashCode();
	}

	@Override
	protected Expression clone() throws CloneNotSupportedException {
		Expression newExp = new Expression(this.getVariables(), this.getOriginalExpression(), this.getPostfixExpression(), this.treeRoot);
		return newExp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Expression) {
			Expression otherExp = ((Expression) obj);
			return otherExp.reconstructExpressionFromTree(otherExp.treeRoot).equals(this.reconstructExpressionFromTree(this.treeRoot));
		} else
			return false;
	}

	@Override
	public String toString() {
		return this.treeRoot.toString();
	}

}
