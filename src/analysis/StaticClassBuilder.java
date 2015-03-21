package analysis;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import staticFamily.BlockLabel;
import staticFamily.StaticClass;
import staticFamily.StaticField;
import staticFamily.StaticMethod;
import staticFamily.StaticStmt;
import symbolic.Blacklist;
import symbolic.Expression;

public class StaticClassBuilder implements Callable<StaticClass>{

	private ArrayList<String> smaliCode;
	private String smaliFilePath;
	private int maxOriginalLineNumber;
	private int index = 1;
	private MethodContext methodContext = new MethodContext();

	@Override
	public StaticClass call() throws Exception {
		StaticClass c = new StaticClass();
		/** Pre-method section:
	 	 	 1. Class attributes
	 	 	 2. Fields
		 **/
		if (smaliCode.size() > 0)
		{
			c.setDeclaration(smaliCode.get(0));
		}
		while (index < smaliCode.size())
		{
			String line = smaliCode.get(index++);
			if (line.equals("# direct methods") || line.equals("# virtual methods"))
				break;
			if (line.startsWith(".super "))
			{
				String superClassName = line.substring(line.lastIndexOf(" ")+1);
				c.setSuperClass(Utility.dexToJavaTypeName(superClassName));
			}
			else if (line.startsWith(".source \""))
			{
				String sourceName = line.substring(line.lastIndexOf(".source ")+8).replace("\"", "");
				c.setSourceFileName(sourceName);
			}
			else if (line.startsWith(".implements "))
			{
				String interfaceName = line.substring(line.indexOf(".implements ")+12);
				c.addInterface(interfaceName);
			}
			/**
			  'MemberClasses' annotation contains the names
			  of formally declared inner classes, they usually
			  have names, not '$1','$2', etc.
			  NOTE: MemberClasses does not contain the synthetic
			  inner classes
			 **/
			else if (line.startsWith(".annotation system Ldalvik/annotation/MemberClasses;"))
			{
				while (!line.equals(".end annotation") && index < smaliCode.size())
				{
					line = smaliCode.get(index++);
					if (line.startsWith("        ") && line.endsWith(";,"))
					{
						String memberClassName = line.substring(
								line.indexOf("L"), line.lastIndexOf(","));
						c.addInnerClass(Utility.dexToJavaTypeName(memberClassName));
					}
				}
			}
			/**
			  Every class that has 'InnerClass' annotation
			  must also have either 'EnclosingClass' or
			  'EnclosingMethod' annotation, which will specify
			  where this inner class is declared. (inside of 
			  a class, or inside of a method)
			**/
			else if (line.equals(".annotation system Ldalvik/annotation/InnerClass;"))
			{
				while (!line.equals(".end annotation") && index < smaliCode.size())
					line = smaliCode.get(index++);
				c.setIsInnerClass(true);
			}
			/** this is an inner class defined within a method **/
			else if (line.equals(".annotation system Ldalvik/annotation/EnclosingMethod;"))
			{
				while (!line.equals(".end annotation") && index < smaliCode.size())
				{
					line = smaliCode.get(index++);
					if (line.startsWith("    value = "))
					{
						String methodSig = line.substring(line.lastIndexOf(" = ")+3);
						String className = methodSig.substring(0, methodSig.indexOf("->"));
						c.setOuterClass(Utility.dexToJavaTypeName(className));
						c.setIsDefinedInsideMethod(true);
					}
				}
			}
			/** this is an inner class defined within a class **/
			else if (line.equals(".annotation system Ldalvik/annotation/EnclosingClass;"))
			{
				while (!line.equals(".end annotation") && index < smaliCode.size())
				{
					line = smaliCode.get(index++);
					if (line.startsWith("    value = "))
					{
						String className = line.substring(line.lastIndexOf(" = ")+3);
						c.setOuterClass(Utility.dexToJavaTypeName(className));
					}
				}
			}
			/** create a StaticField object **/
			else if (line.startsWith(".field "))
			{
				StaticField f = new StaticField();
				String subSig = line.substring(line.lastIndexOf(" ")+1);
				String initValue = "";
				if (line.contains(" = ")) {
					subSig = line.split(" = ")[0];
					subSig = subSig.substring(subSig.lastIndexOf(" ")+1);
					initValue = line.split(" = ")[1];
				}
				f.setDeclaration(line);
				f.setDeclaringClass(c.getJavaName());
				f.setInitValue(initValue);
				f.setSubSignature(subSig);
				c.addField(f);
			}
		}
		
		/** Parse Method Section **/
		while (index < smaliCode.size())
		{
			String line = smaliCode.get(index++);
			if (line.startsWith(".method "))
			{
				StaticMethod m = new StaticMethod();
				String subSig = line.substring(line.lastIndexOf(" ")+1);
				String fullSig = c.getDexName() + "->" + subSig;
				String params = subSig.substring(subSig.indexOf("(") + 1, subSig.indexOf(")"));
				m.setDeclaration(line);
				m.setSignature(fullSig);
				ArrayList<String> explicitParams = Utility.parseParameters(params);
				if (!m.isStatic()) // add implicit parameter 'this' if any
					explicitParams.add(0, c.getDexName());
				m.setParamTypes(explicitParams);
				methodContext.currentLineNumber = -1;
				methodContext.currentStmtID = 0;
				methodContext.label = new BlockLabel();
				methodContext.label.setNormalLabels(new ArrayList<String>(Arrays.asList(":main")));
				methodContext.normalLabelAlreadyUsed = false;
				while (!line.equals(".end method") && index < smaliCode.size())
				{
					line = smaliCode.get(index++);
					if (line.contains(" "))
						line = line.trim();
					if (line.equals("") || line.startsWith("#"))
						continue;
					if (line.startsWith("."))
					{
						parseDots(m, line);
					}
					else if (line.startsWith(":"))
					{
						parseColons(m, line);
					}
					else
					{
						StaticStmt s = buildStaticStmt(m, line);
						m.addSmaliStmt(s);
						/** Instrumentation Job 1:
						If current statement doesn't have a line number, we give
						it one. **/
						if (methodContext.currentLineNumber == -1)
						{
							String toAdd = "    .line " + this.maxOriginalLineNumber++;
							int insertLocation = index-1;
							String tempLine = smaliCode.get(insertLocation);
							while (!tempLine.equals("") && !tempLine.equals("    .prologue"))
								tempLine = smaliCode.get(--insertLocation);
							smaliCode.add(insertLocation+1, toAdd);
							++index;
							s.setSourceLineNumber(this.maxOriginalLineNumber-1);
						}
						else
						{
							s.setSourceLineNumber(methodContext.currentLineNumber);
							methodContext.currentLineNumber = -1;
						}
						/** Instrumentation Job 2:
						 If current statement is the first statement or
						 the return statement, we add println() to the code **/
						if (s.getStmtID() == 0 && 
							!Blacklist.classInBlackList(c.getJavaName()))
						{
							String line1 = "    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;";
							String line2 = "    const-string v1, \"METHOD_STARTING," + m.getSignature() + "\"";
							String line3 = "    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V";
							int insertLocation = index - 1;
							String tempLine = smaliCode.get(insertLocation);
							while (!tempLine.equals("") && !tempLine.equals("    .prologue"))
								tempLine = smaliCode.get(--insertLocation);
							smaliCode.add(insertLocation+1, "");
							smaliCode.add(insertLocation+1, line3);
							smaliCode.add(insertLocation+1, line2);
							smaliCode.add(insertLocation+1, line1);
							index += 4;
							if (m.getLocalVariableCount() < 2)
							{
								while (!tempLine.startsWith("    .locals "))
									tempLine = smaliCode.get(--insertLocation);
								smaliCode.set(insertLocation, "    .locals 2");
							}
						}
						else if (s.getSmaliStmt().startsWith("return") &&
								!Blacklist.classInBlackList(c.getJavaName()))
						{
							ArrayList<Integer> occupiedReg = new ArrayList<Integer>();
							int outVNo = 0, stringVNo = 0, returnVNo = -1;
							String returnVName = s.getvA();
							if (returnVName.startsWith("v"))
								returnVNo = Integer.parseInt(returnVName.replace("v", ""));
							occupiedReg.add(returnVNo);
							if (s.getSmaliStmt().startsWith("return-wide"))
								occupiedReg.add(returnVNo+1);
							boolean foundSlots = false;
							for(int i = 0; i < m.getLocalVariableCount()-1; i++)
							{
								if (!occupiedReg.contains(i) && !occupiedReg.contains(i+1))
								{
									foundSlots = true;
									outVNo = i;
									stringVNo = i + 1;
									break;
								}
							}
							if (!foundSlots)
							{
								outVNo = occupiedReg.get(occupiedReg.size()-1)+1;
								stringVNo = outVNo + 1;
							}
							String line1 = "    sget-object v" + outVNo + ", Ljava/lang/System;->out:Ljava/io/PrintStream;";
							String line2 = "    const-string v" + stringVNo + ", \"METHOD_RETURNING," + m.getSignature() + "\"";
							String line3 = "    invoke-virtual {v" + outVNo + ", v" + stringVNo + "}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V";
							int insertLocation = index - 1;
							String tempLine = smaliCode.get(insertLocation);
							while (!tempLine.equals(""))
								tempLine = smaliCode.get(--insertLocation);
							smaliCode.add(insertLocation+1, "");
							smaliCode.add(insertLocation+1, line3);
							smaliCode.add(insertLocation+1, line2);
							smaliCode.add(insertLocation+1, line1);
							index += 4;
							if (stringVNo >= m.getLocalVariableCount())
							{
								while (!tempLine.startsWith("    .locals "))
									tempLine = smaliCode.get(--insertLocation);
								smaliCode.set(insertLocation, "    .locals " + (stringVNo+1));
							}
						}
						/** Instrumentation Job 3:
						 For concolic execution, we need to let jdb recognize
						 the switch variable. **/
						if (s.isSwitchStmt())
						{
							String variableName = s.getvA();
							if (!m.getVariableDebugInfo().containsKey(variableName))
							{
								String debugName = "wenhao" + variableName;
								m.setVariableDebugInfo(variableName, debugName);
								String toAdd = "    .local " + variableName + 
										", \"" + debugName + "\":I";
								int insertionLocation = index - 1;
								smaliCode.add(insertionLocation, toAdd);
								++index;
							}
						}
					}
					c.addMethod(m);
				}
			}
		}
		/** Write instrumented code to file **/
		PrintWriter out = new PrintWriter(new FileWriter(smaliFilePath));
		for (String line: smaliCode)
			out.write(line + "\n");
		out.close();
		return c;
	}

	private void parseDots(StaticMethod m, String line)
	{
		if (line.startsWith(".line "))
		{
			/**
			   ran into a weird app that gave same line number to two
			   different statements. Therefore, if we meet a line number 
			   that has already been used, we remove this line number 
			   and will assign a new line number for the statement later.
			**/
			methodContext.currentLineNumber = Integer.parseInt(line.split(" ")[1]);
			if (m.getSourceLineNumbers().contains(methodContext.currentLineNumber))
			{
				smaliCode.remove(index-1);
				index--;
				methodContext.currentLineNumber = -1;
			}
			else
				m.addSourceLineNumbers(methodContext.currentLineNumber);
		}
		else if (line.startsWith(".catch "))
		{
			String range = line.substring(line.indexOf("{")+1, line.indexOf("}"));
			range = range.split(" .. ")[0];
			String tgtLabel = line.substring(line.lastIndexOf(" :")+1);
			String exceptionType = line.substring(line.indexOf(".catch ")+7, line.indexOf("; {"));
			for (StaticStmt s : m.getSmaliStmts())
			{
				if (!s.getBlockLabel().getTryLabels().contains(range))
					continue;
				s.setHasCatch(true);
				s.getExceptionMap().put(exceptionType, tgtLabel);
			}
		}
		else if (line.startsWith(".catchall "))
		{
			String range = line.substring(line.indexOf("{")+1, line.indexOf("}"));
			range = range.split(" .. ")[0];
			String tgtLabel = line.substring(line.lastIndexOf(" :")+1);
			for (StaticStmt s : m.getSmaliStmts())
			{
				if (!s.getBlockLabel().getTryLabels().contains(range))
					continue;
				s.hasFinally();
				s.setFinallyTargetLabel(tgtLabel);
			}
		}
		else if (line.startsWith(".locals "))
		{
			int localsCount = Integer.parseInt(line.split(" ")[1]);
			m.setLocalVariableCount(localsCount);
		}
		else if (line.startsWith(".annotation"))
		{
			while (!line.equals(".end annotation") && index < smaliCode.size())
			{
				line = smaliCode.get(index++);
				if (line.contains(" "))
					line = line.trim();
			}
		}
		else if (line.startsWith(".local "))
		{
			String localVariableName = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			String debugInfo = line.substring(line.indexOf(", ")+2);
			String debugVariableName = debugInfo.split(":")[0];
			m.setVariableDebugInfo(localVariableName, debugVariableName);
		}
		else if (line.startsWith(".param") && line.contains("\""))
		{
			String paramLocalName = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			String paramDebugName = line.substring(line.indexOf(", \"")+3);
			paramDebugName = paramDebugName.substring(0, paramDebugName.indexOf("\""));
			m.setVariableDebugInfo(paramLocalName, paramDebugName);
		}
	}
	
	private void parseColons(StaticMethod m, String line)
	{
		if (line.startsWith(":array_"))
		{
			String label = line;
			int arrayStmtID = methodContext.tableDataMap.get(label);
			StaticStmt s = m.getSmaliStmts().get(arrayStmtID);
			Expression ex = s.getExpression();
			Expression labelEx = (Expression) ex.getChildAt(1);
			if (!labelEx.getContent().equals(label))
				System.out.println("[ERROR] methodContext matched wrong fill-array-data statement with :array_ label");
			String lengthInfo = smaliCode.get(index++);
			int elementByteLength = Integer.parseInt(
					lengthInfo.substring(lengthInfo.lastIndexOf(" ")+1));
			int size = 0;
			String type = "";
			ArrayList<Expression> elementExList = new ArrayList<Expression>();
			while (index < smaliCode.size())
			{
				line = smaliCode.get(index++);
				if (line.contains(" "))
					line = line.trim();
				if (line.equals(".end array-data"))
					break;
				String hex = "", dec = "";
				switch (elementByteLength)
				{
					case 1: // value has suffix 't'
					{
						type = "char";
						hex = line;
						dec = Integer.parseInt(hex.replace("0x", "").replace("t", "")) + "";
						
						break;
					}
					case 2: // value has suffix 's'
					{
						type = "short";
						hex = line;
						dec = Integer.parseInt(hex.replace("0x", "").replace("s", "")) + "";
						
						break;
					}
					case 4: // int or float (distinguish by the # annotation)
					{
						if (line.contains("# "))
						{
							type = "float";
							hex = line.substring(0, line.indexOf(" "));
							dec = line.substring(line.indexOf("# ")+2);
						}
						else
						{
							type = "int";
							hex = line;
							dec = Integer.parseInt(hex.replace("0x", ""), 16) + "";
						}
						break;
					}
					case 8: // long or double (distinguish by the # annotation)
					{
						if (line.contains("# "))
						{
							type = "double";
							hex = line.substring(0, line.indexOf(" "));
							dec = line.substring(line.indexOf("# ")+2);
						}
						else
						{
							type = "long";
							hex = line;
							dec = Long.parseLong(hex.replace("0x", "").replace("L", "")) + "";
						}
						break;
					}
				}
				Expression eleEx = new Expression("$element");
				Expression indexEx = new Expression(size + "");
				Expression valueEx = new Expression("$number");
				valueEx.add(new Expression(type));
				valueEx.add(new Expression(hex));
				valueEx.add(new Expression(dec));
				eleEx.add(indexEx);
				eleEx.add(valueEx);
				elementExList.add(eleEx);
				size++;
			}
			Expression arrayEx = new Expression("$array");
			arrayEx.add(new Expression(size + ""));
			arrayEx.add(new Expression(type));
			for (Expression eleEx : elementExList)
				arrayEx.add(eleEx);
			ex.remove(1);
			ex.add(arrayEx);
		}
		else if (line.startsWith(":sswitch_data_"))
		{
			index++;
			StaticStmt s = m.getSmaliStmts().get(methodContext.tableDataMap.get(line));
			Map<Integer, String> switchMap = new HashMap<Integer, String>();
			while (index < smaliCode.size())
			{
				line = smaliCode.get(index++);
				if (line.contains(" "))
					line = line.trim();
				if (line.equals(".end sparse-switch"))
					break;
				String hexValue = line.substring(0, line.indexOf(" "));
				String caseTargetLabel = line.substring(line.indexOf("-> ")+3);
				int decValue = Integer.parseInt(hexValue.replace("0x", ""), 16);
				switchMap.put(decValue, caseTargetLabel);
			}
			s.setData(switchMap);
		}
		else if (line.startsWith(":pswitch_data_"))
		{
			String initValueInfo = smaliCode.get(index++);
			int switchValue = Integer.parseInt(
					initValueInfo.substring(initValueInfo.lastIndexOf(" ")+1).replace("0x", ""),
					16);
			StaticStmt s = m.getSmaliStmts().get(methodContext.tableDataMap.get(line));
			Map<Integer, String> switchMap = new HashMap<Integer, String>();
			while (index < smaliCode.size())
			{
				line = smaliCode.get(index++);
				if (line.contains(" "))
					line = line.trim();
				if (line.equals(".end packed-switch"))
					break;
				switchMap.put(switchValue++, line);
			}
			s.setData(switchMap);
		}
		else if (line.startsWith(":try_start_"))
		{
			methodContext.label.addTryLabel(line);
		}
		else if (line.startsWith(":try_end_"))
		{
			methodContext.label.getTryLabels().remove(
					line.replace("_end_", "_start_"));
		}
		else
		{	/** There's no naming pattern for normal label, 
				the default labels generated by compilers
				are :goto_1, :cond_1. However there is the
				possibility that the test app has been 
				instrumented with customized label names.
		 	**/
			if (methodContext.normalLabelAlreadyUsed)
			{
				ArrayList<String> newNormalLabel = new ArrayList<String>();
				newNormalLabel.add(line);
				methodContext.label.setNormalLabels(newNormalLabel);
				methodContext.label.setNormalLabelSection(0);
				methodContext.normalLabelAlreadyUsed = false;
			}
			else
			{
				methodContext.label.addNormalLabel(line);
			}
		}
	}
	
	private StaticStmt buildStaticStmt(StaticMethod m, String line)
	{
		StaticStmt s = new StaticStmt();
		s.setStmtID(methodContext.currentStmtID++);
		s.setSmaliStmt(line);
		s.setBlockLabel(methodContext.label.clone());
		methodContext.normalLabelAlreadyUsed = true;
		String bytecodeOp = (line.contains(" "))?
				line.substring(0, line.indexOf(" ")) : line;
		int stmtIndex = 0;
		/** first find the bytecode instruction index */
		while (stmtIndex < 219)
		{
			if (bytecodeOp.equals(DalvikBytecodeFormat.smaliStatements[stmtIndex]))
				break;
			++stmtIndex;
		}
		/** move vA, vB */
		if (stmtIndex >= 1 && stmtIndex <= 9)
		{
			String vA = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			String vB = line.substring(line.indexOf(", ")+2);
			s.setvA(vA);
			s.setvB(vB);
			Expression ex = new Expression("=");
			ex.add(new Expression(vA));
			ex.add(new Expression(vB));
			s.setExpression(ex);
		}
		/** move-result vA */
		else if (stmtIndex >= 10 && stmtIndex <= 12)
		{
			String vA = line.substring(line.indexOf(" ")+1);
			s.setvA(vA);
			Expression ex = new Expression("=");
			ex.add(new Expression(vA));
			Expression resultEx;
			if (methodContext.resultExpression != null)
			{
				resultEx = methodContext.resultExpression.clone();
				methodContext.resultExpression = null;
			}
			else
			{
				resultEx = new Expression("$invokeResult");
			}
			ex.add(resultEx);
			s.setExpression(ex);
		}
		/** return-void  - no action needed */
		else if (stmtIndex == 14)
		{}
		/** return variable */
		else if (stmtIndex > 14 && stmtIndex <= 17)
		{
			String vA = line.substring(line.indexOf(" ")+1);
			s.setvA(vA);
		}
		/** const types:
			'const' and 'const-wide': arbitrary 32/64 bit constant
			with suffix '/high16': the 16 bit constant is right-zero-extended to 32/64 bit
		*/
		/** const vA, #+B */
		else if (stmtIndex >= 18 && stmtIndex <= 21)
		{
			String vA = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			String hex = line.substring(line.indexOf(", ")+2);
			s.setvA(vA);
			String dec = "", type = "";
			if (hex.contains("# ")) // float will be annotated in new version of smali
			{
				dec = hex.substring(hex.indexOf("# ")+2);
				hex = hex.substring(0, hex.indexOf(" "));
				type = "float";
			}
			else
			{
				dec = Integer.parseInt(hex.replace("0x", ""), 16) + "";
				type = "int";
				// Just to be sure, go ahead to see if there's a debug info
				// that specify the type. 
				int tempIndex = index;
				String tempLine;
				while (tempIndex < smaliCode.size())
				{
					tempLine = smaliCode.get(tempIndex++);
					if (tempLine.contains(" "))
						tempLine = tempLine.trim();
					if (tempLine.startsWith(".local "))
					{
						String vName = tempLine.substring(tempLine.indexOf(" ")+1, tempLine.indexOf(", "));
						if (vName.equals(vA))
						{
							if (tempLine.endsWith(":F"))
							{
								type = "float";
								dec = Float.intBitsToFloat(Integer.parseInt(dec)) + "";
							}
						}
						break;
					}
				}
			}
			Expression numberEx = new Expression("$number");
			Expression typeEx = new Expression(type);
			Expression hexEx = new Expression(hex);
			Expression decEx = new Expression(dec);
			numberEx.add(typeEx);
			numberEx.add(hexEx);
			numberEx.add(decEx);
			Expression ex = new Expression("=");
			ex.add(new Expression(vA));
			ex.add(numberEx);
			s.setExpression(ex);
		}
		else if (stmtIndex >= 22 && stmtIndex <= 25)
		{	// this is for wide(64 bit) const
			String vA = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			String hex = line.substring(line.indexOf(", ")+2);
			s.setvA(vA);
			String dec = "", type = "";
			if (hex.contains("# "))	// double
			{
				dec = hex.substring(hex.indexOf(", ")+2);
				hex = hex.substring(0, hex.indexOf(" "));
				type = "double";
			}
			else	// long
			{
				dec = Long.parseLong(hex.replace("0x", "").replace("L", ""), 16) + "";
				type = "long";
			}
			Expression numberEx = new Expression("$number");
			Expression typeEx = new Expression(type);
			Expression hexEx = new Expression(hex);
			Expression decEx = new Expression(dec);
			numberEx.add(typeEx);
			numberEx.add(hexEx);
			numberEx.add(decEx);
			Expression ex = new Expression("=");
			ex.add(new Expression(vA));
			ex.add(numberEx);
			s.setExpression(ex);
		}
		else if (stmtIndex == 26 || stmtIndex == 27)
		{
			String vA = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			s.setvA(vA);
			String stringLit = line.substring(line.indexOf(", ")+2);
			Expression ex = new Expression("=");
			Expression classEx = new Expression("$const-string");
			classEx.add(new Expression(stringLit));
			ex.add(new Expression(vA));
			ex.add(classEx);
			s.setExpression(ex);
		}
		else if (stmtIndex == 28)
		{
			String vA = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			s.setvA(vA);
			String className = line.substring(line.indexOf(", ")+2);
			Expression ex = new Expression("=");
			Expression classEx = new Expression("$const-class");
			classEx.add(new Expression(className));
			ex.add(new Expression(vA));
			ex.add(classEx);
			s.setExpression(ex);
		}
		/** instance-of vA, vB, type@CCCC */
		else if (stmtIndex == 32)
		{
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String type = vs[2];
			s.setvA(vA);
			s.setvB(vB);
			s.setvC(type);
			Expression ex = new Expression("=");
			Expression opEx = new Expression("$instance-of");
			opEx.add(new Expression(vB));
			opEx.add(new Expression(type));
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		/** array-length vA, vB */
		else if (stmtIndex == 33)
		{
			String vA = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			String vB = line.substring(line.indexOf(", ")+2);
			s.setvA(vA);
			s.setvB(vB);
			Expression ex = new Expression("=");
			Expression opEx = new Expression("$array-length");
			opEx.add(new Expression(vB));
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		/** new-instance vA, type@BBBB */
		else if (stmtIndex == 34)
		{
			String vA = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			String className = line.substring(line.indexOf(", ")+2);
			s.setvA(vA);
			Expression ex = new Expression("=");
			Expression opEx = new Expression("$new-instance");
			opEx.add(new Expression(className));
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		/** new array vA, vB, type@CCCC */
		else if (stmtIndex == 35)
		{
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			s.setvA(vA);
			s.setvB(vB);
			String type = vs[2].substring(1);// get rid of the '[' at the begining
			type = Utility.parseParameters(type).get(0); // change to recognizable primitive type
			Expression ex = new Expression("=");
			Expression arrayEx = new Expression("$array");
			Expression lengthEx = new Expression(vB);
			Expression typeEx = new Expression(type);
			arrayEx.add(lengthEx);
			arrayEx.add(typeEx);
			ex.add(new Expression(vA));
			ex.add(arrayEx);
			s.setExpression(ex);
		}
		/** filled-new-array(/range) {vC,vD,vE,vF,vG}, type@BBBB */
		else if (stmtIndex == 36 || stmtIndex == 37)
		{
			String elements = line.substring(line.indexOf("{")+1, line.indexOf("}"));
			String type = line.substring(line.lastIndexOf(", ")).substring(1);
			type = Utility.parseParameters(type).get(0);
			Expression ex = new Expression("$array");
			int length = 1;
			if (elements.contains(", "))
			{
				ArrayList<String> eles = (ArrayList<String>) Arrays.asList(elements.split(", "));
				length = eles.size();
				ex.add(new Expression(length + ""));
				ex.add(new Expression(type));
				int ele_index = 0;
				for (String ele : eles)
				{
					Expression eleEx = new Expression("$element");
					eleEx.add(new Expression("" + ele_index++));
					eleEx.add(new Expression(ele));
					ex.add(eleEx);
				}
			}
			else
			{
				ex.add(new Expression(length + ""));
				ex.add(new Expression(type));
				Expression eleEx = new Expression("$element");
				eleEx.add(new Expression("0"));
				eleEx.add(new Expression(elements));
				ex.add(eleEx);
			}
			methodContext.resultExpression = ex.clone();
		}
		/** fill-array-data vAA, :array_0 */
		else if (stmtIndex == 38)
		{
			String vA = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			String arrayDataLabel = line.substring(line.indexOf(", ")+2);
			Expression ex = new Expression("=");
			ex.add(new Expression(vA));
			ex.add(new Expression(arrayDataLabel));
			s.setExpression(ex);
			methodContext.tableDataMap.put(arrayDataLabel, s.getStmtID());
		}
		/** throw vAA - no action needed*/
		else if (stmtIndex == 39)
		{}
		/** goto :goto_0*/
		else if (stmtIndex >= 40 && stmtIndex <= 42)
		{
			String targetLabel = line.substring(line.indexOf(" ")+1);
			s.setData(targetLabel);
		}
		/** packed/sparse switch */
		else if (stmtIndex == 43 || stmtIndex == 44)
		{
			String vA = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			String switchLabel = line.substring(line.indexOf(", ")+2);
			s.setvA(vA);
			methodContext.tableDataMap.put(switchLabel, s.getStmtID());
		}
		/** float, long, double comparison:	cpmkind vAA, vBB, vCC 
		 Note: this statement is always followed by an ifz-test statement 
		 */
		else if (stmtIndex >= 45 && stmtIndex <= 49)
		{
			String vs[] = line.substring(line.indexOf(" ")+1).split(", ");
			s.setvA(vs[0]);
			s.setvB(vs[1]);
			s.setvC(vs[2]);
			methodContext.ifFollowingCMP = true;
			methodContext.CMPleft = vs[1];
			methodContext.CMPright = vs[2];
		}
		/** if-test vA, vB, :cond_0 */
		else if (stmtIndex >= 50 && stmtIndex <= 55)
		{
			String operator = line.substring(0, line.indexOf(" ")).split("-")[1];
			String vs[] = line.substring(line.indexOf(" ")+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String jumpTargetLabel = vs[2];
			s.setvA(vA);
			s.setvB(vB);
			s.setData(jumpTargetLabel);
			String newOp = "";
			if (operator.equals("eq"))			newOp = "=";
			else if (operator.equals("ne"))		newOp = "/=";
			else if (operator.equals("lt"))		newOp = "<";
			else if (operator.equals("ge"))		newOp = ">=";
			else if (operator.equals("gt"))		newOp = ">";
			else if (operator.equals("le"))		newOp = "<=";
			Expression ex = new Expression(newOp);
			ex.add(new Expression(vA));
			ex.add(new Expression(vB));
			s.setExpression(ex);
		}
		/** ifz vA, :cond_0 */
		else if (stmtIndex >= 56 && stmtIndex <= 61)
		{
			String operator = line.substring(0, line.indexOf(" ")).split("-")[1];
			String vA = line.substring(line.indexOf(" ")+1, line.indexOf(", "));
			String jumpTargetLabel = line.substring(line.indexOf(", ")+2);
			String right = "0";
			s.setvA(vA);
			s.setData(jumpTargetLabel);
			String newOp = "";
			if (methodContext.ifFollowingCMP)
			{
				vA = methodContext.CMPleft;
				right = methodContext.CMPright;
				operator = operator.replace("z", "");
				methodContext.ifFollowingCMP = false;
				methodContext.CMPleft = "";
				methodContext.CMPright = "";
			}
			if (operator.equals("eq"))			newOp = "=";
			else if (operator.equals("ne"))		newOp = "/=";
			else if (operator.equals("lt"))		newOp = "<";
			else if (operator.equals("ge"))		newOp = ">=";
			else if (operator.equals("gt"))		newOp = ">";
			else if (operator.equals("le"))		newOp = "<=";
			else if (operator.equals("eqz"))	newOp = "=";
			else if (operator.equals("nez"))	newOp = "/=";
			else if (operator.equals("ltz"))	newOp = "<";
			else if (operator.equals("gez"))	newOp = ">=";
			else if (operator.equals("gtz"))	newOp = ">";
			else if (operator.equals("lez"))	newOp = "<=";
			Expression ex = new Expression(newOp);
			ex.add(new Expression(vA));
			ex.add(new Expression(right));
			s.setExpression(ex);
		}
		/** aget vAA, vBB, vCC */
		else if (stmtIndex >= 62 && stmtIndex <= 68)
		{
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String arrayIndex = vs[2];
			s.setvA(vA);
			s.setvB(vB);
			s.setvC(arrayIndex);
			Expression ex = new Expression("=");
			Expression opEx = new Expression("$aget");
			opEx.add(new Expression(vB));
			opEx.add(new Expression(arrayIndex));
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		/** aput vAA, vBB, vCC */
		else if (stmtIndex >= 69 && stmtIndex <= 75)
		{
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String arrayIndex = vs[2];
			s.setvA(vA);
			s.setvB(vB);
			s.setvC(arrayIndex);
			Expression ex = new Expression("=");
			Expression opEx = new Expression("$aput");
			opEx.add(new Expression(vB));
			opEx.add(new Expression(arrayIndex));
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		/** iget vA, vB, field@CCCC */
		else if (stmtIndex >= 76 && stmtIndex <= 82)
		{
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String fieldSig = vs[2];
			s.setvA(vA);
			s.setvB(vB);
			s.setData(fieldSig);
			Expression ex = new Expression("=");
			Expression opEx;
			String fieldName = fieldSig.substring(
					fieldSig.indexOf("->")+2, fieldSig.indexOf(":"));
			if (fieldName.startsWith("this$"))
			{
				String fieldType = fieldSig.substring(fieldSig.indexOf(":")+1);
				opEx = new Expression("$this");
				opEx.add(new Expression(fieldType));
			}
			else
			{
				opEx = new Expression("$Finstance");
				opEx.add(new Expression(fieldSig));
				opEx.add(new Expression(vB));
			}
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		/** iput vA, vB, field@CCCC */
		else if (stmtIndex >= 83 && stmtIndex <= 89)
		{
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String fieldSig = vs[2];
			s.setvA(vA);
			s.setvB(vB);
			s.setData(fieldSig);
			Expression ex = new Expression("=");
			Expression opEx = new Expression("$Finstance");
			opEx.add(new Expression(fieldSig));
			opEx.add(new Expression(vB));
			ex.add(opEx);
			ex.add(new Expression(vA));
			s.setExpression(ex);
		}
		/** sget vAA, field@BBBB */
		else if (stmtIndex >= 90 && stmtIndex <= 96)
		{
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String fieldSig = vs[1];
			s.setvA(vA);
			s.setData(fieldSig);
			Expression ex = new Expression("=");
			Expression opEx;
			String fieldName = fieldSig.substring(
					fieldSig.indexOf("->")+2, fieldSig.indexOf(":"));
			if (fieldName.startsWith("this$"))
			{
				String fieldType = fieldSig.substring(fieldSig.indexOf(":")+1);
				opEx = new Expression("$this");
				opEx.add(new Expression(fieldType));
			}
			else
			{
				opEx = new Expression("$Fstatic");
				opEx.add(new Expression(fieldSig));
			}
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		/** sput vAA, field@BBBB */
		else if (stmtIndex >= 97 && stmtIndex <= 103)
		{
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String fieldSig = vs[1];
			s.setvA(vA);
			s.setData(fieldSig);
			Expression ex = new Expression("=");
			Expression opEx = new Expression("$Fstatic");
			opEx.add(new Expression(fieldSig));
			ex.add(opEx);
			ex.add(new Expression(vA));
			s.setExpression(ex);
		}
		/** invoke-kind {vC,vD,vE,vF,vG}, method@BBBB 
		 * 	invoke-kind/range {vAAAA .. vNNNN}, method@MMMM
		 * */
		else if (stmtIndex >= 104 && stmtIndex <= 114)
		{
			//String invokeType = line.substring(0, line.indexOf(" "));
			String params = line.substring(line.indexOf("{")+1, line.indexOf("}"));
			String methodSig = line.substring(line.lastIndexOf(", ")+2);
			s.setData(methodSig);
			Expression ex = new Expression("$invoke");
			Expression sigEx = new Expression(methodSig);
			ex.add(sigEx);
			if (params.contains(", "))
			{
				for (String p : params.split(", "))
				{
					ex.add(new Expression(p));
				}
			}
			else if (params.contains(" .. "))
			{
				String firstV = params.substring(0, params.indexOf(" .. "));
				String lastV = params.substring(params.indexOf(" .. ")+4);
				String prefix = firstV.substring(0, 1);
				int first = Integer.parseInt(firstV.substring(1));
				int last = Integer.parseInt(lastV.substring(1));
				while (first <= last)
				{
					ex.add(new Expression(prefix + first));
					first++;
				}
			}
			else
			{
				ex.add(new Expression(params));
			}
			s.setExpression(ex);
		}
		/** neg, not vA, vB */
		else if (stmtIndex >= 115 && stmtIndex <= 120)
		{
			String operator = line.substring(0, line.indexOf("-"));
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			s.setvA(vA);
			s.setvB(vB);
			Expression ex = new Expression("=");
			Expression opEx = new Expression(operator);
			opEx.add(new Expression(vB));
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		/** primitive type conversion */
		else if (stmtIndex >= 121 && stmtIndex <= 135)
		{
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			s.setvA(vA);
			s.setvB(vB);
			Expression ex = new Expression("=");
			ex.add(new Expression(vA));
			ex.add(new Expression(vB));
			s.setExpression(ex);
		}
		/** binop operation (3 addresses: a = b op c) */
		else if (stmtIndex >= 136 && stmtIndex <= 167)
		{
			String operator = line.substring(0, line.indexOf("-"));
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String vC = vs[2];
			s.setvA(vA);
			s.setvB(vB);
			s.setvC(vC);
			Expression ex = new Expression("=");
			Expression opEx = new Expression(operator);
			opEx.add(new Expression(vB));
			opEx.add(new Expression(vC));
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		/** binop operation (2 addresses: a = a op b) */
		else if (stmtIndex >= 168 && stmtIndex <= 199)
		{
			String operator = line.substring(0, line.indexOf("-"));
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			s.setvA(vA);
			s.setvB(vB);
			Expression ex = new Expression("=");
			Expression opEx = new Expression(operator);
			opEx.add(new Expression(vA));
			opEx.add(new Expression(vB));
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		/** binop/lit# vA, vB, #+CCCC (int only) 
		 	lit16 - 16 bit constant
		 	lit8 - 8 bit constant
		 */
		else if (stmtIndex >= 200 && stmtIndex <= 218)
		{
			String operator = line.substring(0, line.indexOf("-"));
			String vs[] = line.substring(line.indexOf(" " )+1).split(", ");
			String vA = vs[0];
			String vB = vs[1];
			String intLiteral = vs[2];
			s.setvA(vA);
			s.setvB(vB);
			String dec = Integer.parseInt(intLiteral.replace("0x", ""), 16) + "";
			Expression ex = new Expression("=");
			Expression litEx = new Expression("$number");
			Expression typeEx = new Expression("int");
			Expression hexEx = new Expression(intLiteral);
			Expression decEx = new Expression(dec);
			litEx.add(typeEx);
			litEx.add(hexEx);
			litEx.add(decEx);
			Expression opEx = new Expression(operator);
			opEx.add(new Expression(vB));
			opEx.add(litEx);
			ex.add(new Expression(vA));
			ex.add(opEx);
			s.setExpression(ex);
		}
		return s;
	}
	
	public void setMaxOriginalLineNumber(int maxOriginalLineNumber) {
		this.maxOriginalLineNumber = maxOriginalLineNumber;
	}

	public void setSmaliCode(ArrayList<String> smaliCode) {
		this.smaliCode = new ArrayList<String>(smaliCode);
	}

	public void setSmaliFilePath(String smaliFilePath) {
		this.smaliFilePath = smaliFilePath;
	}

	
	private class MethodContext {
		/** general context */
		BlockLabel label;
		boolean normalLabelAlreadyUsed;
		int currentLineNumber;
		int currentStmtID;
		/** for long/float/double comparison statements */
		boolean ifFollowingCMP;
		String CMPleft, CMPright;
		/** for move-result, from filled-new-array */
		Expression resultExpression;
		/** for fill-array-data, pswitch, sswitch */
		Map<String, Integer> tableDataMap = new HashMap<String, Integer>();
	}
	
}
