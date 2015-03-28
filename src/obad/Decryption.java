package obad;

import java.io.BufferedReader;
import java.io.FileReader;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import staticFamily.StaticStmt;
import symbolic.Expression;
import analysis.StaticInfo;

public class Decryption {
	
	static String keyFolder = "/home/wenhaoc/git/decryptingObadStrings/keys/";
	
	static String[] classes = {
			"OocIOCIo", "ocOlcICo", "CIOIIolc", "cOOCoCc", "lOIlloc",
			"oCOllOO", "IlIIlCI", "IcCcCOIC", "OOOOlIO", "olcCIIC",
			"OooOOOo", "oclClII", "OclIIOlC", "occcclc", "IlOIcII",
			"loOOIclc", "IcCcCOIC$cOIcOOo", "CIlOCClc", "cOIcOOo", "oIlclcIc",
			"OlCCcIl", "cIoCcIo", "CIcIoICo", "ooCclcC", "cCOIcIlo",
			"CcoCIcI", "OlICCCco", "cOoOICO", "MainService", "OIccoIlO", 
			"oOCCOOI", "COcocOlo", "OcooIclc", "OlOClICl", "lCICoIO",
			"OcIcoOlc", "OoCOocll", "CICCCcCI", "ololCCOc", "IExtendedNetworkService$cOIcOOo" };
	
	static int[] regs = {
			1,0,1,0,0,0,0,0,0,0,
			0,0,0,0,1,0,0,1,0,1,
			0,0,0,1,0,1,0,0,1,0,
			0,0,1,0,0,0,0,1,0  	};
	
	static int[] lengths = {
			239, 1828, 530, 996, 1437, 756, 2414, 1463, 229, 107, 
			636, 313, 184, 440, 935, 113, 248, 903, 90, 1233, 
			76, 261, 291, 58, 101, 391, 137, 323, 1243, 91,
			1716, 878, 1130, 61, 275, 59, 245, 675, 218, 199	};
	
    public static String decrypt (int[] source, int i, int j, int k) {
    	byte[] result = new byte[k+56];
    	int[] resultInt = new int[k+56];
    	resultInt[0]=i+82;
    	for (int ii = 1; ii<=k+55; ii++) {
    		resultInt[ii] = resultInt[ii-1]+source[j+ii-1]-4;
    	}
    	for (int jj = 0; jj<=k+55; jj++)
    		result[jj] = (byte)resultInt[jj];
    	String results = new String(result);
    	return results;
    }
	
    public static int[] loadKeys(String shortClassName)
    {
    	int index = -1;
    	for (int i = 0; i < 39; i++)
    	{
    		if (shortClassName.equals(classes[i]))
    		{
    			index = i;
    			break;
    		}
    	}
    	if (index == -1)
    		return null;
    	int[] result = new int[lengths[index]];
    	try
    	{
    		BufferedReader in = new BufferedReader(new FileReader(keyFolder+"Key_No_"+(index+1)+".txt"));
    		String line;
    		index = 0;
    		while ((line = in.readLine())!=null)
    		{
    			result[index++] = Integer.parseInt(line);
    		}
    		in.close();
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	return result;
    }
    /**
     * The pattern:
     * 
     *   const v0, v1, v2;
     *   invoke path/to/shortClassName;->cOIcOOo(III)Ljava/lang/String;
     *   move-result-object v3;
     *   Class.forName(v3);
     * 
     * */
	public static void main(String[] args)
	{
		StaticApp staticApp = StaticInfo.initAnalysis(
				ObadData.apkPath, false);
		for (StaticClass c : staticApp.getClasses())
		{
			for (StaticMethod m : c.getMethods())
			{
				for (StaticStmt s : m.getSmaliStmts())
				{
					if (s.invokesMethod() &&
							s.getSmaliStmt().endsWith(";->cOIcOOo(III)Ljava/lang/String;"))
					{
						String targetSig = (String) s.getData();
						String[] params = new String[3];
						int[] intParams = new int[3];
						Expression ex = s.getExpression();
						// get the name of the parameter variables
						for (int i = 1; i < ex.getChildCount(); i++)
						{
							params[i-1] = ((Expression) ex.getChildAt(i)).getContent();
						}
						// go 3 statements back, find out the values of the 3 variables
						for (int i = 1; i < 4; i++)
						{
							StaticStmt prevS = m.getSmaliStmts().get(s.getStmtID()-i);
							Expression thisEx = prevS.getExpression();
							String left = ((Expression) thisEx.getChildAt(0)).getContent();
							Expression rightEx = (Expression) thisEx.getChildAt(1);
							if (rightEx.getContent().equals("$number"))
							{
								String hex = ((Expression)rightEx.getChildAt(2)).getContent();
								int value = Integer.parseInt(hex);
								if (left.equals(params[0]))
									intParams[0] = value;
								else if (left.equals(params[1]))
									intParams[1] = value;
								else if (left.equals(params[2]))
									intParams[2] = value;
							}
						}
						// parse short class name
						String className = targetSig.substring(0, targetSig.indexOf("->"));
						String shortClassName = className.substring(className.lastIndexOf("/")+1
								, className.length()-1);
						int[] keys = loadKeys(shortClassName);
						if (keys == null)
							continue;
						System.out.println("found statement: "
								+ c.getDexName() + ":" + s.getSourceLineNumber() + "\n "
								+ s.getSmaliStmt());
						System.out.println(" " + params[0] + " = " + intParams[0]);
						System.out.println(" " + params[1] + " = " + intParams[1]);
						System.out.println(" " + params[2] + " = " + intParams[2]);
						String decryptedString = decrypt(keys, intParams[0], intParams[1], intParams[2]);
						System.out.println("the string is: " + decryptedString);
					}
				}
			}
		}
	}
	
	
}
