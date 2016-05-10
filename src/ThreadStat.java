
public class ThreadStat {
	
	private long thread_num;
	private int stat;
	private long long_stat;
	
	public ThreadStat(){
		thread_num = 0;
		stat = 0;
		long_stat = 0;
	}

	public long getThread_num() {
		return thread_num;
	}

	public void setThread_num(long thread) {
		this.thread_num = thread;
	}

	public int getStat() {
		return stat;
	}

	public void setStat(int stat) {
		this.stat = stat;
	}
	
	public Long getLongStat() {
                return long_stat;
        }

        public void setLongStat(long stat) {
                this.long_stat = stat;
        }

}
