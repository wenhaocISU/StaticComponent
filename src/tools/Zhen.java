package tools;

import java.util.HashSet;
import java.util.Set;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;

public class Zhen {

	/**
	 * Return all the lines in the app that aren't in 'lines'
	 * */
	Set<String> getMissingLines(Set<String> lines, StaticApp staticApp)
	{
		Set<String> result = new HashSet<String>();
		for (StaticClass c : staticApp.getClasses())
		{
			for (StaticMethod m : c.getMethods())
			{
				for (int i : m.getSourceLineNumbers())
				{
					String thisLine = c.getJavaName() + ":" + i;
					if (!lines.contains(thisLine))
						result.add(thisLine);
				}
			}
		}
		return result;
	}
	
	/**
	 * Return all the lines in one method that aren't in 'lines'
	 * */
	Set<String> getMissingLines(Set<String> lines, StaticMethod m)
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
