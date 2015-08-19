package analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Utility {

	public static Object readObject(String filePath) throws Exception
	{
		
		Object obj = null;
		File f = new File(filePath);
		if (!f.exists())
			return obj;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath));
			obj = in.readObject();
			in.close();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			if (e.getMessage().contains("local class incompatible"))
			{
				System.out.println("Class version incompatible... need rebuild object");
			}
			else e.printStackTrace();
		}
		return obj;
	}
	
	public static void saveObject(Object obj, String filePath)
	{
		System.out.print("Writing StaticApp into file...");
		File f = new File(filePath);
		if (f.exists())
			f.delete();
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath));
			out.writeObject(obj);
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println(" Done.");
	}
	
	@SuppressWarnings("serial")
	private static Map<String, String> primitiveTypes = new HashMap<String, String>()
	{{
		put("V", "void");
		put("Z", "boolean");
		put("B", "byte");
		put("S", "short");
		put("C", "char");
		put("I", "int");
		put("J", "long");
		put("F", "float");
		put("D", "double");
	}};
	
	public static String dexToJavaTypeName(String dexName)
	{
		if (dexName.startsWith("L") && dexName.endsWith(";"))
			return dexName.substring(1, dexName.length()-1).replace("/", ".");
		else if (primitiveTypes.containsKey(dexName))
			return primitiveTypes.get(dexName);
		return dexName;
	}
	
	public static String javaToDexTypeName(String javaName)
	{
		if (javaName.startsWith("L") && javaName.endsWith(";"))
			return javaName;
		switch (javaName) 
		{
			case "void":	return "V";
			case "boolean":	return "Z";
			case "byte":	return "B";
			case "short":	return "S";
			case "char":	return "C";
			case "int":		return "I";
			case "long":	return "J";
			case "float":	return "F";
			case "double":	return "D";
			default:	return "L" + javaName.replace(".", "/") + ";";
		}
	}
	
	
	public static ArrayList<String> parseParameters(String p) {
		ArrayList<String> result = new ArrayList<String>();
		int index = 0;
		while (index < p.length()) {
			char c = p.charAt(index++);
			if (c == 'L') {
				String l = "" + c;
				if (c == 'L') {
					while (c != ';') {
						c = p.charAt(index++);
						l += c;
					}
				}
				result.add(dexToJavaTypeName(l));
			}
			else if (c == '[') {
				int dimension = 0;
				while (c == '[') {
					c = p.charAt(index++);
					dimension++;
				}
				String a = "" + c;
				if (c == 'L') {
					while (c != ';') {
						c = p.charAt(index++);
						a += c;
					}
					a = dexToJavaTypeName(a);
				}
				else switch (c) {
					case 'V':		a = "void";			break;
					case 'Z':		a = "boolean";		break;
					case 'B':		a = "byte";			break;
					case 'S':		a = "short";		break;
					case 'C':		a = "char";			break;
					case 'I':		a = "int";			break;
					case 'J':		a = "long";			break;
					case 'F':		a = "float";		break;
					case 'D':		a = "double";		break;
				}
				for (int i = 0; i < dimension; i++)
					a += "[]";
				result.add(a);
			}
			else {
				switch (c) {
					case 'V':		result.add("void");			break;
					case 'Z':		result.add("boolean");		break;
					case 'B':		result.add("byte");			break;
					case 'S':		result.add("short");		break;
					case 'C':		result.add("char");			break;
					case 'I':		result.add("int");			break;
					case 'J':		result.add("long");			break;
					case 'F':		result.add("float");		break;
					case 'D':		result.add("double");		break;
				}
			}
		}
		return result;
	}
	
}
