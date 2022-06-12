package ticketingsystem;

public class Route {
    int routeNo;//此路线的路线号
    Coach[] coachs;
    int coachnum;
    int seatnum;
    int stationnum;
    //用来快速查询余票的哈希表，购票退票的时候维护此表。<上下站点对应的二进制串,余票>

    Route(int routeNo, int coachnum, int seatnum, int stationnum)
    {
        coachs = new Coach[coachnum + 1];
        for(int i = 1; i <= coachnum; i++) {
            coachs[i] = new Coach(routeNo, seatnum, stationnum);
        }
        this.seatnum = seatnum;
        this.routeNo = routeNo;
        this.coachnum = coachnum;
        this.stationnum = stationnum;

    }


}
