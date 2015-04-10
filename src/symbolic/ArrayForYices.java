package symbolic;


public class ArrayForYices {

	public String name = "";
	public String type = "";
	private Expression arrayEx = null;
	
	public ArrayForYices(Expression ex)
	{
		Expression left = (Expression) ex.getChildAt(0);
		this.name = left.getContent();
		Expression arrayEx = (Expression) ex.getChildAt(1);
		Expression typeEx = (Expression) arrayEx.getChildAt(1);
		this.type = typeEx.getContent();
		initArray();
		if (arrayEx.getChildCount() > 2)
		{
			for (int i = 2; i < arrayEx.getChildCount(); i++)
			{
				Expression elementEx = (Expression) arrayEx.getChildAt(i);
				Expression indexEx = (Expression) elementEx.getChildAt(0);
				Expression valueEx = (Expression) elementEx.getChildAt(1);
				aput(indexEx, valueEx);
			}
		}
	}
 
	
	private void initArray()
	{
		arrayEx = new Expression("$array");
		arrayEx.add(new Expression(name));
		arrayEx.add(new Expression(type));
	}
	

	public Expression aget(Expression indexEx)
	{
		Expression right = new Expression("");
		right.add(this.arrayEx.clone());
		right.add(indexEx.clone());
		return right;
	}
	
	public void aput(Expression indexEx, Expression valueEx)
	{
		Expression realIndexEx = new Expression("");
		realIndexEx.add(indexEx.clone());
		Expression newArrayEx = new Expression("update");
		newArrayEx.add(arrayEx.clone());
		newArrayEx.add(realIndexEx);
		newArrayEx.add(valueEx.clone());
		arrayEx = newArrayEx.clone();
	}
	
}
