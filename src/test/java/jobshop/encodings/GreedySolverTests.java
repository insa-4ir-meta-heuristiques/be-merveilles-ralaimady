package jobshop.encodings;

import jobshop.Instance;
import jobshop.solvers.GreedySolver;
import jobshop.solvers.Solver;
import jobshop.solvers.BasicSolver;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class GreedySolverTests {

    @Test
    public void testGreedySolverSPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));

        Solver solver = new GreedySolver(GreedySolver.Priority.SPT);
        Optional<Schedule> result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.get();
        assert  schedule.isValid() : "The solution is not valid";

        System.out.println("Makespan: " + schedule.makespan());
        System.out.println("Schedule: \n" + schedule);
        System.out.println(schedule.asciiGantt());

        assert schedule.makespan() == 53 : "The greedy solver SPT should have produced a makespan of 16 for this instance.";
    }

    @Test
    public void testGreedySolverLRPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));

        Solver solver = new GreedySolver(GreedySolver.Priority.LRPT);
        Optional<Schedule> result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.get();
        assert  schedule.isValid() : "The solution is not valid";

        System.out.println("Makespan: " + schedule.makespan());
        System.out.println("Schedule: \n" + schedule);
        System.out.println(schedule.asciiGantt());

        assert schedule.makespan() == 54 : "The greedy solver LRPT should have produced a makespan of 11 for this instance.";
    }

    @Test
    public void testGreedySolverEST_SPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));

        Solver solver = new GreedySolver(GreedySolver.Priority.EST_SPT);
        Optional<Schedule> result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.get();
        assert  schedule.isValid() : "The solution is not valid";

        System.out.println("Makespan: " + schedule.makespan());
        System.out.println("Schedule: \n" + schedule);
        System.out.println(schedule.asciiGantt());

        assert schedule.makespan() == 48 : "The greedy solver SPT should have produced a makespan of 48 for this instance.";
    }

    @Test
    public void testGreedySolverEST_LRPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));

        Solver solver = new GreedySolver(GreedySolver.Priority.EST_LRPT);
        Optional<Schedule> result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.get();
        assert  schedule.isValid() : "The solution is not valid";

        System.out.println("Makespan: " + schedule.makespan());
        System.out.println("Schedule: \n" + schedule);
        System.out.println(schedule.asciiGantt());

        assert schedule.makespan() == 56 : "The greedy solver SPT should have produced a makespan of 56 for this instance.";
    }

    @Test
    //Il se peut que le test ne marche pas car avec le random on peut avoir un résultat qui n'est pas le même
    public void testGreedySolverRandom_SPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
        Solver solver = new GreedySolver(GreedySolver.Priority.SPT);

        Optional<Schedule> result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.isPresent() : "The solver did not find a solution";
        // extract the schedule associated to the solution
        Schedule schedule = result.get();
        assert  schedule.isValid() : "The solution is not valid";

        int minFound = schedule.makespan();

        for(int i = 0; i < 100; i++){
            System.out.println(" --------------- Test  "+ i+" ----------------------" );
            result = solver.solve(instance, System.currentTimeMillis() + 10);

            assert result.isPresent() : "The solver did not find a solution";
            // extract the schedule associated to the solution
            schedule = result.get();
            assert  schedule.isValid() : "The solution is not valid";

            System.out.println("Makespan: " + schedule.makespan());
            System.out.println("Schedule: \n" + schedule);
            System.out.println(schedule.asciiGantt());

            minFound = Math.min(minFound, schedule.makespan());

        }
        System.out.println("Makespan: " + minFound);

        assert minFound <= 56 : "The greedy solver SPT should have produced a makespan of 56 for this instance.";

    }
}
