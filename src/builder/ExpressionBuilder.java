package builder;

import staticFamily.StaticStmt;
import symbolic.Expression;

public class ExpressionBuilder {

	

	public static void buildExpression(StaticStmt s, String line)
	{
		String bytecodeOp = (line.contains(" "))?
				line.substring(0, line.indexOf(" ")) : line;
		int stmtIndex = 0;
		while (stmtIndex < 219)
		{
			if (bytecodeOp.equals(DalvikBytecodeFormat.smaliStatements[stmtIndex]))
				break;
			++stmtIndex;
		}
		if (stmtIndex >= 1 && stmtIndex <= 9)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 10 && stmtIndex <= 12)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 14 && stmtIndex <= 17)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 18 && stmtIndex <= 28)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex == 32)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex == 33)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex == 34)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 35 && stmtIndex <= 38)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex == 39)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 40 && stmtIndex <= 42)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex == 43 || stmtIndex == 44)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 45 && stmtIndex <= 49)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 50 && stmtIndex <= 55)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 56 && stmtIndex <= 61)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 62 && stmtIndex <= 68)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 69 && stmtIndex <= 75)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 76 && stmtIndex <= 82)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 83 && stmtIndex <= 89)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 90 && stmtIndex <= 96)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 97 && stmtIndex <= 103)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 104 && stmtIndex <= 114)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 115 && stmtIndex <= 120)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 121 && stmtIndex <= 135)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 136 && stmtIndex <= 167)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 168 && stmtIndex <= 199)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
		else if (stmtIndex >= 200 && stmtIndex <= 218)
		{
			Expression ex = new Expression("");
			s.setExpression(ex);
		}
	}
	
}
