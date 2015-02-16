package main;

public class Paths {

	
	
	/**
	AppDataDir contains all the data generated in static analysis.
	Each apk file will have its own folder, e.g. .../appDataDir/Fast.apk/
	**/
	public static String AppDataDir = "C:/Users/Wenhao/Documents/appData";
	
	/**
	The recommended version of apktool is '2.0.0rc3'
	**/
	public static String apktoolPath = AppDataDir + "/ToolsAndLibs/apktool.2.0.0rc3.jar";
	
	/**
	Apktool needs the 'aapt' path in SDK build tools
	TODO: Need to check if newer version of Apktool
	works well with newer version of SDK.
	**/
	public static String SDKPath = "C:/Users/Wenhao/Documents/adt-bundle-windows-x86_64-20140702/sdk";
	public static String aaptPath = SDKPath + "/build-tools/android-4.4W/aapt.exe";
	
	/**
	Use keytool to generate your own keystore. It will
	be used by jarsigner to sign the compiled apk.
	**/
	public static String KeyStorePath = AppDataDir + "/ToolsAndLibs/wenhaoc.keystore";
	public static String KeyStoreKey = "isu_obad";
}
