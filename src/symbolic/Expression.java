package symbolic;

import javax.swing.tree.DefaultMutableTreeNode;

public class Expression extends DefaultMutableTreeNode{

	private static final long serialVersionUID = 1L;
	
	public Expression(String root)
	{
		this.setUserObject(root);
	}
	
	public String getContent()
	{
		return this.getUserObject().toString();
	}
	
	public Expression clone()
	{
		Expression root = new Expression(this.getUserObject().toString());
		for(int i=0; i< this.getChildCount();i++){
			Expression subNode = (Expression) this.getChildAt(i);
			root.add(subNode.clone());
		}
		return root;
	}
	
	public String toYicesStatement()
	{
		if(this.isLeaf())
		{
			String result = this.getUserObject().toString();
			for(int index = 0; index < this.getChildCount(); index++)
			{
				Expression expr = (Expression) this.getChildAt(index);
				result += " "+expr.toYicesStatement();
			}
			return result;
		}
		String result = "("+this.getUserObject().toString();
		for(int index = 0; index < this.getChildCount(); index++)
		{
			Expression expr = (Expression) this.getChildAt(index);
			result += " "+expr.toYicesStatement();
		}
		return result + " )";
	}
	
	@Override
	public boolean equals(Object object){
		if( object instanceof Expression){
			Expression input = (Expression)object;
			String state1 = this.toYicesStatement();
			String state2 = input.toYicesStatement();
			boolean result = state1.equals(state2);
			return result;
		}
		return false;
	}
	
	
}
