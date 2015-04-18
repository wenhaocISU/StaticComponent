package tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import symbolic.Blacklist;

public class CoverageStats {

	
	/**
	 * Return all the lines in the app that didn't get hit
	 * The result format:
	 * 	Key set: Class name. Format example: com.example.fast
	 * 	Value set: Missing lines within this class. Format is the same as the input Set<String> hitLines.
	 * */
	public Map<String, Set<String>> getMissingLines(Set<String> hitLines, StaticApp staticApp)
	{
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		for (StaticClass c : staticApp.getClasses())
		{
			if (Blacklist.classInBlackList(c.getDexName()))
				continue;
			Set<String> missingLinesInClass = new HashSet<String>();
			for (StaticMethod m : c.getMethods())
			{
				for (int i : m.getSourceLineNumbers())
				{
					String thisLine = c.getJavaName() + ":" + i;
					if (!hitLines.contains(thisLine))
						missingLinesInClass.add(thisLine);
				}
			}
			result.put(c.getJavaName(), missingLinesInClass);
		}
		return result;
	}
	
	public Set<String> getMissingLinesInASet(Set<String> hitLines, StaticApp staticApp)
	{
		Set<String> result = new HashSet<String>();
		for (StaticClass c : staticApp.getClasses())
		{
			if (Blacklist.classInBlackList(c.getDexName()))
				continue;
			for (StaticMethod m : c.getMethods())
			{
				for (int i : m.getSourceLineNumbers())
				{
					String thisLine = c.getJavaName() + ":" + i;
					System.out.println("comparing  " + thisLine);
					if (!hitLines.contains(thisLine))
						result.add(thisLine);
				}
			}
		}
		return result;
	}
	
	
	/**
	 * Return all the lines in one method that aren't in 'lines'
	 * */
	public Set<String> getMissingLines(Set<String> lines, StaticMethod m)
	{
		Set<String> result = new HashSet<String>();
		String className = dexToJavaClassName(m.getDeclaringClass());
		for (int i : m.getSourceLineNumbers())
		{
			String thisLine = className + ":" + i;
			if (!lines.contains(thisLine))
				result.add(thisLine);
		}
		return result;
	}
	
	
	private static String dexToJavaClassName(String dexName)
	{
		if (dexName.startsWith("L") && dexName.endsWith(";"))
			return dexName.substring(1, dexName.length()-1).replace("/", ".");
		return "";
	}
	
}
