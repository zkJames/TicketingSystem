package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class TicketingDS implements TicketingSystem {
    AtomicLong ticketID;
    ConcurrentHashMap<Long, Ticket> mapTicket;
    //记录某类余票剩余数量的哈希表，每次买票退票进行维护，用来快速查询
    ConcurrentHashMap<Integer, LongAdder> remainTicketMap;
    Route[] routes;
    int routenum;
    int coachnum;
    int seatnum;
    int stationnum;
    int threadnum;
    public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
        //0不使用，从1开始
        mapTicket = new ConcurrentHashMap<>();
        ticketID = new AtomicLong(1);
        routes = new Route[routenum + 1];
        for(int i = 1; i <= routenum; i++){
            routes[i] = new Route(i, coachnum, seatnum, stationnum);
        }
        this.routenum = routenum;
        this.coachnum = coachnum;
        this.seatnum = seatnum;
        this.threadnum = threadnum;
        this.stationnum = stationnum;
        //初始化余票为所有 座位数目 * 车箱数
        remainTicketMap = new ConcurrentHashMap<>();
        for(int route = 1; route <= routenum; route++) {
            for (int departure = 1; departure <= stationnum - 1; departure++) {
                for (int arrival = departure + 1; arrival <= stationnum; arrival++) {
                    remainTicketMap.put(routeAndStationToKey(route, departure, arrival), new LongAdder());
                }
            }
        }
    }

    //产生车票
    Ticket issueTicket(String passenger, int route,int coach,int seat,int departure,int arrival){
        Ticket ticket = new Ticket();
        ticket.tid = ticketID.getAndAdd(1);
        ticket.route = route;
        ticket.coach = coach;
        ticket.seat = seat;
        ticket.departure = departure;
        ticket.arrival = arrival;
        ticket.passenger = passenger;
        mapTicket.put(ticket.tid, ticket);
        return ticket;
    }

    //计算某座位购买后， 会引起哪些类型余票数量的减少
    public void decreaseRemainTicket(int route, BinaryChange binaryChange){
        //算法：找出哪些类型的票在此票购买前是可买的，购买后导致此票不能买，对相应类型车票余票数-1
        for(int departure = 1; departure <= stationnum-1; departure++){
            for(int arrival = departure + 1; arrival <= stationnum; arrival++) {
                //若退票前是不可买的，退票后导致此类票可买
                int thisKind = TicketingDS.stationToBinary(departure, arrival);
                if (((thisKind & binaryChange.before) == 0) && ((thisKind & binaryChange.after) != 0)){
                    remainTicketMap.get(routeAndBinaryToKey(route, thisKind)).decrement();//减少余票
                }
            }
        }
    }
    //计算某座位退退票后，是否会引起哪些类型余票数量的增加
    public void increaseRemainTicket(int route, BinaryChange binaryChange){
        //算法：找出哪些类型的票在此票退票前是不可买的，退票后导致此类票可买，对相应类型车票余票数+1
        for(int i = 1; i <= stationnum-1; i++){
            for(int j = i + 1; j <= stationnum; j++) {
                int thisKind = TicketingDS.stationToBinary(i, j);//某一种类型的票
                //若退票前是不可买的(二进制串冲突)，退票后导致此类票可买
                if (((thisKind & binaryChange.before) != 0) && ((thisKind & binaryChange.after) == 0)){
                    remainTicketMap.get(routeAndBinaryToKey(route, thisKind)).increment();//增加余票
                }
            }
        }
    }
//
//    @Override
//    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
//        if(inquiry(route,departure,arrival) == 0){
//            return null;
//        }
//        for(int i = 1; i <= coachnum; i++){
//            for(int j = 1; j <= seatnum; j++){
//                if(routes[route].coachs[i].seats[j].isFree(departure,arrival)){
//                    BinaryChange outcome = routes[route].coachs[i].seats[j].sell(departure,arrival);
//                    if(outcome != null){
//                        decreaseRemainTicket(route, outcome);//更新余票哈希表
//                        return issueTicket(passenger, route, i, j, departure, arrival);
//                    }
//                }
//            }
//        }
//        return null;
//    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if(inquiry(route,departure,arrival) == 0){
            return null;
        }
        int randCoachNo = ThreadLocalRandom.current().nextInt(1,coachnum);//随机产生coach查找起始点
        int randSeatNo = ThreadLocalRandom.current().nextInt(1, seatnum);//随机产生coach查找起始点
        int i = randCoachNo;
        do{
            i = i % coachnum + 1;
            int j = randSeatNo;
            do {
                j = j % seatnum + 1;
                if (routes[route].coachs[i].seats[j].isFree(departure, arrival)) {
                    BinaryChange outcome = routes[route].coachs[i].seats[j].sell(departure, arrival);
                    if (outcome != null) {
                        decreaseRemainTicket(route, outcome);//更新余票哈希表
                        return issueTicket(passenger, route, i, j, departure, arrival);
                    }
                }
            }while (j != randSeatNo);
        }while (i != randCoachNo);
        return null;
    }
    @Override
    public int inquiry(int route, int departure, int arrival) {
        //seatnum*coachnum是初始座位数量 + 表中记录的变化量 即为余票
        return seatnum*coachnum + remainTicketMap.get(routeAndStationToKey(route, departure, arrival)).intValue();
    }
//    @Override
//    public int inquiry(int route, int departure, int arrival) {
//        int ticketCount = 0;
//        for(int i = 1; i <= coachnum; i++) {
//            for (int j = 1; j <= seatnum; j++) {
//                    if(routes[route].coachs[i].seats[j].isFree(departure, arrival)){
//                        ticketCount++;
//                    }
//            }
//        }
//        return ticketCount;
//    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if(!checkTicket(ticket))//找不到票
            return false;
        mapTicket.remove(ticket.tid);
        BinaryChange outcome = routes[ticket.route].coachs[ticket.coach].seats[ticket.seat].refund(ticket.departure,ticket.arrival);
        increaseRemainTicket(ticket.route, outcome);//刷新余票
        return true;
    }


    @Override
    public boolean buyTicketReplay(Ticket ticket) {
        return false;
    }

    @Override
    public boolean refundTicketReplay(Ticket ticket) {
        return false;
    }

    public boolean checkTicket(Ticket ticket){
        Ticket ticket1 = mapTicket.get(ticket.tid);
        if(ticket1 == null)
            return false;
        return ticket.departure == ticket1.departure &&
                ticket.arrival == ticket1.arrival &&
                ticket.seat == ticket1.seat &&
                ticket.route == ticket1.route;

    }
    //将起点终点站转化为二进制串的静态方法
    //起点2--终点5	1110表示  此座位1-2无人 2-3有人  3-4无人  4-5无人
    public static int stationToBinary(int departure, int arrival){
        return (((1<<(arrival-1)) ) - (1<<(departure-1)));
    }

    //将车次、起点、终点转化为二进制串，用作hashmap的key
    public static int routeAndStationToKey(int route,int departure, int arrival){
        int s = (((1<<(arrival-1)) ) - (1<<(departure-1)));
        return (s << 3) + route;//后三位存放route号
    }

    //将车次、二进制串转化为包含route的二进制串，用作hashmap的key
    public static int routeAndBinaryToKey(int route,int binary){
        return (binary << 3) + route;
    }


}

