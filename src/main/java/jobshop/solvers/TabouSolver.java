package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;

import java.util.*;

/** An empty shell to implement a descent solver. */
public class TabouSolver implements Solver {

    final Nowicki neighborhood;
    final Solver baseSolver;
    private int maxIter;
    private int dureeTabou;

    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public TabouSolver(Neighborhood neighborhood, Solver baseSolver, int maxIter, int dureeTabou) {
        this.neighborhood = (Nowicki) neighborhood;
        this.baseSolver = baseSolver;
        this.maxIter = maxIter;
        this.dureeTabou = dureeTabou;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {

        Optional<Schedule> schedule = baseSolver.solve(instance,deadline);
        Optional<Schedule> solution = schedule;
        //Nowicki.Swap bestSwap = null;
        Nowicki.PairTask bestPairTasks = null;
        Optional<Schedule> optimal = schedule;
        int currentIter = 0;

        //Deque<Nowicki.Swap> listSwapTabou = new ArrayDeque<>();
        Deque<Nowicki.PairTask> listTasksTabou = new ArrayDeque<>();

        do {
            ResourceOrder initial = new ResourceOrder(schedule.get());
            //List<ResourceOrder> neighborsList = neighborhood.generateNeighbors(initial);
            List<Nowicki.Pair> neighborsList = neighborhood.generateNeighborsPairTasks(initial);
            int min = Integer.MAX_VALUE;

            for (Nowicki.Pair temp: neighborsList){
                Optional<Schedule> tmpS = temp.resourceOrder.toSchedule();
                if(tmpS.isPresent() && tmpS.get().isValid()){ // vérifie la validité du ressource order
                    if(tmpS.get().makespan()<min  && !listTasksTabou.contains(temp.pairTask)) {
                        solution = tmpS;
                        min = tmpS.get().makespan();
                        bestPairTasks = temp.pairTask;
                    }
                    /*else if(tmpS.get().makespan()<optimal.get().makespan()){ //version 3
                        solution = tmpS;
                        min = tmpS.get().makespan();
                        bestPairTasks = temp.pairTask;
                    }*/
                }
            }

            if(bestPairTasks != null) {
                listTasksTabou.offerFirst(bestPairTasks.inverseTask());
                if (listTasksTabou.size()==dureeTabou) listTasksTabou.pollLast();
            }

            if (solution.get().makespan()<optimal.get().makespan()){
                optimal = solution;

            }
             schedule = solution;
            currentIter++;
        }while(currentIter < maxIter || System.currentTimeMillis() < deadline);

        return optimal;
    }

}
