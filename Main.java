/* Возможна проблема взаимной блокировки (deadlock). Например, когда все одновременно вставят вилки в правые розетки. 
Другая проблема - ресурсное голодание. Роботу может постоянно не везти, и он не сможет "зарядиться".

Решение предполагает наличие трех состояний. В случае, если розетки заняты, робот впадает в ожидание.

*/

public class Main { 
final int N = 5;
public Main() {
    robots = new Robot[N];
    threads = new Thread[N];
    for(int i=0; i<N; ++i) {
        robots[i] = new Robot(i);
    }

    for(int i=0; i<N; ++i) {
        threads[i] = new Thread(robots[i]);
        threads[i].start();
    }
}

public static void main(String[] args) {
    new Main();
}
Robot robots[];
Thread threads[];
void takeSockets(Robot r)
{
    int i = r.getId();
    synchronized (this) {
        r.setState(State.WAIT);
        unsafeCheck(r);
    }
    synchronized (r) {
        while(r.getState() ==  State.WAIT ) { //spurious wakeup
            try {
                r.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

void freeSockets(Robot r) {
    int i = r.getId();
    synchronized (this) {
        r.setState(State.ACTIVITY);
        unsafeCheck(getLeftNeighbor(r));
        unsafeCheck(getRightNeighbor(r));
    }
}
Robot getLeftNeighbor(Robot r) {
    int id=r.getId();
    return robots[ (id + N  - 1) %N];
}
Robot getRightNeighbor(Robot r) {
    int id=r.getId();
    return robots[ (id + 1) %N];
}
private void unsafeCheck(Robot r) {
    if (r.getState() == State.WAIT && getLeftNeighbor(r).getState() != State.CHARGE  && getRightNeighbor(r).getState() != State.CHARGE) {
        r.setState( State.CHARGE);
        synchronized (r) {
            r.notifyAll();
        }

    }
}

enum State {CHARGE, WAIT, ACTIVITY}
class Robot implements Runnable {
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State state;
    public int getId() {
        return id;
    }

    int id;

    public Robot(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                activity();
                takeSockets(this);
                charge();
                freeSockets(this);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void charge() throws InterruptedException {
        System.out.println("Charge, id:"+id);
        Thread.sleep(1000);
    }

    private void activity() throws InterruptedException {
        System.out.println("Activity, id:"+id);
        Thread.sleep(1000);
    }
}
