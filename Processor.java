import java.io.*;
import java.util.ArrayList;

public class Processor {
	
	/*
	 * Creation of multiple ArrayLists to read in and hold information from the input files. 
	 * processList holds the original set, and the following 4 are copies used for 
	 * their specified algorithm.
	 * 
	 */

	static ArrayList<Process> processList = new ArrayList<Process>();
	static ArrayList<Process> robinList = new ArrayList<Process>();
	static ArrayList<Process> HRRNList = new ArrayList<Process>();
	static ArrayList<Process> feedbackList = new ArrayList<Process>();
	static ArrayList<Process> SPNList = new ArrayList<Process>();
	static int duration = 0;
	

	public static void main(String args[]) {
		int totalTime = 0;
		
		/*
		 * This block of code attempts to open and then read the file upon success. 
		 * Following, the information is immediately sifted into the processList ArrayList
		 * that will be used to execute the processes.
		 */
		BufferedReader reader = null;
		try {
			File file = new File("Process.csv");
			reader = new BufferedReader(new FileReader(file));

			String line;
			while ((line = reader.readLine()) != null) {

				String[] processData = line.split(",");
				processList.add(new Process(processData[0], Integer.parseInt(processData[1]),
						Integer.parseInt(processData[2])));
				totalTime += Integer.parseInt(processData[1]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * The code below takes the allocated information, and copies it into different ArrayLists 
		 * to be used with their respective algorithms.
		 */

		for (Process p: processList) {
			System.out.println(p.toString());
			robinList.add(new Process(p));
			HRRNList.add(new Process(p));
			feedbackList.add(new Process(p));
			SPNList.add(new Process(p));
		}

		/*
		 * This block of code creates an instance of the current class, Processor, 
		 * and then uses itself to synchronously generate threads. As we will see below, the
		 * algorithms are implemented as inner classes that implement the runnable interface.
		 * This allows an equivalent distribution in priority, which then return seamless concurrency.
		 * 
		 */
		
		Processor threadFactory = new Processor();
		Processor.RoundRobin thread1 = threadFactory.new RoundRobin();
		Processor.HRRN thread2 = threadFactory.new HRRN();
		Processor.FeedBack thread3 = threadFactory.new FeedBack();
		Processor.SPN thread4 = threadFactory.new SPN();
		
		/*
		 * So long as the elapsed time is less then the total time allocated to run,
		 * continually run each algorithm until completion.
		 *  
		 */
		
		while (duration < totalTime) {

		
			thread1.run();
			       
			thread2.run();
			
			thread3.run();
			
			thread4.run();
			
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~GAP~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			
			duration++;	
			
		}
	}
	

	public class RoundRobin implements Runnable {
		
		ArrayList<Process> queue = new ArrayList<Process>();
		ArrayList<String> results = new ArrayList<String>();
		Process recent;
		public void run() {
			/*
			 * Systematically adds processes to the queue based on their arrival time,
			 * and how recently they were run relative to the other processes.
			 * 
			 */
			boolean skip = false;
			for (Process p : robinList) {
				if (duration >= p.getTimeEntered() && !queue.contains(p)) {
					if(p != null && p.equals(recent)) {
						skip = true;
						continue;
					}
					queue.add(p);
				}
			}
			
			if(skip) queue.add(recent);
			String temp = roundRobin(queue);
			if(temp != null) results.add(temp);
			
			/*
			 * Display used to output results of executed processes.			 * 
			 */
			
			System.out.println("ROUND ROBIN AT TIME: " + duration);
			for(int i = 0; i < results.size(); i++) {
				
				if(results.size() - i == 1) {
					System.out.println(results.get(i));
					break;
				}
				System.out.print(results.get(i) +", ");
					
			}
		}
		
		
		public String roundRobin(ArrayList<Process> queue) {
	
			int timeQuantam = 1;
			
			/*
			 * Run a process until either completion, or until its Time Quantum expires
			 * If completed, remove it from the queue after which it will never be added again
			 * (Due to the time running component), or temporarily remove it and have it so
			 * that when it is added again, push it to the end.
			 * 
			 */
			for (Process p : queue) {
				if (p.getTimeRequired() > 0) {
					p.decrement();
					if (p.getTimeRunning() % timeQuantam == 0 && p.getTimeRunning() != 0) {
						queue.remove(p);
						recent = p;
					}
					p.increment();
					return p.getName();
				} else {
					if (robinList.contains(p)) 
						robinList.remove(p);
				}
			}
			return null;
		}
	}
	
	public class HRRN implements Runnable {

		ArrayList<Process> queue = new ArrayList<Process>();
		ArrayList<String> results = new ArrayList<String>();
		Process running;
		
		public void run() {
			
			/* 
			 *  Checks to make sure a process is not already in the queue,
			 *  and then systematically adds them based on their arrival time.
			 *  
			 */
			for(Process p: HRRNList) {
				if(duration >= p.getTimeEntered() && !queue.contains(p)) {
					queue.add(p);
				}
			}
			String temp = hrrn(queue);
			if(temp != null) results.add(temp);
			
			/*
			 * Display used to output results of executed processes.
			 */
			
			System.out.println("HRRN at time " + duration);
			for(int i = 0; i < results.size(); i++) {
							
				if(results.size() - i == 1) {
					System.out.println(results.get(i));
					break;
				}
				System.out.print(results.get(i) +", ");
			}
		}
		
		public String hrrn(ArrayList<Process> queue) {
			int maxRatio = 0;
			/*
			 * If a process has completed, remove it both from the queue, and the
			 * process list. Reset the maxRatio to 0.
			 */
			 if(running != null && running.getTimeRequired() == 0) {
				HRRNList.remove(running);
				queue.remove(running);
				maxRatio = 0;
			}
			 /*
			  * If no process has been marked to run, determine which one has the highest 
			  * time entered - time required ratio, and choose it to run till completion.
			  * 
			  */
			if(running == null || running.getTimeRequired() <= 0) {
				for(Process p: queue) {
					int waitTime = (p.getTimeRequired() + (duration - p.getTimeEntered())) / p.getTimeRequired();
					if(waitTime > maxRatio) {
						running = p;
						maxRatio = waitTime;
					}
				}
			}
			/*
			 * Run till completion.
			 */
			if(running != null && running.getTimeRequired() > 0) {
				running.decrement();
				return running.getName();
			}
			return null;
		}
	}
	
	public class FeedBack implements Runnable {
		
		ArrayList<ArrayList<Process>> multiLevel = new ArrayList<ArrayList<Process>>();
		ArrayList<String> results = new ArrayList<String>();
		int timeQuantam = 1;
		Process running;
		public void run() {
			/*
			 * Used in the first iteration to add a top layer to my queue of queues.
			 */
			if (multiLevel.isEmpty()) {
				multiLevel.add(new ArrayList<Process>());
			}
			
			/*
			 * Run through the original list of processes, and check to see if one is available to run.
			 * Then go through the entire queue, and make sure that that process is not already accounted for.
			 * If both checks are passed, add a process to the top level queue.
			 */
			for(Process p : feedbackList) {
				boolean inside = false;
				if(duration >= p.getTimeEntered()) {
					for(ArrayList<Process> level : multiLevel) {
						for(Process P : level) {
							if(p.equals(P)) {
								inside = true;
							}
						}
					}
				}
				/*
				 * Add to the top level queue.
				 */
				if(!inside && duration >= p.getTimeEntered()){
					multiLevel.get(0).add(p);
				}
			}
			
			String temp = feedback(multiLevel);
			if(temp != null) results.add(temp);
			
			/*
			 * Display used to output results of executed processes.			 * 
			 */
			
			System.out.println("Feedback at time " + duration);
			for(int i = 0; i < results.size(); i++) {
							
				if(results.size() - i == 1) {
					System.out.println(results.get(i));
					break;
				}
				System.out.print(results.get(i) +", ");
			}
		}
			
		
		public String feedback(ArrayList<ArrayList<Process>> queue) {
		
			for(ArrayList<Process> level : queue) {
				
				/*
				 * Go level by level, and find the first available process to be run.
				 * Determine how long it has run, and check to see if it necessary to push it down a level.
				 * If it is necessary, remove it from the current level first and then shift down. 
				 * If the level does not exist, create it within the multilevel queue first. if it does,
				 * simply bring it down.
				 * 
				 */
				
				for(Process p : level) {
					
					if(p.getTimeRequired() > 0) {
						p.decrement();
						//Check if necessary to push it down
						if(p.getTimeRunning() % timeQuantam == 0) {
							//Create if new level does not exist
							if(queue.indexOf(level) == queue.size() - 1) {
								queue.add(new ArrayList<Process>());
							}
							//Remove from current level
							level.remove(p);
							//Algorithmically determine the next level
							int index = queue.indexOf(level) + 1;
							for(int i = 0; i < queue.size() && p.getLevel() > index; i++) {
								if(queue.get(i).equals(level)) {
									index = i + 1;
								}
							}
							queue.get(index).add(p);
							p.setLevel(index);
							p.resetTimeRunning();
							return p.getName();
						}
						p.increment();
						return p.getName();
					}
				}
			}
			return null;
		}

	}
	public class SPN implements Runnable {
		
		ArrayList<Process> queue = new ArrayList<Process>();
		ArrayList<String> results = new ArrayList<String>();
		Process running;
		public void run() {
			/* 
			 *  Checks to make sure a process is not already in the queue,
			 *  and then systematically adds them based on their arrival time.
			 *  
			 */
			for(Process p : SPNList) {
				if(duration >= p.getTimeEntered() && !queue.contains(p)) {
					queue.add(p);
				}
			}
			String temp = spn(queue);
			if(temp != null) results.add(temp);
			
			/*
			 * Display used to output results of executed processes.			 * 
			 */
			
			System.out.println("Shortets Process Next at time " + duration);
			for(int i = 0; i < results.size(); i++) {
							
				if(results.size() - i == 1) {
					System.out.println(results.get(i));
					break;
				}
				System.out.print(results.get(i) +", ");
			}		
		}
		
		public String spn(ArrayList<Process> queue) {
			int min = Integer.MAX_VALUE;
			/*
			 * If a process has completed, remove it both from the queue, and the
			 * process list.
			 */
			if(running != null && running.getTimeRequired() == 0) {
				queue.remove(running);
				SPNList.remove(running);
			}
			 /*
			  * If no process has been marked to run, determine which one has the 
			  * shortest time required, and choose it to run till completion.
			  * 
			  */
			if(running == null || running.getTimeRequired() <= 0) {
				for(Process p : queue) {
					if(p.getTimeRequired() < min) {
						min = p.getTimeRequired();
						running = p;
					}
				}
			}
			/*
			 * Run till completion
			 */
			if(running != null && running.getTimeRunning() > 0) {
				running.decrement();
				return running.getName();
			}
			return null;
		}
	}
	
}
