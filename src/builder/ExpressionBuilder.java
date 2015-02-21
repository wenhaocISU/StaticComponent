package builder;

import staticFamily.StaticStmt;
import symbolic.Expression;

public class ExpressionBuilder {

	private String[] smaliStatements = {
		"nop",
		"move",
		"move/from16",
		"move/16",
		"move-wide",
		"move-wide/from16",
		"move-wide/16",
		"move-object",
		"move-object/from16",
		"move-object/16",
		"move-result",
		"move-result-wide",
		"move-result-object",
		"move-exception",
		"return-void",
		"return",
		"return-wide",
		"return-object",
		"const/4",
		"const/16",
		"const",
		"const/high16",
		"const-wide/16",
		"const-wide/32",
		"const-wide",
		"const-wide/high16",
		"const-string",
		"const-string/jumbo",
		"const-class",
		"monitor-enter",
		"monitor-exit",
		"check-cast",
		"instance-of",
		"array-length",
		"new-instance",
		"new-array",
		"filled-new-array",
		"filled-new-array/range",
		"fill-array-data",
		"throw",
		"goto",
		"goto/16",
		"goto/32",
		"packed-switch",
		"sparse-switch",
		"cmpl-float",
		"cmpg-float",
		"cmpl-double",
		"cmpg-double",
		"cmp-long",
		"if-eq",
		"if-ne",
		"if-lt",
		"if-ge",
		"if-gt",
		"if-le",
		"if-eqz",
		"if-nez",
		"if-ltz",
		"if-gez",
		"if-gtz",
		"if-lez",
		"aget",
		"aget-wide",
		"aget-object",
		"aget-boolean",
		"aget-byte",
		"aget-char",
		"aget-short",
		"aput",
		"aput-wide",
		"aput-object",
		"aput-boolean",
		"aput-byte",
		"aput-char",
		"aput-short",
		"iget",
		"iget-wide",
		"iget-object",
		"iget-boolean",
		"iget-byte",
		"iget-char",
		"iget-short",
		"iput",
		"iput-wide",
		"iput-object",
		"iput-boolean",
		"iput-byte",
		"iput-char",
		"iput-short",
		"sget",
		"sget-wide",
		"sget-object",
		"sget-boolean",
		"sget-byte",
		"sget-char",
		"sget-short",
		"sput",
		"sput-wide",
		"sput-object",
		"sput-boolean",
		"sput-byte",
		"sput-char",
		"sput-short",
		"invoke-virtual",
		"invoke-super",
		"invoke-direct",
		"invoke-static",
		"invoke-interface",
		"invoke-virtual/range",
		"invoke-virtual/range",
		"invoke-super/range",
		"invoke-direct/range",
		"invoke-static/range",
		"invoke-interface/range",
		"neg-int",
		"not-int",
		"neg-long",
		"not-long",
		"neg-float",
		"neg-double",
		"int-to-long",
		"int-to-float",
		"int-to-double",
		"long-to-int",
		"long-to-float",
		"long-to-double",
		"float-to-int",
		"float-to-long",
		"float-to-double",
		"double-to-int",
		"double-to-long",
		"double-to-float",
		"int-to-byte",
		"int-to-char",
		"int-to-short",
		"add-int",
		"sub-int",
		"mul-int",
		"dev-int",
		"rem-int",
		"and-int",
		"or-int",
		"xor-int",
		"shl-int",
		"shr-int",
		"ushr-int",
		"add-long",
		"sub-long",
		"mul-long",
		"dev-long",
		"rem-long",
		"and-long",
		"or-long",
		"xor-long",
		"shl-long",
		"shr-long",
		"ushr-long",
		"add-float",
		"sub-float",
		"mul-float",
		"dev-float",
		"rem-float",
		"add-double",
		"sub-double",
		"mul-double",
		"dev-double",
		"rem-double",
		"add-int/2addr",
		"sub-int/2addr",
		"mul-int/2addr",
		"dev-int/2addr",
		"rem-int/2addr",
		"and-int/2addr",
		"or-int/2addr",
		"xor-int/2addr",
		"shl-int/2addr",
		"shr-int/2addr",
		"ushr-int/2addr",
		"add-long/2addr",
		"sub-long/2addr",
		"mul-long/2addr",
		"dev-long/2addr",
		"rem-long/2addr",
		"and-long/2addr",
		"or-long/2addr",
		"xor-long/2addr",
		"shl-long/2addr",
		"shr-long/2addr",
		"ushr-long/2addr",
		"add-float/2addr",
		"sub-float/2addr",
		"mul-float/2addr",
		"dev-float/2addr",
		"rem-float/2addr",
		"add-double/2addr",
		"sub-double/2addr",
		"mul-double/2addr",
		"dev-double/2addr",
		"rem-double/2addr",
		"add-int/lit16",
		"rsub-int/lit16",
		"mul-int/lit16",
		"dev-int/lit16",
		"rem-int/lit16",
		"and-int/lit16",
		"or-int/lit16",
		"xor-int/lit16",
		"add-int/lit8",
		"rsub-int/lit8",
		"mul-int/lit8",
		"dev-int/lit8",
		"rem-int/lit8",
		"and-int/lit8",
		"or-int/lit8",
		"xor-int/lit8",
		"shl-int/lit8",
		"shr-int/lit8",
		"ushr-int/lit8"
	};

	public void buildExpression(StaticStmt s, String line)
	{
		String bytecodeOp = (line.contains(" "))?
				line.substring(0, line.indexOf(" ")) : line;
		int stmtIndex = 0;
		while (stmtIndex < 219)
		{
			if (bytecodeOp.equals(smaliStatements[stmtIndex]))
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
