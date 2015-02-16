package staticFamily;

import symbolic.Expression;

public class StaticStmt {

	private String smaliStmt = "";
	private int sourceLineNumber = -1;
	
	private String vA = "", vB = "", vC = "";
	
	private Expression expression;
	
	private boolean updatesSymbolicStates;
	private boolean updatesPathConditions;
	private boolean endsMethod;
	
	public String getSmaliStmt() {
		return smaliStmt;
	}
	
	public void setSmaliStmt(String smaliStmt) {
		this.smaliStmt = smaliStmt;
	}

	public int getSourceLineNumber() {
		return sourceLineNumber;
	}

	public void setSourceLineNumber(int sourceLineNumber) {
		this.sourceLineNumber = sourceLineNumber;
	}

	public String getvA() {
		return vA;
	}

	public void setvA(String vA) {
		this.vA = vA;
	}

	public String getvB() {
		return vB;
	}

	public void setvB(String vB) {
		this.vB = vB;
	}

	public String getvC() {
		return vC;
	}

	public void setvC(String vC) {
		this.vC = vC;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public boolean updatesSymbolicStates() {
		return updatesSymbolicStates;
	}

	public void setUpdatesSymbolicStates(boolean updatesSymbolicStates) {
		this.updatesSymbolicStates = updatesSymbolicStates;
	}

	public boolean updatesPathConditions() {
		return updatesPathConditions;
	}

	public void setUpdatesPathConditions(boolean updatesPathConditions) {
		this.updatesPathConditions = updatesPathConditions;
	}

	public boolean endsMethod() {
		return endsMethod;
	}

	public void setEndsMethod(boolean endsMethod) {
		this.endsMethod = endsMethod;
	}

	
}
