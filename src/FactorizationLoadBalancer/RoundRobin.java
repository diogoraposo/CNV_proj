package FactorizationLoadBalancer;

import java.util.List;

public class RoundRobin {
    
    int [] temp;
    int [] tempWaitinTime;
    int commBT, k, tq;
    int[][] d;
    int btcache;
    
    void start( ){
        for (int i = 0; i < d.length; i++) {
            int bt  = d[i][1];
            if( bt > 0){
                if( bt <= tq){
                    temp[i] = btcache+bt;
                    btcache = temp[i];
                    k += bt;
                    bt -= bt;
                    
                }
                else{
                    temp[i] = btcache+tq;
                    btcache = temp[i];
                    bt -= tq;
                    k += tq;
                }
                
                d[i][1] = bt;
                
            }
        }
        if( k!= commBT)
        start();
    }
    

    public void run(List<Job> jobList, int quantum) {
        
        int pcount = jobList.size();
        d = new int[pcount][2];
        
        temp = new int[pcount];
        int count = 0;
        for(Job job:jobList){
            d[count][0] = count;
            
            int m = job.getCpuTime();
            d[count][1] = m;
            
            commBT += m;
            count++;
        }
        
        tq = quantum;
        start();
        
    }
}
