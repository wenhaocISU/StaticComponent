package main;

import staticFamily.StaticApp;
import symbolic.SymbolicExecution;
import analysis.StaticInfo;

public class Main {

	public static void main(String[] args) {

		String apkPaths[] = {
				"C:/Users/Wenhao/Documents/juno_workspace/AndroidTest/bin/AndroidTest.apk",
				"C:/Users/Wenhao/Documents/juno_workspace/AndroidTest/bin/TheApp.apk"
				
		};
		
		String apkPath = apkPaths[1];
		
		StaticApp staticApp = StaticInfo.initAnalysis(apkPath, false);
		
		SymbolicExecution sex = new SymbolicExecution(staticApp);
		sex.doFullSymbolic("Lthe/app/MainActivity;->onCreate(Landroid/os/Bundle;)V");
	}

}
