package ticketingsystem;

public class Coach {
    int seatnum;
    int stationnum;
    int routeNo;//此车厢所在的车次
    Seat[] seats;
    Coach(int routeNo, int seatnum,int stationnum)
    {
        seats = new Seat[seatnum + 1];
        this.routeNo = routeNo;
        for(int seatno = 1; seatno <= seatnum; seatno++){
            seats[seatno] = new Seat(routeNo,seatno,stationnum);
        }
        this.seatnum = seatnum;
        this.stationnum = stationnum;
    }
}
