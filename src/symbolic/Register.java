package symbolic;

import java.util.ArrayList;

public class Register {
	public String regName = "";
	public String type = "";
	public Expression ex = null;
	public ArrayForYices aFY = null;
	public ArrayList<Expression> fieldExs = new ArrayList<Expression>();
	//boolean isFirstHalfOfWide = false;
	//boolean isSecondHalfOfWide = false;
	public boolean isReturnedVariable = false;
	public boolean isParam = false;
	public String originalParamName = "";
}