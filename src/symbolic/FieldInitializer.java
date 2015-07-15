package symbolic;

import java.util.ArrayList;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;

public class FieldInitializer {

	private Expression fieldEx;
	private String fieldSig;
	private byte flag;	// 'i' represents instance field, 's' represents static field
	private StaticApp staticApp;
	
	public FieldInitializer(
				Expression fieldEx, 
				String fieldSig, 
				byte flag, 
				StaticApp staticApp)
	{
		this.fieldEx = fieldEx;
		this.fieldSig = fieldSig;
		this.flag = flag;
		this.staticApp = staticApp;
	}
	
	public Expression findFieldInitValue()
	{
		// 1. find class name
		// 2. look for <init> or <clinit> based on the flag 'i' or 's'
		// 3. perform symbolic execution on that method
		// 4. from the path summaries, look for Expressions that has fieldSig as left
		// 	  if found, then return the right; otherwise return null;
		if (this.flag != 's' && this.flag !='i')
			return null;
		String className = fieldSig.substring(0, fieldSig.indexOf("->"));
		StaticClass c = staticApp.findClassByDexName(className);
		if (c == null)
			return null;
		String methodSubSig = (flag == 'i')? "<init>()V" : "<clinit>()V";
		StaticMethod m = c.getMethodBySubSig(methodSubSig);
		if (m == null)
			return null;
		SymbolicExecution sex = new SymbolicExecution(staticApp);
		ArrayList<PathSummary> psList = sex.doFullSymbolic(m.getSignature());
		if (psList.size() < 1)
			return null;
		// (NOTE): since this is only a temp solution, to avoid complications, here I make the assumption that there
		// are no branch statements in the <init> or <clinit>
		for (Expression ex : psList.get(0).getSymbolicStates())
		{
			Expression left = (Expression) ex.getChildAt(0);
			Expression right = (Expression) ex.getChildAt(1);
			if (left.equals(fieldEx))
				return right.clone();
		}
		
		return null;
	}
	
}
