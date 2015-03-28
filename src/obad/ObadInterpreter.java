package obad;

import java.util.ArrayList;
import java.util.List;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import staticFamily.StaticStmt;
import symbolic.Expression;
import analysis.StaticInfo;

public class ObadInterpreter {

	static StaticApp staticApp;
	
	public static void main(String[] args)
	{
		staticApp = StaticInfo.initAnalysis(ObadData.apkPath, false);
		for (StaticClass c : staticApp.getClasses())
		{
			for (StaticMethod m : c.getMethods())
			{
				for (StaticStmt s : m.getSmaliStmts())
				{
					if (s.invokesMethod() &&
							s.getSmaliStmt().endsWith(ObadData.encryptionMethodSig))
					{

						String targetSig = (String) s.getData();
						String[] params = new String[3];
						int[] intParams = new int[3];
						Expression ex = s.getExpression();
						// get the name of the parameter variables
						for (int i = 1; i < ex.getChildCount(); i++)
						{
							params[i-1] = ((Expression) ex.getChildAt(i)).getContent();
						}
						// go 3 statements back, find out the values of the 3 variables
						for (int i = 1; i < 4; i++)
						{
							StaticStmt prevS = m.getSmaliStmts().get(s.getStmtID()-i);
							Expression thisEx = prevS.getExpression();
							String left = ((Expression) thisEx.getChildAt(0)).getContent();
							Expression rightEx = (Expression) thisEx.getChildAt(1);
							if (rightEx.getContent().equals("$number"))
							{
								try
								{
									String hex = ((Expression)rightEx.getChildAt(1)).getContent();
									int value = Integer.parseInt(hex.replace("0x", ""), 16);
									if (left.equals(params[0]))
										intParams[0] = value;
									else if (left.equals(params[1]))
										intParams[1] = value;
									else if (left.equals(params[2]))
										intParams[2] = value;
								}
								catch (Exception e)
								{
									System.out.println(c.getJavaName() + ":" + s.getSourceLineNumber());
									e.printStackTrace();
								}
							}
						}
						StaticMethod om = staticApp.findMethod(targetSig);
						List<Register> regs = new ArrayList<Register>();
						int paramCount = om.isStatic()? 3:4;
						int localCount = om.getLocalVariableCount();
						// First, initiate parameters with concrete values
						for (int i = 0; i < paramCount; i++)
						{
							Register reg = new Register();
							reg.name = "p" + i;
							reg.value = intParams[i];
							regs.add(reg);
						}
						for (int i = 0; i < localCount; i++)
						{
							Register reg = new Register();
							reg.name = "v" + i;
							regs.add(reg);
						}
						interpret(om, regs);
					}
				}
			}
		}
	}

	
	static void interpret(StaticMethod m, List<Register> regs)
	{
		int stmtID = 0;
		while (true)
		{
			// Starts symbolic execution
			// possible statements:
			// add, const, move, int-to-byte
			// sget
			// new-instance
			// new-array
			// if
			// aput, aget
			StaticStmt s = m.getSmaliStmts().get(stmtID);
			stmtID++;
			if (s.endsMethod())
				break;
			if (s.ifJumps())
			{
				Expression ex = s.getExpression();
				Expression leftEx = (Expression) ex.getChildAt(0);
				Expression rightEx = (Expression) ex.getChildAt(1);
				String left = leftEx.getContent();
				String right = rightEx.getContent();
			}
			else if (s.getSmaliStmt().startsWith("sget"))
			{
				Expression ex = s.getExpression();
				Expression leftEx = (Expression) ex.getChildAt(0);
				String left = leftEx.getContent();
				String targetSig = (String) s.getData();
				String className = targetSig.substring(0, targetSig.indexOf("->"));
				String shortClassName = className.substring(className.lastIndexOf("/")+1, className.length()-1);
				int[] keys = Decryption.loadKeys(shortClassName);
				for (Register reg : regs)
				{
					if (reg.name.equals(left))
					{
						reg.isArray = true;
						reg.array = keys;
					}
				}
			}
			else if (s.getSmaliStmt().startsWith("new-instance"))
			{
				Expression ex = s.getExpression();
				Expression leftEx = (Expression) ex.getChildAt(0);
				Expression rightEx = (Expression) ex.getChildAt(1);
				String left = leftEx.getContent();
				String right = rightEx.getContent();
			}
			else if (s.getSmaliStmt().startsWith("new-array"))
			{
				Expression ex = s.getExpression();
				Expression leftEx = (Expression) ex.getChildAt(0);
				Expression rightEx = (Expression) ex.getChildAt(1);
				String left = leftEx.getContent();
				String right = rightEx.getContent();
			}
			else if (s.getSmaliStmt().startsWith("aput"))
			{
				Expression ex = s.getExpression();
				Expression leftEx = (Expression) ex.getChildAt(0);
				Expression rightEx = (Expression) ex.getChildAt(1);
				String left = leftEx.getContent();
				String right = rightEx.getContent();
			}
			else if (s.getSmaliStmt().startsWith("aget"))
			{
				Expression ex = s.getExpression();
				Expression leftEx = (Expression) ex.getChildAt(0);
				Expression rightEx = (Expression) ex.getChildAt(1);
				String left = leftEx.getContent();
				String right = rightEx.getContent();
			}
			else // add or assign
			{
				Expression ex = s.getExpression();
				Expression leftEx = (Expression) ex.getChildAt(0);
				Expression rightEx = (Expression) ex.getChildAt(1);
				String left = leftEx.getContent();
				String right = rightEx.getContent();
			}
		}
	}
	
}


class Register
{
	String name = "";
	boolean isArray = false;
	int value = 0;
	int[] array = null;
}
