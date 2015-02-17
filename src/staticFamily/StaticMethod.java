package staticFamily;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticMethod implements Serializable {

	private static final long serialVersionUID = 1L;

	private String declaration;
	private String signature;
	
	private byte localVariableCount = 0;
	
	private ArrayList<StaticStmt> smaliStmts = new ArrayList<StaticStmt>();
	private ArrayList<String> paramTypes = new ArrayList<String>();
	
	private ArrayList<Integer> sourceLineNumbers = new ArrayList<Integer>();
	
	private List<String> inCallSourceSigs = new ArrayList<String>();
	private List<String> outCallTargetSigs = new ArrayList<String>();
	private List<String> fieldRefSigs = new ArrayList<String>();
	
	// key: name of the dex local variable
	// value: corresponding name in debug info
	private Map<String, String> variableDebugInfo = new HashMap<String, String>();

	public String getDeclaration() {
		return declaration;
	}

	public void setDeclaration(String declaration) {
		this.declaration = declaration;
	}

	public String getDeclaringClass() {
		return signature.substring(0, signature.indexOf("->"));
	}
	
	public String getSubSignature() {
		return signature.substring(signature.indexOf("->")+2, signature.length());
	}
	
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public byte getLocalVariableCount() {
		return localVariableCount;
	}

	public void setLocalVariableCount(byte localVariableCount) {
		this.localVariableCount = localVariableCount;
	}

	public ArrayList<StaticStmt> getSmaliStmts() {
		return smaliStmts;
	}

	public void addSmaliStmt(StaticStmt smaliStmt) {
		this.smaliStmts.add(smaliStmt);
	}
	
	public void setSmaliStmts(ArrayList<StaticStmt> smaliStmts) {
		this.smaliStmts = smaliStmts;
	}

	public ArrayList<String> getParamTypes() {
		return paramTypes;
	}

	public void setParamTypes(ArrayList<String> paramTypes) {
		this.paramTypes = paramTypes;
	}

	public ArrayList<Integer> getSourceLineNumbers() {
		return sourceLineNumbers;
	}

	public void addSourceLineNumbers(Integer sourcelineNumber) {
		this.sourceLineNumbers.add(sourcelineNumber);
	}
	
	public void setSourceLineNumbers(ArrayList<Integer> sourceLineNumbers) {
		this.sourceLineNumbers = sourceLineNumbers;
	}

	public List<String> getInCallSourceSigs() {
		return inCallSourceSigs;
	}

	public void addInCallSourceSig(String inCallSourceSig) {
		if (!this.inCallSourceSigs.contains(inCallSourceSig))
			this.inCallSourceSigs.add(inCallSourceSig);
	}
	
	public void setInCallSourceSigs(List<String> inCallSourceSigs) {
		this.inCallSourceSigs = inCallSourceSigs;
	}

	public List<String> getOutCallTargetSigs() {
		return outCallTargetSigs;
	}

	public void addOutCallTargetSig(String outCallTargetSig) {
		this.outCallTargetSigs.add(outCallTargetSig);
	}
	
	public void setOutCallTargetSigs(List<String> outCallTargetSigs) {
		if (!this.outCallTargetSigs.contains(outCallTargetSigs))
			this.outCallTargetSigs = outCallTargetSigs;
	}

	public List<String> getFieldRefSigs() {
		return fieldRefSigs;
	}

	public void addFieldRefSig(String fieldRefSig) {
		if (!this.fieldRefSigs.contains(fieldRefSig))
			this.fieldRefSigs.add(fieldRefSig);
	}
	
	public void setFieldRefSigs(List<String> fieldRefSigs) {
		this.fieldRefSigs = fieldRefSigs;
	}

	public Map<String, String> getVariableDebugInfo() {
		return variableDebugInfo;
	}

	public void setVariableDebugInfo(Map<String, String> variableDebugInfo) {
		this.variableDebugInfo = variableDebugInfo;
	}

}
