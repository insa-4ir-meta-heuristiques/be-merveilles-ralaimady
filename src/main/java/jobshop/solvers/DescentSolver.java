package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;

import java.util.List;
import java.util.Optional;

/** An empty shell to implement a descent solver. */
public class DescentSolver implements Solver {

    final Neighborhood neighborhood;
    final Solver baseSolver;

    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public DescentSolver(Neighborhood neighborhood, Solver baseSolver) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        Optional<Schedule> schedule = baseSolver.solve(instance,deadline);
        ResourceOrder initial = new ResourceOrder(schedule.get());
        List<ResourceOrder> neighborsList = neighborhood.generateNeighbors(initial);
        Optional<Schedule> solution = schedule;
        int min = schedule.get().makespan();

        for (ResourceOrder temp: neighborsList) {
            Optional<Schedule> tmpS = temp.toSchedule();
            if(tmpS.isPresent() && tmpS.get().isValid()){ // vérifie la validité du ressource order
                if(tmpS.get().makespan()<min){ //vérifie si le makespan est meilleur
                    solution = tmpS;
                    min = tmpS.get().makespan();
                }
            }
        }
        return solution;
    }

}
