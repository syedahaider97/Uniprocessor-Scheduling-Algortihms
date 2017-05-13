
public class Process {

	private String processName;
	private int timeRequired;			//Time needed till completion
	private int timeEntered;			//Time the process was "created"
	private int timeRunning = 1;		//Used to measure consecutive iterations
	private int level = 0;				//Used to keep track of queue level (feedback only)

	
	//Constructor used to create processes when parsing files
	public Process(String name, int required, int enter) {
		
		processName = name;
		timeRequired = required;
		timeEntered = enter;
	}
	//Copy Constructor
	public Process(Process p) {
		processName = p.getName();
		timeRequired = p.getTimeRequired();
		timeEntered = p.getTimeEntered();
		timeRunning = p.getTimeRunning();
	}
	
	/*
	 * Getter and Setter methods
	 */
	public String getName() {
		return processName;
	}
	public int getTimeEntered() {
		return timeEntered;
	}
	public int getTimeRequired() {
		return timeRequired;
	}
	public int getTimeRunning() {
		return timeRunning;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int newLevel) {
		level = newLevel;
	}
	public void resetTimeRunning() {
		timeRunning = 1;
	}
	public void increment(){
		timeRunning++;
	}
	
	public void decrement() {
		timeRequired--;
	}

	/*
	 * For printing if necessary 
	 */
	public String toString() {
		return "Process [processName=" + processName + ", timeRequired=" + timeRequired + ", timeEntered=" + timeEntered + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processName == null) ? 0 : processName.hashCode());
		result = prime * result + timeEntered;
		result = prime * result + timeRequired;
		return result;
	}

	/*
	 * Check to see if two processes are equal to each other
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Process other = (Process) obj;
		if (processName == null) {
			if (other.processName != null)
				return false;
		} else if (!processName.equals(other.processName))
			return false;
		if (timeEntered != other.timeEntered)
			return false;
		if (timeRequired != other.timeRequired)
			return false;
		return true;
	}

}
