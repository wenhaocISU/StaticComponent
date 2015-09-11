package staticFamily;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import symbolic.Expression;

public class StaticClass implements Serializable {

	private static final long serialVersionUID = 1L;

	private String declaration = "";
	private String sourceFileName = "";
	
	private List<StaticField> fields = new ArrayList<StaticField>();
	private List<StaticMethod> methods = new ArrayList<StaticMethod>();
	public ArrayList<StaticField> tempFields = new ArrayList<StaticField>();
	
	private String superClass = "";
	private List<String> interfaces = new ArrayList<String>();
	
	private String outerClass = "";
	private List<String> innerClasses = new ArrayList<String>();
	
	private boolean isInnerClass, isDefinedInsideMethod;
	private boolean isActivity, isMainActivity;
	
	private Map<String, Integer> enumMap = null;
	
	public String getDeclaration() {
		return declaration;
	}
	
	public void setDeclaration(String declaration) {
		this.declaration = declaration;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public String getJavaName() {
		String result = declaration.substring(
				declaration.lastIndexOf(" ")+2, 
				declaration.length()-1);
		return result.replace("/", ".");
	}

	public String getDexName() {
		String result = declaration.substring(
				declaration.lastIndexOf(" ")+1, 
				declaration.length());
		return result;
	}

	public List<StaticField> getFields() {
		return fields;
	}
	
	public void addField(StaticField field) {
		this.fields.add(field);
	}

	public void setFields(List<StaticField> fields) {
		this.fields = fields;
	}

	public List<StaticMethod> getMethods() {
		return methods;
	}

	public void addMethod(StaticMethod method) {
		this.methods.add(method);
	}
	
	public void setMethods(List<StaticMethod> methods) {
		this.methods = methods;
	}

	public String getOuterClass() {
		return outerClass;
	}

	public void setOuterClass(String outerClass) {
		this.outerClass = outerClass;
	}

	public List<String> getInnerClasses() {
		return innerClasses;
	}

	public void addInnerClass(String innerClass) {
		if (!this.innerClasses.contains(innerClass))
			this.innerClasses.add(innerClass);
	}
	
	public void setInnerClasses(List<String> innerClasses) {
		this.innerClasses = innerClasses;
	}

	public boolean isInnerClass() {
		return isInnerClass;
	}

	public void setIsInnerClass(boolean isInnerClass) {
		this.isInnerClass = isInnerClass;
	}

	public boolean isActivity() {
		return isActivity;
	}

	public void setIsActivity(boolean isActivity) {
		this.isActivity = isActivity;
	}

	public boolean isMainActivity() {
		return isMainActivity;
	}

	public void setIsMainActivity(boolean isMainActivity) {
		this.isMainActivity = isMainActivity;
	}
	
	public boolean isEnum()
	{
		return declaration.contains(" enum ");
	}
	
	public Map<String, Integer> getEnumMap()
	{
		if (!isEnum())
			return null;
		if (this.enumMap == null)
		{
			this.enumMap = new HashMap<String, Integer>();
			StaticMethod clInitM = getMethod(getDexName() + "-><clinit>()V");
			Map<String, Integer> valueMap = new HashMap<String, Integer>();
			Map<String, String> fieldNameMap = new HashMap<String, String>();
			for (StaticStmt s : clInitM.getSmaliStmts())
			{
				String stmt = s.getSmaliStmt();
				if (stmt.startsWith("const-string"))
				{
					String variableName = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
					String fieldName = stmt.substring(stmt.indexOf(", ")+2).replace("\"", "");
					fieldNameMap.put(variableName, fieldName);
				}
				else if (stmt.startsWith("const/4"))
				{
					String variableName = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
					String valueString = stmt.substring(stmt.indexOf(", ")+2);
					int value = Integer.parseInt(valueString.replace("0x", ""), 16);
					valueMap.put(variableName, value);
				}
				else if (stmt.startsWith("invoke-direct") && stmt.contains("<init>"))
				{
					Expression invokeEx = s.getExpression();
					if (invokeEx.getChildCount() == 4)
					{
						Expression fieldVariableEx = (Expression) invokeEx.getChildAt(2);
						Expression valueEx = (Expression) invokeEx.getChildAt(3);
						String fieldV = fieldVariableEx.getContent();
						String valueV = valueEx.getContent();
						String fieldName = fieldNameMap.get(fieldV);
						int value = valueMap.get(valueV);
						enumMap.put(fieldName, value);
					}
				}
			}
		}
		return enumMap;
	}
	
	public boolean isPublic() {
		return declaration.contains(" public ");
	}
	
	public boolean isPrivate() {
		return declaration.contains(" private ");
	}
	
	public boolean isInterface() {
		return declaration.contains(" interface ");
	}
	
	public boolean isFinal() {
		return declaration.contains(" final ");
	}
	
	public boolean isAbstract() {
		return declaration.contains(" abstract ");
	}
	
	public boolean isProtected() {
		return declaration.contains(" protected ");
	}

	public String getSuperClass() {
		return superClass;
	}

	public StaticClass getSuperClass(StaticApp staticApp)
	{
		return staticApp.findClassByJavaName(superClass);
	}
	
	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public void addInterface(String interfaceName) {
		if (!this.interfaces.contains(interfaceName))
			this.interfaces.add(interfaceName);
	}
	
	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
	}

	public boolean isDefinedInsideMethod() {
		return isDefinedInsideMethod;
	}

	public void setIsDefinedInsideMethod(boolean isDefinedInsideMethod) {
		this.isDefinedInsideMethod = isDefinedInsideMethod;
	}
	
	public StaticMethod getMethod(String methodSignature)
	{
		for (StaticMethod m : this.methods)
			if (m.getSignature().equals(methodSignature))
				return m;
		return null;
	}
	
	public StaticMethod getMethodBySubSig(String subSig)
	{
		for (StaticMethod m : this.methods)
			if (m.getSubSignature().equals(subSig))
				return m;
		return null;
	}
	
	public StaticField getField(String fieldSubSig)
	{
		for (StaticField f : this.fields)
			if (f.getSubSignature().equals(fieldSubSig))
				return f;
		return null;
	}
	
	public StaticMethod findMethodByLineNumber(int lineNumber)
	{
		for (StaticMethod m : this.methods)
		{
			if (m.getSourceLineNumbers().contains(lineNumber))
				return m;
		}
		return null;
	}
	
	
}
