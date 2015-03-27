package obad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Misc {

	public static void createKeyText(String smaliPath, String outPath)
	{
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(smaliPath));
			PrintWriter out = new PrintWriter(new FileWriter(outPath));
			String line = "";
			
			while (!line.equals(".method static <clinit>()V"))
			{
				line = in.readLine();
			}
			while (!line.startsWith("    .array-data "))
			{
				line = in.readLine();
			}
			while (true)
			{
				line = in.readLine();
				if (line.equals("    .end array-data"))
					break;
				int newElement = Integer.parseInt(
						line.trim().replace("0x", "").replace("s", "").replace("t", ""),
						16
					);
				out.write(newElement + "\n");
			}
			in.close();
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		String smaliPath = "/home/wenhaoc/AppData/obad.apk/apktool/smali/com/android/internal/"
				+ "telephony/IExtendedNetworkService$cOIcOOo.smali";
		String outPath = "/home/wenhaoc/git/decryptingObadStrings/keys/Key_No_40.txt";
		createKeyText(smaliPath, outPath);
	}
	
}
