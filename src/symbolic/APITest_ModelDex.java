package symbolic;

import java.io.File;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import analysis.StaticInfo;

public class APITest_ModelDex {

	private static String modelDexFolder = "/home/wenhaoc/git/StaticComponent/libs/models/";
	
	public static void loadInLibrary(StaticApp staticApp)
	{
		StaticInfo.decodeRes = false;
		StaticInfo.instrumentApps = false;
		StaticInfo.parseManifest = false;
		
		System.out.println("\nLoading model dex into StaticApp, might take a while...");
		File[] modelDexes = (new File(modelDexFolder)).listFiles();
		for (File modelDex : modelDexes)
		{
			if (!modelDex.getName().endsWith(".apk"))
				continue;
			System.out.println(" loading " + modelDex.getAbsolutePath());
			StaticApp f = StaticInfo.initAnalysis(modelDex.getAbsolutePath(), true);
			for (StaticClass c : f.getClasses())
			{
				System.out.println("  [class]" + c.getDexName());
				staticApp.addClass(c);
			}
		}
		staticApp.alreadyContainsLibraries = true;
		
		StaticInfo.decodeRes = true;
		StaticInfo.instrumentApps = true;
		StaticInfo.parseManifest = true;
	}
	
}
