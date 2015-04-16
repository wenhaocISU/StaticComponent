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
	
	private int localVariableCount = 0;
	
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
	
	public StaticClass getDeclaringClass(StaticApp staticApp)
	{
		return staticApp.findClassByDexName(this.getDeclaringClass());
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

	public int getLocalVariableCount() {
		return localVariableCount;
	}

	public void setLocalVariableCount(int localVariableCount) {
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

	public void setVariableDebugInfo(String localName, String debugName) {
		this.variableDebugInfo.put(localName, debugName);
	}

	public boolean isStatic()
	{
		return this.declaration.contains(" static ");
	}
	
	public boolean isAbstract()
	{
		return this.declaration.contains(" abstract ");
	}
	
	public boolean isNative()
	{
		return this.declaration.contains(" native ");
	}
	
	public boolean isPrivate()
	{
		return this.declaration.contains(" private ");
	}
	
	public boolean isProtected()
	{
		return this.declaration.contains(" protected ");
	}
	
	public StaticStmt getFirstStmtOfBlock(String label) {
		for (StaticStmt s : smaliStmts) {
			if (s.getBlockLabel().getNormalLabels().contains(label))
				return s;
		}
		return null;
	}

	public StaticStmt getStmtByLineNumber(int newHitLine)
	{
		for (StaticStmt s : this.smaliStmts)
			if (s.getSourceLineNumber() == newHitLine)
				return s;
		return null;
	}
}
