package staticFamily;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import symbolic.Expression;

public class StaticStmt implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String smaliStmt = "";
	private int sourceLineNumber = -1;
	private int stmtID = -1;
	private BlockLabel blockLabel;
	private String vA = "", vB = "", vC = "";
	private boolean hasCatch = false, hasFinally = false;
	private Map<String, String> ExceptionMap = new HashMap<String, String>();
	private String finallyTargetLabel;
	private boolean isSwitchStmt = false;
	/**
	 data is used differently by different statement types:
	 Switch: case switch map: Map<Integer, String>. (<value, jumpLabel>)
	 Invoke: method signature
	 **/
	private Object data;
	
	private Expression expression = null;
	
	/**
	 Only 1 of the following 6 boolean values can be true
	 	boolean endsMethod()
	 	boolean ifJumps()
	 	boolean switchJumps()
	 	boolean gotoJumps()
	 	boolean updatesSymbolicStates();
	 	boolean invokesMethod()
	 **/
	public boolean endsMethod()
	{
		return (smaliStmt.startsWith("return") || 
				smaliStmt.startsWith("throw"));
	}
	public boolean gotoJumps()
	{
		return (smaliStmt.startsWith("goto"));
	}
	public boolean ifJumps()
	{
		return (smaliStmt.startsWith("if"));
	}
	public boolean switchJumps()
	{
		return (smaliStmt.startsWith("packed-switch") ||
				smaliStmt.startsWith("sparse-switch"));
	}
	public boolean updatesSymbolicStates()
	{
		return (expression != null);
	}
	public boolean invokesMethod()
	{
		return (smaliStmt.startsWith("invoke"));
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
		this.setData(data);
	}
	public boolean isSwitchStmt() {
		return isSwitchStmt;
	}
	public void setIsSwitchStmt(boolean isSwitchStmt) {
		this.isSwitchStmt = isSwitchStmt;
	}
	public String getFinallyTargetLabel() {
		return finallyTargetLabel;
	}
	public void setFinallyTargetLabel(String finallyTargetLabel) {
		this.finallyTargetLabel = finallyTargetLabel;
	}
	public int getStmtID()
	{
		return stmtID;
	}
	public void setStmtID(int stmtID)
	{
		this.stmtID = stmtID;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
}
