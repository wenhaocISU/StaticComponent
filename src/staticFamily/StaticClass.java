package staticFamily;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StaticClass implements Serializable {

	private static final long serialVersionUID = 1L;

	private String declaration = "";
	private String sourceFileName = "";
	
	private List<StaticField> fields = new ArrayList<StaticField>();
	private List<StaticMethod> methods = new ArrayList<StaticMethod>();
	
	private String outerClass = "";
	private List<String> innerClasses = new ArrayList<String>();
	
	private boolean isInnerClass;
	private boolean isActivity, isMainActivity;
	
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

	public void setInnerClasses(List<String> innerClasses) {
		this.innerClasses = innerClasses;
	}

	public boolean isInnerClass() {
		return isInnerClass;
	}

	public void setInnerClass(boolean isInnerClass) {
		this.isInnerClass = isInnerClass;
	}

	public boolean isActivity() {
		return isActivity;
	}

	public void setActivity(boolean isActivity) {
		this.isActivity = isActivity;
	}

	public boolean isMainActivity() {
		return isMainActivity;
	}

	public void setMainActivity(boolean isMainActivity) {
		this.isMainActivity = isMainActivity;
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
	
	
}
