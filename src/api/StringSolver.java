package api;

import java.util.HashMap;
import java.util.Map;

import staticFamily.StaticStmt;
import symbolic.Expression;
import symbolic.Register;
import symbolic.SymbolicContext;

public class StringSolver {

	private static final String[] SB_signatures = {
		"Ljava/lang/StringBuilder;->append(",
		"Ljava/lang/StringBuilder;->toString()Ljava/lang/String;",
		"Ljava/lang/StringBuilder;-><init>()V",
		"Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V",
	};
	
	private static final String[] String_signatures = {
		"Ljava/lang/String;->equals(Ljava/lang/Object;)Z",
		"Ljava/lang/String;->length()I",
		"Ljava/lang/String;->substring(I)Ljava/lang/String;",
		"Ljava/lang/String;->substring(II)Ljava/lang/String;",
		"Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z",
	};
	
	private static final Map<String, String> sig_keyword_map = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("Ljava/lang/StringBuilder;->append(", "$string-append");
			put("Ljava/lang/String;->equals(Ljava/lang/Object;)Z", "$string-equals");
			put("Ljava/lang/String;->length()I", "$string-length");
			put("Ljava/lang/String;->substring(I)Ljava/lang/String;", "$string-substring");
			put("Ljava/lang/String;->substring(II)Ljava/lang/String;", "$string-substring");
			put("Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z", "$string-contains");
		}
	};
	
	private static final String oldKeyword = "$api";


	public static boolean isSolvableStringAPI(String invokeSig)
	{
		for (String ss : SB_signatures)
		{
			if (invokeSig.startsWith(ss))
				return true;
		}
		for (String ss : String_signatures)
		{
			if (invokeSig.startsWith(ss))
				return true;
		}
		return false;
	}

	public static void updateSymbolicStates(StaticStmt stmt, SymbolicContext symbolicContext)
	{
		String sig = (String) stmt.getData();
		if (sig.equals("Ljava/lang/StringBuilder;-><init>()V"))
		{
			Expression p0ValueEx = new Expression("\"\"");
			Expression p0Ex = (Expression) stmt.getExpression().getChildAt(1);
			Register reg = symbolicContext.findRegister(p0Ex.getContent());
			reg.ex = new Expression("=");
			reg.ex.add(p0Ex.clone());
			reg.ex.add(p0ValueEx.clone());
		}
		else if (sig.equals("Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V"))
		{
			Expression p0Ex = (Expression) stmt.getExpression().getChildAt(1);
			Expression p1Ex = (Expression) stmt.getExpression().getChildAt(2);
			Expression p1ValueEx = symbolicContext.findValueOf(p1Ex);
			Register reg = symbolicContext.findRegister(p0Ex.getContent());
			reg.ex = new Expression("=");
			reg.ex.add(p0Ex.clone());
			reg.ex.add(p1ValueEx.clone());
		}
		else if (sig.startsWith("Ljava/lang/StringBuilder;->append("))
		{
			Expression p0Ex = (Expression) stmt.getExpression().getChildAt(1);
			Expression p1Ex = (Expression) stmt.getExpression().getChildAt(2);
			Expression p0ValueEx = symbolicContext.findValueOf(p0Ex);
			Expression p1ValueEx = symbolicContext.findValueOf(p1Ex);
			
/*			if (sig.equals("Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;"))
			{
				// boolean although appears as 0 or 1 in bytecode, but when appended to string, it will appear as "false" or "true"
				if (p1ValueEx.getContent().equals("1"))
					p1ValueEx = new Expression("\"true\"");
				else if (p1ValueEx.getContent().equals("0"))
					p1ValueEx = new Expression("\"false\"");
			}*/
			
			Expression stmtEx = new Expression(oldKeyword);
			stmtEx.add(new Expression(stmt.getSmaliStmt()));
			stmtEx.add(p0ValueEx.clone());
			stmtEx.add(p1ValueEx.clone());
			Register reg = symbolicContext.findRegister(p0Ex.getContent());
			reg.ex = new Expression("=");
			reg.ex.add(p0Ex.clone());
			reg.ex.add(stmtEx);
		}
	}
	
	public static Expression generateResultExpression(StaticStmt stmt, SymbolicContext symbolicContext)
	{
		// signatures that do not need keywords:
		//		toString, init
		String sig = (String) stmt.getData();
		if (sig.equals("Ljava/lang/StringBuilder;-><init>()V"))
		{
			// do nothing, return empty Expression
			Expression p0ValueEx = new Expression("\"\"");
			Expression p0Ex = (Expression) stmt.getExpression().getChildAt(1);
			Register reg = symbolicContext.findRegister(p0Ex.getContent());
			reg.ex = new Expression("=");
			reg.ex.add(p0Ex.clone());
			reg.ex.add(p0ValueEx.clone());
			return p0ValueEx;
		}
		else if (sig.equals("Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V"))
		{
			Expression p0Ex = (Expression) stmt.getExpression().getChildAt(1);
			Expression p1Ex = (Expression) stmt.getExpression().getChildAt(2);
			Expression p1ValueEx = symbolicContext.findValueOf(p1Ex);
			Register reg = symbolicContext.findRegister(p0Ex.getContent());
			reg.ex = new Expression("=");
			reg.ex.add(p0Ex.clone());
			reg.ex.add(p1ValueEx.clone());
			return p1ValueEx.clone();
		}
		else if (sig.startsWith("Ljava/lang/StringBuilder;->toString("))
		{
			// return value of the 1 parameter, no need to put keyword, no need to update symbolic states
			Expression p0Ex = (Expression) stmt.getExpression().getChildAt(1);
			Expression p0ValueEx = symbolicContext.findValueOf(p0Ex);
			return p0ValueEx.clone();
		}
		else
		{	// other cases: get keyword, value of every parameter, return result
			String keyword = findKeyword(sig);
			Expression result = new Expression(keyword);
			for (int i = 1; i < stmt.getExpression().getChildCount(); i++)
			{
				Expression paramEx = (Expression) stmt.getExpression().getChildAt(i);
				Expression paramValueEx = symbolicContext.findValueOf(paramEx);
				if (!paramEx.equals(paramValueEx))
				{
					result.add(paramValueEx.clone());
				}
			}
			return result;
		}
	}
	
	private static String findKeyword(String sig)
	{
		for (Map.Entry<String, String> entry : sig_keyword_map.entrySet())
		{
			if (sig.startsWith(entry.getKey()))
				return entry.getValue();
		}
		return "";
	}

}
