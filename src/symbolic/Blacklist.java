package symbolic;

import java.util.ArrayList;
import java.util.Arrays;

public class Blacklist {

	/**
	 * Some of the classes or methods(especially android.support.v4/v7) 
	 * is really big and irrelevant to the behavior of the app.
	 * Therefore they will be skipped when:
	 *  1. before starting to generate PathSummary
	 *  2. symbolic execution encounters InvokeStmt that invokes them,
	 *  3. concrete execution setting break points.
	 * This is only a temporary solution, to increase the efficiency of 
	 * concolic execution. Maybe in the future there will be a way to deal
	 * with these huge methods efficiently.
	 * */
	public static ArrayList<String> classes = new ArrayList<String>(Arrays.asList(
			"Landroid/app/*",
			"Landroid/support/v4/*",
			"Landroid/support/v7/*",
			"Landroid/support/annotation/*",
			"Lcom/example/androidtest/R*",
			"Lcom/actionbarsherlock/*",
			"Lcom/flurry/sdk/*",
			"Lcom/flurry/android/*",
			"Lorg/kobjects/*",
			"Lorg/ksoap2/*",
			"Lorg/kxml2/*",
			"Lorg/xmlpull/*"
	));
	
	
	public static ArrayList<String> methods = new ArrayList<String>(Arrays.asList(
			""
	));

	
	public static boolean classInBlackList(String className) {
		for (String c : classes) {
			if (c.endsWith("*")) {
				if (className.startsWith(c.substring(0, c.lastIndexOf("*"))))
					return true;
			}
			else if (c.endsWith(";")) {
				if (className.equals(c))
					return true;
			}
		}
		return false;
	}
	
	public static boolean methodInBlackList(String methodSig) {
		return methods.contains(methodSig);
	}

	
}
