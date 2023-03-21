package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;

import java.util.*;
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
        EST_SPT est_spt = new EST_SPT(instance);
        EST_LRPT est_lrpt = new EST_LRPT(instance);
        if(this.priority == Priority.SPT){
            taskList = new TreeSet<Task>(new SPT(instance));
        }else if(this.priority == Priority.LRPT){
            taskList = new TreeSet<Task>(new LRPT(instance));
        }else if(this.priority == Priority.EST_SPT){
            taskList = new TreeSet<Task>(est_spt);
        }else if(this.priority == Priority.EST_LRPT){
            taskList = new TreeSet<Task>(est_lrpt);
        }

        //initialisation de taskList avec les premières tâches de chaque job
        for (int i=0; i< instance.numJobs; i++){
            taskList.add(new Task(i, 0));
            System.out.println(taskList.size());
        }


        while (!taskList.isEmpty()){
            Task min = taskList.pollFirst();

            if(this.priority == Priority.EST_SPT){
                System.out.println("Machine Tab : " + Arrays.toString(est_spt.MachineTab));
                System.out.println("JobTab + " + Arrays.toString(est_spt.JobTab));
                int max = Math.max(est_spt.MachineTab[instance.machine(min)], est_spt.JobTab[min.job]);
                est_spt.MachineTab[instance.machine(min)] = max + instance.duration(min);
                est_spt.JobTab[min.job] = max + instance.duration(min);
            }else if(this.priority == Priority.EST_LRPT){
                System.out.println("Machine Tab : " + Arrays.toString(est_lrpt.MachineTab));
                System.out.println("JobTab + " + Arrays.toString(est_lrpt.JobTab));
                int max = Math.max(est_lrpt.MachineTab[instance.machine(min)], est_lrpt.JobTab[min.job]);
                est_lrpt.MachineTab[instance.machine(min)] = max + instance.duration(min);
                est_lrpt.JobTab[min.job] = max + instance.duration(min);
            }
            System.out.println(" Job : " + min.job + " - Task : " + min.task + " - Machine : " + instance.machine(min));
            solution.addTaskToMachine(instance.machine(min), min);
            if(min.task < instance.numTasks - 1) {
                if(this.priority == Priority.EST_SPT){
                    est_spt = new EST_SPT(instance, est_spt.MachineTab, est_spt.JobTab);
                    TreeSet<Task> task = new TreeSet<Task>(est_spt);
                    task.addAll(taskList);
                    taskList = task;
                }else if(this.priority == Priority.EST_LRPT){
                    est_lrpt = new EST_LRPT(instance, est_lrpt.MachineTab, est_lrpt.JobTab);
                    TreeSet<Task> task = new TreeSet<Task>(est_lrpt);
                    task.addAll(taskList);
                    taskList = task;
                }
                taskList.add(new Task(min.job, (min.task + 1)));
            }
            System.out.println("-----------------------------------------------------------------");
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

    class EST_SPT implements Comparator<Task>{
        private Instance instance;
        public int[] MachineTab;
        public int[] JobTab;
        public EST_SPT(Instance instance){
            this.instance = instance;
            MachineTab = new int[instance.numMachines];
            JobTab = new int[instance.numJobs];
        }

        public EST_SPT(Instance instance, int[] machineTab, int[] jobTab){
            this.instance = instance;
            MachineTab = machineTab;
            JobTab = jobTab;
        }

        public int compare(Task one, Task two){
            int result = 0;
            int valOne = Math.max(MachineTab[instance.machine(one)],JobTab[one.job]);
            int valTwo = Math.max(MachineTab[instance.machine(two)],JobTab[two.job]);

            if ( valOne == valTwo ) {
                result = Integer.compare(instance.duration(one), instance.duration(two));
            } else{
                result = Integer.compare(valOne, valTwo);
            }
            return result;
        }
    }

    class EST_LRPT implements Comparator<Task>{
        private Instance instance;
        public int[] MachineTab;
        public int[] JobTab;
        public EST_LRPT(Instance instance){
            this.instance = instance;
            MachineTab = new int[instance.numMachines];
            JobTab = new int[instance.numJobs];
        }

        public EST_LRPT(Instance instance, int[] machineTab, int[] jobTab) {
            this.instance = instance;
            MachineTab = machineTab;
            JobTab = jobTab;
        }

        public int compare(Task one, Task two){
            int result = 0;
            int valOne = Math.max(MachineTab[instance.machine(one)],JobTab[one.job]);
            int valTwo = Math.max(MachineTab[instance.machine(two)],JobTab[two.job]);

            if ( valOne == valTwo ) {
                int remainOne = IntStream.range(one.task, instance.numTasks).mapToObj(i -> {
                    return Integer.valueOf(instance.duration(one.job, i));
                }).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();
                int remainTwo = IntStream.range(two.task, instance.numTasks).mapToObj(i -> {
                    return Integer.valueOf(instance.duration(two.job, i));
                }).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();

                result = -Integer.compare(remainOne, remainTwo);
                if(result == 0){
                    result = Integer.compare(one.job, two.job);
                }
            } else{
                result = Integer.compare(valOne, valTwo);
            }
            return result;
        }
    }
}
