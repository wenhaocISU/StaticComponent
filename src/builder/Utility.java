package builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Utility {

	public static Object readObject(String filePath) throws Exception{
		
		Object obj = null;
		File f = new File(filePath);
		if (!f.exists())
			return obj;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath));
			obj = in.readObject();
			in.close();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			if (e.getMessage().contains("local class incompatible"))
			{
				System.out.println("Class version incompatible... need rebuild object");
			}
			else e.printStackTrace();
		}
		return obj;
	}
	
	public static void saveObject(Object obj, String filePath) {
		File f = new File(filePath);
		if (f.exists())
			f.delete();
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath));
			out.writeObject(obj);
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
