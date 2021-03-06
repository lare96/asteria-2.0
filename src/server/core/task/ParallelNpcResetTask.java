package server.core.task;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import server.core.Rs2Engine;
import server.world.entity.npc.Npc;

/**
 * A concurrent task that resets a single {@link Npc}.
 * 
 * @author lare96
 */
public class ParallelNpcResetTask implements Runnable {

    /** A {@link Logger} for printing debugging info. */
    private static Logger logger = Logger.getLogger(ParallelNpcResetTask.class.getSimpleName());

    /** The {@link Npc} we need to reset. */
    private Npc npc;

    /**
     * The {@link CountDownLatch} being used to keep the main game thread in
     * sync with resetting.
     */
    private CountDownLatch updateLatch;

    /**
     * Create a new {@link ParallelNpcResetTask}.
     * 
     * @param npc
     *        the {@link Npc} we need to reset.
     * @param updateLatch
     *        the {@link CountDownLatch} being used to keep the main game thread
     *        in sync with resetting.
     */
    public ParallelNpcResetTask(Npc npc, CountDownLatch updateLatch) {
        this.npc = npc;
        this.updateLatch = updateLatch;
    }

    @Override
    public void run() {

        /**
         * Put a concurrent lock on the npc we are currently resetting - so only
         * one thread in the pool can access this npc at a time.
         */
        synchronized (npc) {

            /** Now we actually reset the npc. */
            try {
                npc.reset();

                /** Handle any errors with the npc. */
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.warning(npc + " error while concurrently resetting for the next game tick!");
                Rs2Engine.getWorld().unregister(npc);

                /** Count down the latch regardless if there was an error or not. */
            } finally {
                updateLatch.countDown();
            }
        }
    }
}
