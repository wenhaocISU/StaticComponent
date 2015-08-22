package analysis;

import java.util.ArrayList;

import staticFamily.StaticClass;
import staticFamily.StaticField;
import staticFamily.StaticMethod;

public class SpecialInstrumentation {

	static final String tempFieldName = "specialAPEXField_";
	
	public int add_println_beginning(StaticClass c, StaticMethod m, ArrayList<String> smaliCode, int insertLocation)
	{
	/**
	FORMAT:
	    Using 2 regular sized parameters
		   iput p1, p0, tempField1; (or sput p1, tempField1;)
		   iput p2, p0, tempField2; (or sput p2, tempField2;)
		   sget-object p1, Ljava/lang/System;->out:Ljava/io/PrintStream;
		   const-string p2, "METHOD_STARTING,Lcom/google/android/gms/internal/av;-><clinit>()V"
		   invoke-virtual {p1, p2}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
		   iget p1, p0, tempField1; (or sget p1, tempField1;)
		   iget p2, p0, tempField2; (or sget p2, tempField2;)
		OR (if we're using 2 registers of 1 wide type parameter):
		   iput-wide p1, p0, tempField1; (or sput p1, tempField1;)
		   sget-object p1, Ljava/lang/System;->out:Ljava/io/PrintStream;
		   const-string p2, "METHOD_STARTING,Lcom/google/android/gms/internal/av;-><clinit>()V"
		   invoke-virtual {p1, p2}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
		   iget-wide p1, p0, tempField1; (or sget p1, tempField1;)
	**/
		
		int linesAdded = 0;
		ArrayList<String> fieldsToAdd = new ArrayList<String>();
		
		// Step 1. select 2 registers from 2 (or 1) parameter
		String param1Type = m.getParamTypes().get(1);
		String param2Type = m.getParamTypes().get(2);
		int param1Index = 1, param2Index = 2;
		if (m.isStatic())	
		{
			param1Index = 0;
			param2Index = 1;
			param1Type = m.getParamTypes().get(0);
			param2Type = m.getParamTypes().get(1);
		}
		param1Type = Utility.javaToDexTypeName(param1Type);
		param2Type = Utility.javaToDexTypeName(param2Type);
		ArrayList<String> paramTypes = new ArrayList<String>();
		if (param1Type.equals("D") || param1Type.equals("J"))
		{
			paramTypes.add(param1Type);
		}
		else if (param2Type.equals("D") || param2Type.equals("J"))
		{
			param1Index++;
			param2Index++;
			paramTypes.add(param2Type);
		}
		else
		{
			paramTypes.add(param1Type);
			paramTypes.add(param2Type);
		}
		
		// Step 2. Find or Create Fields
		ArrayList<String> fieldSigs = new ArrayList<String>();
		boolean param1Found = false;
		for (StaticField tf : c.tempFields)
		{
			if (tf.getType().equals(paramTypes.get(0))  && (tf.isStatic()==m.isStatic()))
			{
				fieldSigs.add(c.getDexName() + "->" + tf.getSubSignature());
				param1Found = true;
				break;
			}
		}
		if (!param1Found)
		{
			String newFieldName = SpecialInstrumentation.tempFieldName + c.tempFields.size();
			String subSig = newFieldName + ":" + paramTypes.get(0);
			String declaration = m.isStatic()? ".field static " + subSig : ".field " + subSig;
			StaticField tf = new StaticField();
			tf.setDeclaration(declaration);
			tf.setDeclaringClass(c.getDexName());
			tf.setSubSignature(subSig);
			c.tempFields.add(tf);
			fieldSigs.add(c.getDexName() + "->" + tf.getSubSignature());
			fieldsToAdd.add(tf.getDeclaration());
		}
		if (paramTypes.size() > 1)
		{
			boolean param2Found = false;
			for (StaticField tf : c.tempFields)
			{
				if (tf.getType().equals(paramTypes.get(1)) 
						&& (tf.isStatic()==m.isStatic())
						&& (!fieldSigs.contains(c.getDexName() + "->" + tf.getSubSignature()))
					)
				{
					fieldSigs.add(c.getDexName() + "->" + tf.getSubSignature());
					param2Found = true;
					break;
				}
			}
			if (!param2Found)
			{
				String newFieldName = SpecialInstrumentation.tempFieldName + c.tempFields.size();
				String subSig = newFieldName + ":" + paramTypes.get(1);
				String declaration = m.isStatic()? ".field static " + subSig : ".field " + subSig;
				StaticField tf = new StaticField();
				tf.setDeclaration(declaration);
				tf.setDeclaringClass(c.getDexName());
				tf.setSubSignature(subSig);
				c.tempFields.add(tf);
				fieldSigs.add(c.getDexName() + "->" + tf.getSubSignature());
				fieldsToAdd.add(tf.getDeclaration());
			}
		}
		
		// Step 3. Generate put, println, get statements
		ArrayList<String> printStatements = new ArrayList<String>();
		printStatements.add("    sget-object p" + param1Index + ", Ljava/lang/System;->out:Ljava/io/PrintStream;");
		printStatements.add("    const-string p" + param2Index + ", \"METHOD_STARTING," + m.getSignature() + "\"");
		printStatements.add("    invoke-virtual {p" + param1Index + ", p" + param2Index + "}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V");
		
		ArrayList<String> putStatements = new ArrayList<String>();
		ArrayList<String> getStatements = new ArrayList<String>();
		String putFieldOp = getFieldOpKeyword(paramTypes.get(0));
		String getFieldOp = putFieldOp.replace("put", "get");
		String putLine1 = m.isStatic()? "    s"+putFieldOp + " p" + param1Index + ", " + fieldSigs.get(0)
									  : "    i"+putFieldOp + " p" + param1Index + ", p0, " + fieldSigs.get(0);
		putStatements.add(putLine1);
		String getLine1 = m.isStatic()? "    s"+getFieldOp + " p" + param1Index + ", " + fieldSigs.get(0)
				  					  : "    i"+getFieldOp + " p" + param1Index + ", p0, " + fieldSigs.get(0);
		getStatements.add(getLine1);
		if (paramTypes.size()>1)
		{
			putFieldOp = getFieldOpKeyword(paramTypes.get(1));
			getFieldOp = putFieldOp.replace("put", "get");
			String putLine2 = m.isStatic()? "    s"+putFieldOp + " p" + param2Index + ", " + fieldSigs.get(1)
										  : "    i"+putFieldOp + " p" + param2Index + ", p0, " + fieldSigs.get(1);
			putStatements.add(putLine2);
			String getLine2 = m.isStatic()? "    s"+getFieldOp + " p" + param2Index + ", " + fieldSigs.get(1)
										  : "    i"+getFieldOp + " p" + param2Index + ", p0, " + fieldSigs.get(1);
			getStatements.add(getLine2);
		}
		
		// Step 4. Insert statements
		for (int i = getStatements.size()-1; i >= 0; i--)
		{
			smaliCode.add(insertLocation+1, getStatements.get(i));
			linesAdded++;
		}
		for (int i = printStatements.size()-1; i >= 0; i--)
		{
			smaliCode.add(insertLocation+1, printStatements.get(i));
			linesAdded++;
		}
		for (int i = putStatements.size()-1; i >= 0; i--)
		{
			smaliCode.add(insertLocation+1, putStatements.get(i));
			linesAdded++;
		}
		
		// Step 5. Insert field declarations
		String fieldSign = m.isStatic()? "# static fields" : "# instance fields";
		int fieldInsertLoc = -1;
		boolean foundFieldSign = false;
		for (int i = 0; i < smaliCode.size(); i++)
		{
			String line = smaliCode.get(i);
			if (line.equals("# direct methods") || line.equals("# virtual methods"))
			{
				fieldInsertLoc = i;
				break;
			}
			if (line.equals(fieldSign))
			{
				fieldInsertLoc = i+1;
				foundFieldSign = true;
				break;
			}
		}
		if (!foundFieldSign)
		{
			smaliCode.add(fieldInsertLoc, "");
			smaliCode.add(fieldInsertLoc, "");
			smaliCode.add(fieldInsertLoc, fieldSign);
			linesAdded += 3;
			fieldInsertLoc++;
		}
		for (int i = fieldsToAdd.size()-1; i >= 0; i--)
		{
			smaliCode.add(fieldInsertLoc, "");
			smaliCode.add(fieldInsertLoc, fieldsToAdd.get(i));
			linesAdded += 2;
		}
		return linesAdded;
	}
	
	public int add_println_returning(StaticClass c, StaticMethod m, ArrayList<String> smaliCode, int index)
	{
		int linesAdded = 0;
		
		// 1. find suitable parameters
		String param1Type = m.getParamTypes().get(1);
		String param2Type = m.getParamTypes().get(2);
		int param1Index = 1, param2Index = 2;
		if (m.isStatic())	
		{
			param1Index = 0;
			param2Index = 1;
			param1Type = m.getParamTypes().get(0);
			param2Type = m.getParamTypes().get(1);
		}
		param1Type = Utility.javaToDexTypeName(param1Type);
		param2Type = Utility.javaToDexTypeName(param2Type);
		ArrayList<String> paramTypes = new ArrayList<String>();
		if (param1Type.equals("D") || param1Type.equals("J"))
		{
			paramTypes.add(param1Type);
		}
		else if (param2Type.equals("D") || param2Type.equals("J"))
		{
			param1Index++;
			param2Index++;
			paramTypes.add(param2Type);
		}
		else
		{
			paramTypes.add(param1Type);
			paramTypes.add(param2Type);
		}
		
		// 2. find fields. No need to create since they must've been created already
		ArrayList<String> fieldSigs = new ArrayList<String>();
		boolean param1Found = false;
		for (StaticField tf : c.tempFields)
		{
			if (tf.getType().equals(paramTypes.get(0))  && (tf.isStatic()==m.isStatic()))
			{
				fieldSigs.add(c.getDexName() + "->" + tf.getSubSignature());
				param1Found = true;
				break;
			}
		}
		if (!param1Found)
		{
			System.out.println("  [WARNING] Can't find suitable temp field when adding METHOD_RETURNING. This shouldn't happen!");
		}
		if (paramTypes.size() > 1)
		{
			boolean param2Found = false;
			for (StaticField tf : c.tempFields)
			{
				if (tf.getType().equals(paramTypes.get(1)) 
						&& (tf.isStatic()==m.isStatic())
						&& (!fieldSigs.contains(c.getDexName() + "->" + tf.getSubSignature()))
					)
				{
					fieldSigs.add(c.getDexName() + "->" + tf.getSubSignature());
					param2Found = true;
					break;
				}
			}
			if (!param2Found)
			{
				System.out.println("  [WARNING] Can't find suitable temp field when adding METHOD_RETURNING. This shouldn't happen!");
			}
		}
		
		// 3. generate statements
		ArrayList<String> printStatements = new ArrayList<String>();
		printStatements.add("    sget-object p" + param1Index + ", Ljava/lang/System;->out:Ljava/io/PrintStream;");
		printStatements.add("    const-string p" + param2Index + ", \"METHOD_RETURNING," + m.getSignature() + "\"");
		printStatements.add("    invoke-virtual {p" + param1Index + ", p" + param2Index + "}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V");
		
		ArrayList<String> putStatements = new ArrayList<String>();
		ArrayList<String> getStatements = new ArrayList<String>();
		String putFieldOp = getFieldOpKeyword(paramTypes.get(0));
		String getFieldOp = putFieldOp.replace("put", "get");
		String putLine1 = m.isStatic()? "    s"+putFieldOp + " p" + param1Index + ", " + fieldSigs.get(0)
									  : "    i"+putFieldOp + " p" + param1Index + ", p0, " + fieldSigs.get(0);
		putStatements.add(putLine1);
		String getLine1 = m.isStatic()? "    s"+getFieldOp + " p" + param1Index + ", " + fieldSigs.get(0)
				  					  : "    i"+getFieldOp + " p" + param1Index + ", p0, " + fieldSigs.get(0);
		getStatements.add(getLine1);
		if (paramTypes.size()>1)
		{
			putFieldOp = getFieldOpKeyword(paramTypes.get(1));
			getFieldOp = putFieldOp.replace("put", "get");
			String putLine2 = m.isStatic()? "    s"+putFieldOp + " p" + param2Index + ", " + fieldSigs.get(1)
										  : "    i"+putFieldOp + " p" + param2Index + ", p0, " + fieldSigs.get(1);
			putStatements.add(putLine2);
			String getLine2 = m.isStatic()? "    s"+getFieldOp + " p" + param2Index + ", " + fieldSigs.get(1)
										  : "    i"+getFieldOp + " p" + param2Index + ", p0, " + fieldSigs.get(1);
			getStatements.add(getLine2);
		}
		
		// 4. add statements
		ArrayList<String> statementsToAdd = new ArrayList<String>();
		statementsToAdd.addAll(putStatements);
		statementsToAdd.addAll(printStatements);
		statementsToAdd.addAll(getStatements);
		
		int insertLocation = index - 1;
		String tempLine = smaliCode.get(insertLocation);
		boolean moveLabel = false;
		ArrayList<String> labelLines = new ArrayList<String>();
		String labelLine = smaliCode.get(insertLocation-1);
		while (labelLine.startsWith("    :"))
		{
			labelLines.add(labelLine);
			moveLabel = true;
			smaliCode.remove(insertLocation-1);
			linesAdded--;
			insertLocation--;
			labelLine = smaliCode.get(insertLocation-1);
		}
		while (!tempLine.equals(""))
			tempLine = smaliCode.get(--insertLocation);
		smaliCode.add(insertLocation+1, "");
		linesAdded++;
		
		for (int i = statementsToAdd.size()-1; i >=0; i--)
		{
			smaliCode.add(insertLocation+1, statementsToAdd.get(i));
			linesAdded++;
		}
		
		if (moveLabel)
		{
			for (int i = 0; i < labelLines.size(); i++)
			{
				smaliCode.add(insertLocation+1, labelLine);
				linesAdded++;
			}
		}
		
		return linesAdded;
	}
	
	
	private String getFieldOpKeyword(String type)
	{
		if (type.equals("I") || type.equals("F"))
			return "put";
		if (type.equals("Z"))
			return "put-boolean";
		if (type.equals("B"))
			return "put-byte";
		if (type.equals("C"))
			return "put-char";
		if (type.equals("S"))
			return "put-short";
		if (type.equals("J") || type.equals("D"))
			return "put-wide";
		return "put-object";
	}
	
	
	
	
}
