package symbolic;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import analysis.StaticInfo;

public class APITest {

	private static String frameworkAPKPath = "libs/framework.apk";
	private static String otherAPKPath = "libs/others.apk";
	
	public static void loadInLibrary(StaticApp staticApp)
	{
		StaticInfo.decodeRes = false;
		StaticInfo.instrumentApps = false;
		StaticInfo.parseManifest = false;
		
		System.out.println("\nLoading \"framework.apk\" into StaticApp, might take a while...");
		StaticApp f = StaticInfo.initAnalysis(frameworkAPKPath, false);
		for (StaticClass c : f.getClasses())
		{
			staticApp.addClass(c);
		}
		
		System.out.println("\nLoading \"others.apk\" into StaticApp, might take a while...");
		StaticApp o = StaticInfo.initAnalysis(otherAPKPath, false);
		for (StaticClass c : o.getClasses())
		{
			staticApp.addClass(c);
		}
		
		staticApp.alreadyContainsLibraries = true;
		
		StaticInfo.decodeRes = true;
		StaticInfo.instrumentApps = true;
		StaticInfo.parseManifest = true;
	}
	
}
