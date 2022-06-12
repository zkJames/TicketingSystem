## 题目

使用Java语言设计并完成一个用于列车售票的可线性化并发数据结构：

TicketingDS类，该类实现TicketingSystem接口，同时提供 TicketingDS ( routenum , coachnum , seatnum , stationnum , threadnum ) ; 构造函数。其中，routenum是车次总数（缺省为5个），coachnum是列车 的车厢数目（缺省为8个），seatnum是每节车厢的座位数（缺省为100个）， stationnum 是每个车次经停站的数量（缺省为10个，含始发站和终点站）， threadnum 是并发购票的线程数（缺省为16个）。 

为简单起见，假设每个车次的coachnum、seatnum和stationnum都相 同。车票涉及的各项参数均从1开始计数，例如车厢从1到8编号，车站从1到10编 号等。 每位学生需编写多线程测试程序，在main方法中用下述语句创建TicketingDS类 的一个实例。

```java
 final TicketingDS tds = new TicketingDS ( routenum , coachnum , seatnum , stationnum , threadnum ) ; 
```

​	系统中同时存在threadnum个线程（缺省为16个），每个线程是一个票务代理，按照60%查询余票，30%购票和10% 退票的比率反复调用TicketingDS类 的三种方法若干次（缺省为总共10000次）。按照线程数为4，8，16，32， 64个的情况分别给出每种方法调用的平均执行时间，同时计算系统的总吞 吐率（单位时间内完成的方法调用总数）。 正确性要求 - 每张车票都有一个唯一的编号tid，不能重复。 - 每一个tid的车票只能出售一次。退票后，原车票的tid作废。 

## 正确性要求

- 每个区段有余票时，系统必须满足该区段的购票请求。 
- 车票不能超卖，系统不能卖无座车票。
- 买票、退票方法需满足可线性化要求。
- 查询余票方法的约束放松到静态一致性。查询结果允许不精确，但是在某个车次查票过程中没有其 他购票和退票的“ 静止”状态下，该车次查询余票的结果必须准确。
