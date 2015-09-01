package api;

import staticFamily.StaticStmt;
import symbolic.Expression;
import symbolic.SymbolicContext;

public class APISolver {

	private static final String keyword = "$api";
	
	
	public static Expression generateResultExpression(StaticStmt stmt, SymbolicContext symbolicContext)
	{
		String invokeSig = (String) stmt.getData();
		if (StringSolver.isSolvableStringAPI(invokeSig))
		{
			return StringSolver.generateResultExpression(stmt, symbolicContext);
		}
		
		Expression result = new Expression(keyword);
		Expression invokeEx = stmt.getExpression();
		result.add(new Expression(stmt.getSmaliStmt()));
		//if (invokeSig)
		// Temp Invoke Expression Format:
		// root - invoke statement
		// children - Expression of each children in order
		Expression invokeStmtEx = stmt.getExpression();
		if (invokeStmtEx.getChildCount() > 1)
		{
			for (int i = 1; i < invokeStmtEx.getChildCount(); i++)
			{
				Expression paramEx = (Expression) invokeStmtEx.getChildAt(i);
				Expression paramValueEx = symbolicContext.findValueOf(paramEx);
				if (!paramEx.equals(paramValueEx))
				{
					result.add(paramValueEx.clone());
				}
			}
		}
		return result;
	}
	
}
