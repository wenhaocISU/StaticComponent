package staticFamily;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StaticField implements Serializable {

	private static final long serialVersionUID = 1L;

	private String declaration = "";
	private String declaringClass = "";
	private String subSignature = "";
	private String initValue = "";
	
	private List<String> inCallSourceSigs = new ArrayList<String>();

	public String getDeclaration() {
		return declaration;
	}

	public void setDeclaration(String declaration) {
		this.declaration = declaration;
	}

	public List<String> getInCallSourceSigs() {
		return inCallSourceSigs;
	}
	
	public void addInCallSourceSigs(String inCallSourceSig) {
		if (!this.inCallSourceSigs.contains(inCallSourceSig))
			this.inCallSourceSigs.add(inCallSourceSig);
	}

	public void setInCallSourceSigs(List<String> inCallSourceSigs) {
		this.inCallSourceSigs = inCallSourceSigs;
	}

	public String getDeclaringClass() {
		return declaringClass;
	}

	public void setDeclaringClass(String declaringClass) {
		this.declaringClass = declaringClass;
	}

	public String getInitValue() {
		return initValue;
	}

	public void setInitValue(String initValue) {
		this.initValue = initValue;
	}

	public String getSubSignature() {
		return subSignature;
	}

	public void setSubSignature(String subSignature) {
		this.subSignature = subSignature;
	}
	
	public String getName() {
		return subSignature.substring(0, subSignature.indexOf(":"));
	}
	
	public String getType() {
		return subSignature.substring(
				subSignature.indexOf(":")+1, subSignature.length());
	}
	
	public boolean isPublic() {
		return declaration.contains(" public ");
	}
	
	public boolean isPrivate() {
		return declaration.contains(" private ");
	}
	
	public boolean isProtected() {
		return declaration.contains(" protected ");
	}
	
	public boolean isFinal() {
		return declaration.contains(" final ");
	}
	
	public boolean isStatic() {
		return declaration.contains(" static ");
	}
	
	public boolean isSynthetic() {
		return declaration.contains(" synthetic ");
	}
}

