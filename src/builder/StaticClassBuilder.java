package builder;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

import staticFamily.BlockLabel;
import staticFamily.StaticClass;
import staticFamily.StaticField;
import staticFamily.StaticMethod;
import staticFamily.StaticStmt;

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
				c.setSuperClass(Utility.nameDexToJava(superClassName));
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
						c.addInnerClass(Utility.nameDexToJava(memberClassName));
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
						c.setOuterClass(Utility.nameDexToJava(className));
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
						c.setOuterClass(Utility.nameDexToJava(className));
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
		
		// Parse Method Section
		while (index < smaliCode.size())
		{
			String line = smaliCode.get(index++);
			if (line.startsWith(".method "))
			{
				StaticMethod m = new StaticMethod();
				String subSig = line.substring(line.lastIndexOf(" ")+1);
				String fullSig = c.getDexName() + "->" + subSig;
				String params = subSig.substring(subSig.indexOf("(") + 1, subSig.indexOf(")"));
				m.setSignature(fullSig);
				m.setParamTypes(Utility.parseParameters(params));
				methodContext.currentLineNumber = -1;
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
						/** Instrumentation 1:
						If current statement doesn't have a line number, we give
						it one. **/
						if (methodContext.currentLineNumber == -1)
						{
							String toAdd = "    .line " + this.maxOriginalLineNumber++;
							int insertionLocation = index-1;
							String tempLine = smaliCode.get(insertionLocation);
							while (!tempLine.equals("") && !tempLine.equals("    .prologue"))
								tempLine = smaliCode.get(--insertionLocation);
							smaliCode.add(insertionLocation+1, toAdd);
							++index;
							s.setSourceLineNumber(this.maxOriginalLineNumber-1);
						}
						else
						{
							s.setSourceLineNumber(methodContext.currentLineNumber);
							methodContext.currentLineNumber = -1;
						}
						/** Instrumentation 2:
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
				}
			}
		}
		// Write back to file
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
				//TODO
			}
		}
		else if (line.startsWith(".catchall "))
		{
			//TODO
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
			while (!line.equals(".end array-data") && index < smaliCode.size())
			{
				line = smaliCode.get(index++);
				if (line.contains(" "))
					line = line.trim();
				//TODO
			}
		}
		else if (line.startsWith(":sswitch_data_"))
		{
			while (!line.equals(".end sparse-switch") && index < smaliCode.size())
			{
				line = smaliCode.get(index++);
				if (line.contains(" "))
					line = line.trim();
				//TODO
			}
		}
		else if (line.startsWith(":pswitch_data_"))
		{
			while (!line.equals(".end packed-switch") && index < smaliCode.size())
			{
				line = smaliCode.get(index++);
				if (line.contains(" "))
					line = line.trim();
				//TODO
			}
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
		{	// This is the regular label section
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
		// smali stmt
		// statement id
		// block label
		// line number
		StaticStmt s = new StaticStmt();
		s.setSmaliStmt(line);
		s.setBlockLabel(methodContext.label.clone());
		methodContext.normalLabelAlreadyUsed = true;
		//ExpressionBuilder.buildExpression(s, line);
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
		BlockLabel label;
		boolean normalLabelAlreadyUsed;
		boolean needAddLineNumber;
		int currentLineNumber;
		
	}
	
}
