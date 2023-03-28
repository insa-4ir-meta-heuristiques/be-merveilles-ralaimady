package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;

import java.util.List;
import java.util.Optional;

/** An empty shell to implement a descent solver. */
public class TabouSolver implements Solver {

    final Neighborhood neighborhood;
    final Solver baseSolver;
    private int maxIter;

    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public TabouSolver(Neighborhood neighborhood, Solver baseSolver, int maxIter) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
        this.maxIter = maxIter;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {

        Optional<Schedule> schedule = baseSolver.solve(instance,deadline);
        Optional<Schedule> solution = schedule;
        Optional<Schedule> optimal = schedule;
        int currentIter = 0;
        do {
            ResourceOrder initial = new ResourceOrder(schedule.get());
            List<ResourceOrder> neighborsList = neighborhood.generateNeighbors(initial);
            int min = Integer.MAX_VALUE;

            for (ResourceOrder temp: neighborsList) {
                Optional<Schedule> tmpS = temp.toSchedule();
                if(tmpS.isPresent() && tmpS.get().isValid()){ // vérifie la validité du ressource order
                    if(tmpS.get().makespan()<min){ //vérifie si le makespan est meilleur
                        solution = tmpS;
                        min = tmpS.get().makespan();
                    }
                }
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
