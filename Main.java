
import java.util.Scanner;

/**
 * Main class sets up and runs the simulation,
 * including managing user input and timestep control.
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
		
        //String file = args[0];
        
		MemorySimulatorBase sim = null;
        
        Scanner input = new Scanner(System.in);
        int userChoice;
        
        System.out.println("Which algorithm would you like to run?");
        
        userChoice = menu();
        
        
        while(userChoice != 0){
            System.out.println("Please Pick One of the Folowing Input Files to Choose From:");
            System.out.println("firstnext.txt");
            System.out.println("input.txt");
            System.out.println("bestworst.txt");
            System.out.println("defrag.txt");
            String file = inputValidate(input.next());
            
            switch(userChoice){
                case 0:
                    break;
                case 1:
                    System.out.println("Running First Fit Algorithm:");
			        sim = new FirstFitMemorySimulator( file );
                    Run(sim);
                    break;
                case 2:
                    System.out.println("Running Next Fit Algorithm:");
			        sim = new NextFitMemorySimulator( file );
                    Run(sim);
                    break;
                case 3:
                    System.out.println("Running Best Fit Algorithm:");
			        sim = new BestFitMemorySimulator( file );
                    Run(sim);
                    break;
                case 4:
                    System.out.println("Running Worst Fit Algorithm:");
			        sim = new WorstFitMemorySimulator( file );
                    Run(sim);
                    break;
                default:
                    Externals.invalidUsageExit();
            }
            userChoice = menu();
        }
    }
    
    public static void Run(MemorySimulatorBase sim)
    {
		sim.timeStepUntil(0);
		sim.printMemory();
	
		int count = 0;
		while (sim.processesRemaining() != 0) {
			sim.timeStepUntil(count++);
		}
		
		System.out.println("No more events to process... ending this simulation!");
    }


    public static int getInt(Scanner input){
        while(! input.hasNextInt()){
            input.next();
            System.out.println("Not an integer try again: ");
        }
        return input.nextInt();
    }
    public static int menu(){
        int option;
        Scanner input = new Scanner(System.in);
        do{
            System.out.println();
            System.out.println("Your options are: ");
            System.out.println("-------------------");
            System.out.println("\t1) Run First Fit Algorithm");
            System.out.println("\t2) Run Next Fit Algorithm");
            System.out.println("\t3) Run Best Fit Algorithm");
            System.out.println("\t4) Run Worst Fit Algorithm");
            System.out.println("\t0) EXIT");
            System.out.println("\n Please enter your option: ");
            option = getInt(input);
            System.out.println();
        } while(option < 0 || option > 4);
        return option;
    }
    public static String inputValidate(String fileName){
        Scanner input = new Scanner(System.in);
        while(!(fileName.equals("input.txt") || fileName.equals("firstnext.txt") || fileName.equals("bestworst.txt") || fileName.equals("defrag.txt"))){
            System.out.println("Incorrect file name. Please pick one of the following files:");
            System.out.println("firstnext.txt");
            System.out.println("input.txt");
            System.out.println("bestworst.txt");
            System.out.println("defrag.txt");
            fileName = input.next();
        }
        return fileName;
    }
}
