// stations are labelled from 0 to 4
// with 0 being the bottommost station and 4 being the topmost station
// semaphore index - even for track 0 and odd for track 1
// semaphore index for station i for left track -> 4 * i, right track -> 4 * i + 1
// starting points for trains
// t1, t2, t3 -> left track, station 0-> semaphore0
// t4, t5 -> right track, station 0 => semaphore1

import java.util.*;
import java.util.concurrent.Semaphore;

public class TrainSimulator190101103{
    public static void main(String[] args){
        Date startDate = new Date();
        long programStart = startDate.getTime();

        Semaphore[] semaphores = new Semaphore[18];
        for (int i = 0; i < 18; i++){
            // 4k and 4k+1 form for i represent the station where there is space for trains to stop
            if (i%4 == 0 || (i-1)%4 == 0) semaphores[i] = new Semaphore(5);
            else semaphores[i] = new Semaphore(1);
        }
        // creation of train threads
        Train train1 = new Train(1, semaphores, 0, false, programStart);
        Thread train1_thread = new Thread(train1);
        train1_thread.start();

        Train train2 = new Train(2, semaphores, 0, false, programStart);
        Thread train2_thread = new Thread(train2);
        train2_thread.start();

        Train train3 = new Train(3, semaphores, 0, false, programStart);
        Thread train3_thread = new Thread(train3);
        train3_thread.start();

        Train train4 = new Train(4, semaphores, 1, false, programStart);
        Thread train4_thread = new Thread(train4);
        train4_thread.start();

        Train train5 = new Train(5, semaphores, 1, false, programStart);
        Thread train5_thread = new Thread(train5);
        train5_thread.start();
    }
}

class Train implements Runnable{
    // starting time of the program
    private long programStart;

    // trackNum can be 0 or 1
    private int trackNum;

    public static int NAP_TIME = 3000; 

    // Denotes whether the train in question is travelling down or up the track
	private boolean goingDown;

	// The id of the train
	private int id;

	// The current semaphore where the train is
	private int semaphore;

    // A global field of semaphores
	private Semaphore[] semaphores;

    public Train(int id, Semaphore[] semaphores, int initialState, boolean goingDown, long programStart) {
		this.id = id;
		this.semaphores = semaphores;
        this.semaphore = initialState;
        this.goingDown = goingDown;
        this.programStart = programStart;
        acquire(initialState);
        if (semaphore % 2 == 0) this.trackNum = 0;
        else this.trackNum = 1;
	}

    private void acquire(int semaphore){
        try {
			this.semaphores[semaphore].acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
    }

    // Releases the current semaphore
	private void release(int semaphore) {
		this.semaphores[semaphore].release();
	}

    private void reverseDirection(){
        if (this.goingDown == true) this.goingDown = false;
        else this.goingDown = true;
    }

    private static void nap(int millisecs) {
		try {
			Thread.sleep(Math.abs(millisecs));
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

    @Override
    public void run(){
        while (true){
            if ((semaphore == 0 || semaphore == 1) && goingDown == true){
                reverseDirection();
                continue;
            }
            if ((semaphore == 16 || semaphore == 17) && goingDown == false){
                reverseDirection();
                continue;
            }

            // find out the next two states in which the train can transition into
            // semaphore_same is the semaphore on the same track on which the train can possibly enter
            int semaphore_same = -1, semaphore_diff = -1;
            if (this.goingDown){
                semaphore_same = semaphore - 2;
            }
            else{
                semaphore_same = semaphore + 2;
            }

            if (semaphore_same % 2 == 0){
                semaphore_diff = semaphore_same + 1;
            }
            else{
                semaphore_diff = semaphore_same - 1;
            }

            
            if (semaphore%4 == 0 || (semaphore-1)%4 == 0){
                // train is currently at a station, thus track change can take place
                int stationNum = semaphore / 4;
                if (this.semaphores[semaphore_same].tryAcquire()){
                    Date curDate = new Date();
                    long timeDiff = curDate.getTime() - programStart;
                    System.out.println("prev semaphore = " + semaphore + ", new semaphore = " + semaphore_same + ", time = " + timeDiff + " : Train " + id + " did not change the track and departed from station " + stationNum);
                    release(semaphore);
                    semaphore = semaphore_same;
                }
                else if (this.semaphores[semaphore_diff].tryAcquire()){
                    // track changed
                    int previousTrack = this.trackNum;
                    this.trackNum = 1 - this.trackNum;
                    Date curDate = new Date();
                    long timeDiff = curDate.getTime() - programStart;
                    System.out.println("prev semaphore = " + semaphore + ", new semaphore = " + semaphore_diff + ", time = " + timeDiff + " : Train " + id + " departed from station " + stationNum + " and changed the track from " + previousTrack + " to " + this.trackNum);
                    release(semaphore);
                    semaphore = semaphore_diff;
                }
                else{
                    Date curDate = new Date();
                    long timeDiff = curDate.getTime() - programStart;
                    System.out.println("prev semaphore = " + semaphore + ", new semaphore = " + semaphore + ", time = " + timeDiff + " : Train " + id + " is still halting on the station " + stationNum + " since the tracks ahead are busy.");
                }
            }
            else{
                int comingStationNum = semaphore_same / 4;
                if (this.semaphores[semaphore_same].tryAcquire()){
                    Date curDate = new Date();
                    long timeDiff = curDate.getTime() - programStart;
                    System.out.println("prev semaphore = " + semaphore + ", new semaphore = " + semaphore_same + ", time = " + timeDiff + " : Train " + id + " did not change the track and arrived at station " + comingStationNum);
                    release(semaphore);
                    semaphore = semaphore_same;
                }
                else{
                    Date curDate = new Date();
                    long timeDiff = curDate.getTime() - programStart;
                    System.out.println("prev semaphore = " + semaphore + ", new semaphore = " + semaphore + ", time = " + timeDiff + " : Train " + id + " is still halting on the track " + trackNum + " since the tracks on station " + comingStationNum + " are busy.");
                }
            }
            nap(NAP_TIME);
        }
    }
}
