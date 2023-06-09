package jobshop.solvers.neighborhood;

import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Implementation of the Nowicki and Smutnicki neighborhood.
 * <p>
 * It works on the ResourceOrder encoding by generating two neighbors for each block
 * of the critical path.
 * For each block, two neighbors should be generated that respectively swap the first two and
 * last two tasks of the block.
 */
public class Nowicki extends Neighborhood {

    /**
     * A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     * <p>
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     * <p>
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     */

    public class PairTask {
        final public Task firstTask;
        final public Task secondTask;

        public PairTask(Task firstTask, Task secondTask) {
            this.firstTask = firstTask;
            this.secondTask = secondTask;
        }

        public PairTask inverseTask(){
            return new PairTask(secondTask, firstTask);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PairTask pairTask1 = (PairTask) o;
            return firstTask == pairTask1.firstTask && secondTask == pairTask1.secondTask;
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstTask, secondTask);
        }
    }

    public class Pair {
        public ResourceOrder resourceOrder;
        public PairTask pairTask;

        public Pair(ResourceOrder order, PairTask pairTask) {
            this.resourceOrder = order;
            this.pairTask = pairTask;
        }
    }

    public static class Block {
        /**
         * machine on which the block is identified
         */
        public final int machine;
        /**
         * index of the first task of the block
         */
        public final int firstTask;
        /**
         * index of the last task of the block
         */
        public final int lastTask;

        /**
         * Creates a new block.
         */
        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     * <p>
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     * <p>
     * The swap with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    public static class Swap {
        /**
         * machine on which to perform the swap
         */
        public final int machine;

        /**
         * index of one task to be swapped (in the resource order encoding).
         * t1 should appear earlier than t2 in the resource order.
         */
        public final int t1;

        /**
         * index of the other task to be swapped (in the resource order encoding)
         */
        public final int t2;

        /**
         * Creates a new swap of two tasks.
         */
        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            if (t1 < t2) {
                this.t1 = t1;
                this.t2 = t2;
            } else {
                this.t1 = t2;
                this.t2 = t1;
            }
        }

        /**
         * function that inverse Swap
         */
        public Swap inverseSwap() {
            return new Swap(machine, t2, t1);
        }



        /**
         * Creates a new ResourceOrder order that is the result of performing the swap in the original ResourceOrder.
         * The original ResourceOrder MUST NOT be modified by this operation.
         */
        public ResourceOrder generateFrom(ResourceOrder original) {
            ResourceOrder order = original.copy();
            order.swapTasks(machine, t1, t2);
            return order;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Swap swap = (Swap) o;
            return machine == swap.machine && t1 == swap.t1 && t2 == swap.t2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(machine, t1, t2);
        }
    }


    @Override
    public List<ResourceOrder> generateNeighbors(ResourceOrder current) {
        // convert the list of swaps into a list of neighbors (function programming FTW)
        return allSwaps(current).stream().map(swap -> swap.generateFrom(current)).collect(Collectors.toList());

    }

    public List<Pair> generateNeighborsPairTasks(ResourceOrder current) {
        // convert the list of swaps into a list of neighbors (function programming FTW)
        return allSwaps(current).stream().map(swap -> new Pair(
                swap.generateFrom(current),
                new PairTask(
                        current.getTaskOfMachine(swap.machine, swap.t1),
                        current.getTaskOfMachine(swap.machine, swap.t2)
                )
        )).collect(Collectors.toList());
    }

    /**
     * Generates all swaps of the given ResourceOrder.
     * This method can be used if one wants to access the inner fields of a neighbors.
     */
    public List<Swap> allSwaps(ResourceOrder current) {
        List<Swap> neighbors = new ArrayList<>();
        // iterate over all blocks of the critical path
        for (var block : blocksOfCriticalPath(current)) {
            // for this block, compute all neighbors and add them to the list of neighbors
            neighbors.addAll(neighbors(block));
        }
        return neighbors;
    }

    /**
     * Returns a list of all the blocks of the critical path.
     */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {

        Schedule schedule = order.toSchedule().get();
        List<Task> critical_path = schedule.criticalPath();
        List<Block> result = new ArrayList<>();
        Task one, two;
        boolean ongoing = false;
        int start = 0, end = 0;

        for (int i = 0; i < critical_path.size() - 1; i++) {
            one = critical_path.get(i);
            two = critical_path.get(i + 1);

            if (schedule.instance.machine(one) == schedule.instance.machine(two)) {
                if (!ongoing) { // début d'un block
                    ongoing = true;
                    start = order.getIndexOrder(schedule.instance.machine(one), one);
                }
            } else { // Cas où les deux tâches i et i+1 ne sont pas sur la même machine
                if (ongoing) {  //Cas où on avait déjà un block en cours
                    end = order.getIndexOrder(schedule.instance.machine(one), one);
                    ;
                    ongoing = false;
                    result.add(new Block(schedule.instance.machine(one), start, end));
                }
            }
        }
        return result;
    }

    /**
     * For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood
     */
    List<Swap> neighbors(Block block) {
        List<Swap> result = new ArrayList<>();
        if ((block.lastTask - block.firstTask) == 1) {
            result.add(new Swap(block.machine, block.lastTask, block.firstTask));
        } else {
            result.add(new Swap(block.machine, block.firstTask + 1, block.firstTask));
            result.add(new Swap(block.machine, block.lastTask, block.lastTask - 1));
        }
        return result;
    }

}
