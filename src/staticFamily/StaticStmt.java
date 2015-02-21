package staticFamily;

import java.util.HashMap;
import java.util.Map;

import symbolic.Expression;

public class StaticStmt {

	private String smaliStmt = "";
	private int sourceLineNumber = -1;
	private BlockLabel blockLabel;
	private String vA = "", vB = "", vC = "";
	private boolean hasCatch = false, hasFinally = false;
	private Map<String, String> ExceptionMap = new HashMap<String, String>();
	private boolean isSwitchStmt = false;
	/**
	 data is used differently by different statement types:
	 Switch: case switch map: Map<Integer, String>. (<value, jumpLabel>)
	 Invoke: method signature
	 **/
	private Object data;
	
	private Expression expression;
	
	/**
	 Only 1 of the following 5 boolean values can be true
	 	boolean endsMethod()
	 	boolean unconditionallyJumps()
	 	boolean conditionallyJumps()
	 	boolean updatesSymbolicStates();
	 	boolean invokesMethod()
	 **/
	public boolean endsMethod()
	{
		return (smaliStmt.startsWith("return") || 
				smaliStmt.startsWith("throw"));
	}
	public boolean unconditionallyJumps()
	{
		return (smaliStmt.startsWith("goto"));
	}
	public boolean conditionallyJumps()
	{
		return (smaliStmt.startsWith("if") || 
				smaliStmt.startsWith("packed-switch") ||
				smaliStmt.startsWith("sparse-switch"));
	}
	public boolean updatesSymbolicStates()
	{
		return (expression != null);
	}
	public boolean invokesMethod()
	{
		return (smaliStmt.startsWith("invoke-"));
	}
	
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

	public BlockLabel getBlockLabel() {
		return blockLabel;
	}

	public void setBlockLabel(BlockLabel label) {
		this.blockLabel = label;
	}

	public boolean hasCatch() {
		return hasCatch;
	}

	public void setHasCatch(boolean hasCatch) {
		this.hasCatch = hasCatch;
	}

	public boolean hasFinally() {
		return hasFinally;
	}

	public void setHasFinally(boolean hasFinally) {
		this.hasFinally = hasFinally;
	}

	public Map<String, String> getExceptionMap() {
		return ExceptionMap;
	}

	public void setExceptionMap(Map<String, String> exceptionMap) {
		ExceptionMap = exceptionMap;
	}
	
	public void putData(Object data)
	{
		this.data = data;
	}
	public boolean isSwitchStmt() {
		return isSwitchStmt;
	}
	public void setIsSwitchStmt(boolean isSwitchStmt) {
		this.isSwitchStmt = isSwitchStmt;
	}
	
}
