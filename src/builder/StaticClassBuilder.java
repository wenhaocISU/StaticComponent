package builder;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import staticFamily.StaticClass;

public class StaticClassBuilder implements Callable<StaticClass>{

	private ArrayList<String> smaliCode;
	private String smaliFilePath;
	private int maxOriginalLineNumber;

	@Override
	public StaticClass call() throws Exception {
		
		StaticClass c = new StaticClass();
		// First Line
		if (smaliCode.size() > 0)
		{
			c.setDeclaration(smaliCode.get(0));
		}
		int index = 1, maxIndex = smaliCode.size();
		// Parse Pre-Method Section
		while (index < maxIndex)
		{
			String line = smaliCode.get(index++);
			if (line.equals("# direct methods") || line.equals("# virtual methods"))
				break;
			//TODO here
		}
		// Parse Method Section
		while (index < maxIndex)
		{
			String line = smaliCode.get(index++);
			
		}
		// Write back to file
		PrintWriter out = new PrintWriter(new FileWriter(smaliFilePath));
		for (String line: smaliCode)
			out.write(line + "\n");
		out.close();
		return c;
	}

	public void setMaxOriginalLineNumber(int maxOriginalLineNumber) {
		this.maxOriginalLineNumber = maxOriginalLineNumber;
	}

	public void setSmaliCode(ArrayList<String> smaliCode) {
		this.smaliCode = smaliCode;
	}

	public void setSmaliFilePath(String smaliFilePath) {
		this.smaliFilePath = smaliFilePath;
	}

	
}
