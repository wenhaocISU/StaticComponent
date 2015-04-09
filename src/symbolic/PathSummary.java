package symbolic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PathSummary implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private ArrayList<String> executionLog;
	private ArrayList<Expression> symbolicStates;
	private ArrayList<Expression> pathConditions;
	private ArrayList<String> pathChoices;
	private String methodSignature;
		
	public PathSummary() {
		executionLog = new ArrayList<String>();
		symbolicStates = new ArrayList<Expression>();
		pathConditions = new ArrayList<Expression>();
		pathChoices = new ArrayList<String>();
	};
	
	public ArrayList<String> getExecutionLog()
	{
		return this.executionLog;
	}
	
	public void setExecutionLog(ArrayList<String> executionLog)
	{
		this.executionLog = new ArrayList<String>(executionLog);
	}
	
	public void addExecutionLog(String execLog) {
		this.executionLog.add(execLog);
	}
	
	public ArrayList<Expression> getSymbolicStates()
	{
		return this.symbolicStates;
	}
	
	public void setSymbolicStates(ArrayList<Expression> symbolicStates)
	{
		this.symbolicStates = new ArrayList<Expression>(symbolicStates);
	}
	
	public void updateSymbolicStates(Expression newEx) {
		this.symbolicStates.add(newEx);
	}
	
	public ArrayList<Expression> getPathConditions()
	{
		return this.pathConditions;
	}
	
	public void setPathConditions(ArrayList<Expression> pathConditions)
	{
		this.pathConditions = new ArrayList<Expression>(pathConditions);
	}
	
	public void updatePathConditions(Expression newCond) {
		this.pathConditions.add(newCond);
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}

	public ArrayList<String> getPathChoices() {
		return pathChoices;
	}

	public void addPathChoice(String pathChoice)
	{
		this.pathChoices.add(pathChoice);
	}
	
	public void setPathChoices(ArrayList<String> pathChoices) {
		this.pathChoices = pathChoices;
	}
	
}
