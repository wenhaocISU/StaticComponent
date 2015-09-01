package symbolic;

import java.util.ArrayList;


public class SymbolicContext {
	/** registers, including local variables and parameter references */
	ArrayList<Register> registers = new ArrayList<Register>();
	/** the expressions that might changes global context ($Fstatic) */
	ArrayList<Expression> outExs = new ArrayList<Expression>();
	/** the most recent method invocation result */
	Expression recentInvokeResult = null;
	/** store all the array assignments of each array into one Expression */
	ArrayList<ArrayForYices> arrays = new ArrayList<ArrayForYices>();
	public Expression findValueOf(Expression theLeft)
	{
		if (theLeft.getContent().equals("$number"))
		{
			Expression decEx = (Expression) theLeft.getChildAt(2);
			return decEx.clone();
		}
		for (Register reg : registers)
		{
			if (reg.regName.equals(theLeft.getContent()))
			{
				Expression value = new Expression("");
				try
				{
					value = (Expression) reg.ex.getChildAt(1);
				}
				catch (NullPointerException e)
				{
					
				}
				return value.clone();
			}
		}
		return theLeft;
	}
	public Expression findArrayExOfField(Expression fieldSig)
	{
		for (ArrayForYices aFY : arrays)
		{
			if (aFY.fieldEx.equals(fieldSig))
			{
				if (aFY.arrayEx != null)
					return aFY.arrayEx.clone();
				else return aFY.fieldEx.clone();
			}
		}
		return fieldSig;
	}
	public void printAll()
	{
		for (Register reg : registers)
		{
			System.out.println("[" + reg.regName + "]");
			if (reg.ex != null)
				System.out.println("ex: " + reg.ex.toYicesStatement());
			if (!reg.fieldExs.isEmpty())
			{
				System.out.println("fields: ");
				for (Expression fieldEx : reg.fieldExs)
					System.out.println("  " + fieldEx.toYicesStatement());
			}
		}
		for (Expression outEx : outExs)
		{
			System.out.println("[outEx]" + outEx.toYicesStatement());
		}
	}
	public Register findRegister(String name)
	{
		for (Register reg : registers)
			if (reg.regName.equals(name))
				return reg;
		return null;
	}
	public void updateOutExs(Expression newOutEx)
	{
		Expression newLeft = (Expression) newOutEx.getChildAt(0);
		for (Expression thisOutEx : outExs)
		{
			Expression thisLeft = (Expression) thisOutEx.getChildAt(0);
			if (thisLeft.equals(newLeft))
			{
				Expression newRight = (Expression) newOutEx.getChildAt(1);
				thisOutEx.remove(1);
				thisOutEx.insert(newRight.clone(), 1);
				return;
			}
		}
		outExs.add(newOutEx.clone());
	}
	public void addArrayForYices(ArrayForYices aFY_to_add)
	{
		for (int i = 0; i < arrays.size(); i++)
		{
			if (arrays.get(i).name.equals(aFY_to_add.name))
			{
				arrays.remove(i);
				arrays.add(aFY_to_add);
				return;
			}
		}
		arrays.add(aFY_to_add);
	}
}
