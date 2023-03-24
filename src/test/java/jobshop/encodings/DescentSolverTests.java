package jobshop.encodings;

import jobshop.Instance;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.GreedySolver;
import jobshop.solvers.Solver;
import jobshop.solvers.neighborhood.Nowicki;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class DescentSolverTests {

    @Test
    public void testDescentSolverSPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));

        Solver sptSolver = new GreedySolver(GreedySolver.Priority.SPT);
        Solver solver = new DescentSolver(new Nowicki(), sptSolver);
        Optional<Schedule> result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.get();
        assert  schedule.isValid() : "The solution is not valid";

        System.out.println("Makespan: " + schedule.makespan());
        System.out.println("Schedule: \n" + schedule);
        System.out.println(schedule.asciiGantt());

        assert schedule.makespan() <= 53 : "The greedy solver SPT should have produced a makespan of 16 for this instance.";
    }


}
