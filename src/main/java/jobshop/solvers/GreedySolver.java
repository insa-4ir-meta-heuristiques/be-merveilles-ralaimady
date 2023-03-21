package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** An empty shell to implement a greedy solver. */
public class GreedySolver implements Solver {

    /** All possible priorities for the greedy solver. */
    public enum Priority {
        SPT, LPT, SRPT, LRPT, EST_SPT, EST_LPT, EST_SRPT, EST_LRPT
    }

    /** Priority that the solver should use. */
    final Priority priority;

    /** Creates a new greedy solver that will use the given priority. */
    public GreedySolver(Priority p) {
        this.priority = p;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        ResourceOrder solution = new ResourceOrder(instance);
        TreeSet<Task> taskList = null;
        if(this.priority == Priority.SPT){
            taskList = new TreeSet<Task>(new SPT(instance));
        }else if(this.priority == Priority.LRPT){
            taskList = new TreeSet<Task>(new LRPT(instance));
        }

        //initialisation de taskList avec les premières tâches de chaque job
        for (int i=0; i< instance.numJobs; i++){
            taskList.add(new Task(i, 0));
            System.out.println(taskList.size());
        }


        while (!taskList.isEmpty()){
            Task min = taskList.first();
            taskList.remove(min);
            System.out.println(min.job + " -- " + min.task);
            solution.addTaskToMachine(instance.machine(min), min);
            if(min.task < instance.numTasks - 1) taskList.add(new Task(min.job, (min.task+1)));
        }
        return solution.toSchedule();

    }

    class SPT implements Comparator<Task>{
        private Instance instance;

        public SPT(Instance instance){
            this.instance = instance;
        }
        public int compare(Task one, Task two){
            return Integer.compare(instance.duration(one), instance.duration(two));
        }
    }

    class LRPT implements Comparator<Task>{
        private Instance instance;

        public LRPT(Instance instance){
            this.instance = instance;
        }
        public int compare(Task one, Task two){
            int remainOne = IntStream.range(one.task, instance.numTasks).mapToObj(i -> {
                return Integer.valueOf(instance.duration(one.job, i));
            }).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();
            int remainTwo = IntStream.range(two.task, instance.numTasks).mapToObj(i -> {
                return Integer.valueOf(instance.duration(two.job, i));
            }).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();

            int res = -Integer.compare(remainOne, remainTwo);
            if(res == 0){
                return Integer.compare(one.job, two.job);
            }else{
                return res;
            }
        }
    }
}
