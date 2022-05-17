
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Starter code for a memory simulator.
 * Simulator strategies extend this abstract class.
 */
public abstract class MemorySimulatorBase {

    protected static final int MEM_SIZE = 560;

	protected static final char FREE_MEMORY = '.';
	protected static final char RESERVED_MEMORY = '#';
	protected int CURRENT_TIME = -1;
	
	protected char[] main_memory;
	protected ArrayList<Process> processes;
	
	protected static final boolean MEMSIM_DEBUG = true;
    
    
	private int timeAdded;
	
	/**
	 * Default constructor that takes an input file
	 * @param fileName
	 */
	public MemorySimulatorBase(String fileName) {
    
		main_memory = new char[ MEM_SIZE ];
        
		processes = InputFileParser.parseInputFile( fileName );
        
		initializeMainMemory();
        
		for (Process p : processes) {
			debugPrintln("Process " + p.getPid() + " (size " + p.getSize() + ")");
			debugPrintln("  Start Time: " + p.getStartTime());
			debugPrintln("  End Time: " + p.getEndTime());
		}
	}
	
	/**
	 * Return the index of the first position of the next available slot
	 * in memory
	 * 
	 * Different memory strategy classes must override this abstract method.
	 * @param slotSize The size of the requested slot
	 * @return The index of the first position of an available requested block
	 */
	protected abstract int getNextSlot(int slotSize);
	
	/**
	 * Move the simulator one virtual time step into the future,
	 * handling processes leaving and entering the system.
	 * NOTE: Not used now that the project specifications have changed.
	 */
	public void timeStep() {
		CURRENT_TIME++;
		while (!eventOccursAt(CURRENT_TIME)) {
			debugPrintln("Fast-forwarding past boring time " + CURRENT_TIME);
			CURRENT_TIME++;
		}
		
		debugPrintln("=========== TIME IS NOW " + CURRENT_TIME + " ============");
		
		//Processes exit the system
		ArrayList<Process> toRemove = new ArrayList<Process>();
		for (Process p : processes) {
			if (p.getEndTime() == CURRENT_TIME) {
				debugPrintln("Removing process " + p.getPid());
				removeFromMemory(p);
				toRemove.add(p);
			}
		} 
		for (Process p : toRemove) {
			processes.remove(p);
		}
		
		//Processes enter the system
		for (Process p : processes) {
			if (p.getStartTime() == CURRENT_TIME) {
				debugPrintln("Adding process " + p.getPid());
				putInMemory(p);
			}
		}
	}

	/**
	 * Move the simulator into the future
	 * @param t The time to which to move the simulator
	 */
	public void timeStepUntil(int t) {
		while (CURRENT_TIME < t) {
			CURRENT_TIME++;
			while (!eventOccursAt(CURRENT_TIME) && CURRENT_TIME < t) {
				debugPrintln("Fast-forwarding past boring time " + CURRENT_TIME);
				CURRENT_TIME++;
			}
			
			debugPrintln("=========== TIME IS NOW " + CURRENT_TIME + " ============");
			
			//Processes exit the system
			ArrayList<Process> toRemove = new ArrayList<Process>();
			for (Process p : processes) {
				if (p.getEndTime() == CURRENT_TIME) {
					debugPrintln("Removing process " + p.getPid());
					removeFromMemory(p);
					toRemove.add(p);
				}
			} 
			for (Process p : toRemove) {
				processes.remove(p);
			}
			
			//Processes enter the system
			for (Process p : processes) {
				if (p.getStartTime() == CURRENT_TIME) {
					debugPrintln("Adding process " + p.getPid());
					putInMemory(p);
				}
			}
            if(eventOccursAt(CURRENT_TIME))
				printMemory();
		}
	}
	
	/**
	 * Find whether an event occurs at a specific time
	 * Useful for ascertaining if we can skip a time in the simulator
	 * @param time The time we should check to see if an event occurs
	 * @return True if an event occurs at time, else false
	 */
	private boolean eventOccursAt(int time) {
		for (Process p : processes) {
			if (p.getStartTime() == time || p.getEndTime() == time) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Put a process into memory
	 * @param p The process to put into memory
	 */
	protected void putInMemory(Process p) {
		int targetSlot = getNextSlot(p.getSize());
		if (targetSlot == -1) {
			defragment();
			targetSlot = getNextSlot(p.getSize());
			if (targetSlot == -1) {
				Externals.outOfMemoryExit();
			}
		}
		debugPrintln("Got a target slot of " + targetSlot + " for pid " + p.getPid());
		//If we get here, we know that there's an open chunk
		for (int i = 0; i < p.getSize(); i++) {
			main_memory[i+targetSlot] = p.getPid();
		}
	}
	
	/**
	 * Take a process out of memory
	 * @param p The process to remove from memory
	 */
	protected void removeFromMemory(Process p) {
		for (int i = 0; i < main_memory.length; i++) {
			if (main_memory[i] == p.getPid()) {
				main_memory[i] = FREE_MEMORY;
			}
		}
	}

	/**
	 * Initialize our main memory with the predetermined amount of reserved and
	 * free memory 
	 */
	private void initializeMainMemory() {
		for (int i = 0; i < 80 && i < main_memory.length; i++) {
			main_memory[i] = RESERVED_MEMORY;
		}
		for (int i = 80; i < main_memory.length; i++) {
			main_memory[i] = FREE_MEMORY;
		}
	}

	/**
	 * Print the current contents of memory
	 */
	public void printMemory() {
		System.out.print("Memory at time " + CURRENT_TIME + ":");
		for (int i = 0; i < main_memory.length; i++) {
			if (i % 80 == 0) {
				System.out.println("");
			}
			System.out.print( main_memory[i] + "" );
		}
		System.out.println("");
	}
	
	/**
	 * Attempt to defragment main memory
	 */
	private void defragment() {
		HashMap<Character, Integer> processesMoved = new HashMap<Character, Integer>();
		DecimalFormat f = new DecimalFormat("##.00");
		
		System.out.println("Performing defragmentation...");
		
		int destination = 80;
		for (int i = 0; i < main_memory.length; i++) {
			if (main_memory[i] != FREE_MEMORY 
					&& main_memory[i] != RESERVED_MEMORY
					&& i != destination ) {
				main_memory[destination] = main_memory[i];
				main_memory[i] = FREE_MEMORY; 
				destination++;
				processesMoved.put(main_memory[i], null);
			}
		}
		int numMoved = processesMoved.size();
		int freeBlockSize = main_memory.length - destination;
		double percentage = (double)freeBlockSize / (double)main_memory.length;
	
		System.out.println("Defragmentation completed.");
		System.out.println("Relocated " + numMoved + " processes " +
				"to create a free memory block of " + freeBlockSize + " units " +
				"(" + f.format(percentage * 100) + "% of total memory).");
	}
	
	/**
	 * Print a string if a debug flag is set.
	 * Do not include a newline.
	 * @param toPrint The string to print
	 */
	private static void debugPrint(String toPrint) {
		if (MEMSIM_DEBUG == true) {
			System.out.print(toPrint);
		}
	}

	/**
	 * Print a string if a debug flag is set.
	 * Include a newline.
	 * @param toPrint The string to print
	 */
	private static void debugPrintln(String toPrint) {
		if (MEMSIM_DEBUG == true) {
			System.out.println(toPrint);
		}
	}
	
	/**
	 * Get the number of processes with events remaining in the simulator
	 * @return The number of processes with events remaining in the simulator
	 */
	public int processesRemaining() {
		return processes.size();
	}
	
}
