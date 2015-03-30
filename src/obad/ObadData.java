package obad;

public class ObadData {

	public static String apkPath = "/home/wenhaoc/AppStorage/obad.apk";
	
	public static String keyFolder = "/home/wenhaoc/git/decryptingObadStrings/keys/";
	
	public static String encryptionMethodSig = ";->cOIcOOo(III)Ljava/lang/String;";
	
	public static String[] classes = {
			"OocIOCIo", "ocOlcICo", "CIOIIolc", "cOOCoCc", "lOIlloc",
			"oCOllOO", "IlIIlCI", "IcCcCOIC", "OOOOlIO", "olcCIIC",
			"OooOOOo", "oclClII", "OclIIOlC", "occcclc", "IlOIcII",
			"loOOIclc", "IcCcCOIC$cOIcOOo", "CIlOCClc", "cOIcOOo", "oIlclcIc",
			"OlCCcIl", "cIoCcIo", "CIcIoICo", "ooCclcC", "cCOIcIlo",
			"CcoCIcI", "OlICCCco", "cOoOICO", "MainService", "OIccoIlO", 
			"oOCCOOI", "COcocOlo", "OcooIclc", "OlOClICl", "lCICoIO",
			"OcIcoOlc", "OoCOocll", "CICCCcCI", "ololCCOc", "IExtendedNetworkService$cOIcOOo" };
	
	public static int[] regs = {
			1,0,1,0,0,0,0,0,0,0,
			0,0,0,0,1,0,0,1,0,1,
			0,0,0,1,0,1,0,0,1,0,
			0,0,1,0,0,0,0,1,0  	};
	
	public static int[] lengths = {
			239, 1828, 530, 996, 1437, 756, 2414, 1463, 229, 107, 
			636, 313, 184, 440, 935, 113, 248, 903, 90, 1233, 
			76, 261, 291, 58, 101, 391, 137, 323, 1243, 91,
			1716, 878, 1130, 61, 275, 59, 245, 675, 218, 199	};
	
}
