package tools;

import java.io.File;
import java.io.OutputStream;

import main.Paths;

public class Jarsigner {

	public static void signAPK(String inAppPath, String outAppPath)
	{
		File inApp = new File(inAppPath);
		if (!inApp.exists())
		{
			System.out.println("[jarsigner]Cannot find file " + inAppPath);
			return;
		}
		System.out.println("Signing APK with jarsigner...");
		File outApp = new File(outAppPath);
		if (outApp.exists())
			outApp.delete();
		try {
			String keystoreName = Paths.KeyStorePath.substring(Paths.KeyStorePath.lastIndexOf("/")+1);
			String command = "jarsigner -keystore " + Paths.KeyStorePath
							+ " -signedjar " + outAppPath
							+ " " + inAppPath
							+ " " + keystoreName;
			Process pc = Runtime.getRuntime().exec(command);
			OutputStream out = pc.getOutputStream();
			out.write((Paths.KeyStoreKey + "\n").getBytes());
			out.flush();
			pc.waitFor();
			outApp = new File(outAppPath);
			if (outApp.exists()) {
				System.out.println("Signed file:\n\t" + outAppPath);
				inApp.delete();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
