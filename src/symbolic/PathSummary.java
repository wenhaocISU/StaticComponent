package symbolic;

import java.io.Serializable;
import java.util.ArrayList;

public class PathSummary implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private ArrayList<String> executionLog;
	private ArrayList<Expression> symbolicStates;
	private ArrayList<Expression> pathConditions;
	
	public PathSummary() {
		executionLog = new ArrayList<String>();
		symbolicStates = new ArrayList<Expression>();
		pathConditions = new ArrayList<Expression>();
	};
	
	public void addExecutionLog(String execLog) {
		this.executionLog.add(execLog);
	}
	
	public void updateSymbolicStates(Expression newEx) {
		this.symbolicStates.add(newEx);
	}
	
	public void updatePathConditions(Expression newCond) {
		this.pathConditions.add(newCond);
	}

}
