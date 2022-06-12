package ticketingsystem;
//某座位购买/退票后，二进制串前后的变换，用来计算余票
class BinaryChange {

    int before;
    int after;

    public BinaryChange(int before, int after) {
        this.before = before;
        this.after = after;
    }

    public int getBefore() {
        return before;
    }

    public void setBefore(int before) {
        this.before = before;
    }



    public void setAfter(int after) {
        this.after = after;
    }

    public int getAfter() {
        return after;
    }
}