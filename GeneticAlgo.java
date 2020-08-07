import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

//this class represents the genetic algorithm approach to find hamiltonian path

public class GeneticAlgo {

    //this is the initial population size which is  maintained throughout
    private static int pop_size = 30;

    //this is the length of a chromosome. this value depends on the input graph
    //because the length of the Hamiltonian path depends on number of vertices in a graph
    private static int chrom_size;

    //mutation rate is set to be 30%
    private static double mutation_rate = 0.3;

    //chromosome rate is set to be 40%
    private static double crossover_rate = 0.4;

    //this is max generation, after which the program terminates
    //this value can be set to less than 1000 for small graphs and more for very large graph
    private static int max_gen = 10000;

    //population array
    private static int[][] population;

    //array to store fitness of each chromosome in current population pool
    private static int[] each_chrom_fitness = new int[pop_size];

    //boolean variable to specify if solution is found
    private static boolean solution_found = false;


    public static void main(String[] args) {
        // TODO Auto-generated method stub

        long start_time = System.nanoTime();

        //this function takes the hard-coded input graph and sets the chromosome size as well as population size
        int[][] graph = inputGraph();

        //calling findHamiltonianCycle function that returns a hamiltonian path if any
        int solution = findHamiltonianCycle(graph);

        if(solution_found) {
            displaySolution(solution);
        } else {
            System.out.println("It is highly probably that the solution does not exists.");
        }

        long end_time = System.nanoTime();

        System.out.println("\n\nThe program took: " + ((end_time-start_time)/1000000)+ "miliseconds.");

    }


    //this function reads graph input from file
    private static int[][] inputGraph() {

        int[][] graph = null;

        //String path = System.getProperty("user.dir") + "/graph_input.txt/";
        //String path = "D:\\GA3\\src\\input1.txt";
        //String path = "D:\\GA3\\src\\input2.txt";
        String path = "D:\\GA3\\src\\input3.txt";

        File file = new File(path);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String line;
        try {


            int i=0;

            line = br.readLine();
            String[] str = line.split(",");
            graph = new int[str.length][str.length];

            while(line != null) {

                str = line.split(",");
                int j=0;
                for(String s: str) {
                    graph[i][j] = Integer.parseInt(s);
                    j++;
                }
                i++;
                line = br.readLine();

            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        try {
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        chrom_size = graph.length +1;
        population = new int[pop_size][chrom_size];

        return graph;
    }


    //the GeneticAlgo function that drives the program
    public static int findHamiltonianCycle(int graph[][]) {

        //generating the random initial population where each chromosome represents a possible solution array
        initialPopulation();

        int fittest_chrom = 0;

        for(int generation = 0; generation<=max_gen; generation++) {

            //assigning fitness to each chromosome using assignFitness function
            each_chrom_fitness = assignFitness(graph);

            //get fittest member using a function
            fittest_chrom = getFittestChrom();

            if(generation%100 == 0) {
                System.out.println("\n\nThis is generation: " + generation +"\n");

                //printing the fittest chromosome
                OrderCrossover.printArray(population[fittest_chrom]);
                System.out.println("\nThe fittest chromosome is chromosome number: "+fittest_chrom +
                        ", with fitness value " + each_chrom_fitness[fittest_chrom] + "\n");
            }

            if(each_chrom_fitness[fittest_chrom] == chrom_size - 1) {
                //solution found

                System.out.println("\n\nThis is generation: " + generation +"\n");

                OrderCrossover.printArray(population[fittest_chrom]);
                System.out.println("\nThe fittest chromosome is chromosome number: "+fittest_chrom +
                        ", with fitness value " + each_chrom_fitness[fittest_chrom] + "\n");

                solution_found = true;
                return fittest_chrom;

            }else {
                //function where population is evolved, a.k.a crossover and mutation is performed
                evolve_population();
            }

        }

        return -1;

    }


    //function to perform crossover and mutation
    public static void evolve_population() {
        int new_population[][] = new int[pop_size][chrom_size];

        int a = 0, b = 0;
        int winner, loser;

        for(int i=0; i<pop_size; i++) {

            //using roulette wheel method to select two parent chromosomes
            do {
                a = select_chrom_using_roulette_wheel();
                b = select_chrom_using_roulette_wheel();
            }while(a==b);


            if(each_chrom_fitness[a] > each_chrom_fitness[b]) {
                winner = a;
                loser = b;
            } else {
                loser = a;
                winner = b;
            }

            int[] temp_chrom = population[winner];

            //performing ordered crossover with crossover rate of 30%
            if(randomDoubleZeroToOne() < crossover_rate) {
                temp_chrom = OrderCrossover.crossover(population[winner], population[loser]);
            }

            new_population[i] = temp_chrom;
        }

        //looping through the chromosomes and performing swap mutation with mutation rate of 40%
        for(int i=0; i<pop_size; i++) {
            if(randomDoubleZeroToOne() < mutation_rate) {
                new_population[i] = performSwapMutation(new_population[i]);
            }
        }


        population = new_population;

    }

    //function to perform swap mutation
    private static int[] performSwapMutation(int[] parent) {

        int[] clone_parent = parent.clone();
        int len = clone_parent.length;

        int r1 = randomNumberBetween(1,len-1);
        int r2 = randomNumberBetween(1,len-1);

        while(r1==r2) {
            r2 = randomNumberBetween(1,len-1);
        }

        int temp = clone_parent[r2];
        clone_parent[r2] = clone_parent[r1];
        clone_parent[r1] = temp;


        return clone_parent;
    }


    //function to select two chromosome parents using roulette wheel method
    private static int select_chrom_using_roulette_wheel() {

        int fitness_sum = 0;
        for(int i=0; i<pop_size; i++) {
            fitness_sum = fitness_sum + each_chrom_fitness[i];
        }

        Random rand = new Random();
        int pick_number = rand.nextInt(fitness_sum);

        int partial_fitness_sum = 0;

        for(int i=pop_size-1; i>=0; i--) {
            partial_fitness_sum += each_chrom_fitness[i];
            if(partial_fitness_sum > pick_number) {
                return i;
            }
        }

        return -1;
    }


    //function to get the fittest chromosome in the current population pool
    public static int getFittestChrom() {
        int fittest = 0;
        for (int i=0; i<pop_size; i++) {

            if(each_chrom_fitness[i] > each_chrom_fitness[fittest]) {
                fittest = i;
            }
        }

        return fittest;
    }



    //assigning fitness to all the chromosomes in current population pool
    //it is to be noted that if the fitness of a chromosome is equal to number of vertices in graph + 1 than
    //we have found the hamiltonian path
    private static int[] assignFitness(int graph[][]) {
        int[] chrom_fitness = new int [pop_size];

        for(int i=0; i<pop_size; i++) {

            chrom_fitness[i] = 0;

            for(int j=1; j<chrom_size; j++) {

                //check if edge between two adjacent vertex exists or not
                //if exist increase the fitness value
                if(graph[population[i][j]][population[i][j-1]] == 1) {
                    chrom_fitness[i]++;
                }
            }
        }


        return chrom_fitness;
    }


    //generating initial population randomly
    private static void initialPopulation() {

        for(int i=0; i<pop_size; i++) {

            for(int j=0; j<chrom_size; j++) {

                if(j==0 || j==chrom_size-1) {
                    population[i][j] = 0;
                }else {

                    int chrom_val = randomNumber();
                    int check = 0;

                    while(true) {
                        for(int a=0; a<j; a++) {
                            if(population[i][a] == chrom_val) {
                                check = 1;
                            }
                        }

                        if (check == 1) {
                            chrom_val =  randomNumber();
                            check = 0;
                        }else {
                            population[i][j] = chrom_val;
                            break;
                        }
                    }

                }

            }
        }

    }

    public static void displaySolution(int sol) {
        System.out.print("Hamiltonian Cycle Exists! The path is: \n\n");
        for(int i=0; i<chrom_size; i++) {
            System.out.print(population[sol][i] + " ");
        }

    }

    private static int randomNumber() {
        Random rand = new Random();

        return rand.nextInt(1000)% (chrom_size-1);
    }

    private static double randomDoubleZeroToOne() {
        Random rand = new Random();

        return rand.nextInt(1000) / 1000.0;
    }

    private static int randomNumberBetween(int min, int max) {
        Random rand = new Random();
        return min + rand.nextInt(max-min);
    }

    public static boolean arrayContains(int[] arr, int e){
        for(int i = 0; i < arr.length; i++){
            if(arr[i] == e)
                return true;
        }
        return false;
    }
}
