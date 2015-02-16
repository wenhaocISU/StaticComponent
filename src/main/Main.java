package main;

import java.io.File;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import builder.StaticAppBuilder;

public class Main {

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		String apkPaths[] = {
				"C:/Users/Wenhao/Documents/juno_workspace/AndroidTest/bin/AndroidTest.apk"
		};
		
		String apkPath = apkPaths[0];
		
		StaticApp staticApp = StaticAppBuilder.buildOrReadStaticApp(apkPath, true);
		
		for (StaticClass c : staticApp.getClasses())
			System.out.println(c.getJavaName());
		//System.out.println(staticApp.getInstrumentedApkPath());
		//System.out.println(staticApp.getUnsignedApkPath());
		
	}

}
