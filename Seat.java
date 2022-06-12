package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Seat {
    int stationNum;//站台数目
    int routeNo;//此座位对应的车次
    //站台2--5	1110表示  1-2有 2-3无  3-4无  4-5无
    AtomicInteger section;//用来表示某站点到某站点的二进制数
    int seatNo;
    Seat(int routeNo, int seatNo,int stationNum){
        this.routeNo = routeNo;
        this.seatNo = seatNo;
        this.stationNum = stationNum;
        section = new AtomicInteger(0);//默认各条路线的票都有000000
    }

    public boolean isFree(int departure,int arrival) {
        return (section.get() & (((1<<(arrival-1)) ) - (1<<(departure-1)))) == 0;
    }

//    synchronized public boolean sell(int departure,int arrival) {
//        int oldSec = section.get();
//        //判断如果被占用了，则退出循环
//        section.set(oldSec | (((1<<(arrival-1)) ) - (1<<(departure-1)))) ;
//        return true;
//    }
//
//    synchronized public boolean refund(int departure, int arrival) {
//        int oldSec = section.get();
//        int newSec = oldSec & (~(((1<<(arrival-1)) ) - (1<<(departure-1))));
//        section.set(newSec);
//        return true;
//    }

    public BinaryChange sell(int departure, int arrival) {
        while (true){
            int oldSec = section.get();
            //判断如果被占用了，则退出循环
            if((oldSec & (((1<<(arrival-1)) ) - (1<<(departure-1)))) != 0){
                return null;
            }
            //cas操作成功则返回成功
            int update = oldSec | (((1 << (arrival - 1))) - (1 << (departure - 1)));
            if(section.compareAndSet(oldSec, update)) {
                return new BinaryChange(oldSec, update);
            }
        }
    }

    public BinaryChange refund(int departure, int arrival) {
        while (true){
            int oldSec = section.get();
            int newSec = oldSec & (~(((1<<(arrival-1)) ) - (1<<(departure-1))));
            //cas操作成功则返回成功
            if(section.compareAndSet(oldSec, newSec)) {
                return new BinaryChange(oldSec, newSec);
            }
        }
    }
}
