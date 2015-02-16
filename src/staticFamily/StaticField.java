package staticFamily;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StaticField implements Serializable {

	private static final long serialVersionUID = 1L;

	private String declaration = "";
	
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
		this.inCallSourceSigs.add(inCallSourceSig);
	}

	public void setInCallSourceSigs(List<String> inCallSourceSigs) {
		this.inCallSourceSigs = inCallSourceSigs;
	}
	
}
