import java.util.concurrent.locks.*;
import java.util.Queue;
import java.util.LinkedList;
import java.io.*;

public class MyThread implements Runnable {
	
	public static long fileNum;

	public MyThread() {
		fileNum = 0;
	}
	public void run() {

	    while(crawler.useFrontier(2,null) != null && usefileNum(0) <= crawler.numPages) {
            Tuple<String, Integer> temp = crawler.useFrontier(0,null);//Pop
			if(temp.y < 0) {
					break; //Frontier became empty
			}
            try {
                crawler.downloadFile(temp.x, usefileNum(0));
				if (temp.y < crawler.hopsAway)
				{
					crawler.getLinks(temp.x,temp.y);
				}
            } catch (IOException e) {
                e.printStackTrace();
            }
            usefileNum(1);
        	
		}
	}

	public static synchronized long usefileNum(int action) {
		long fileNum2 = 0;
		switch(action)
		{
			case 0:
			{
				fileNum2 = fileNum;
				break;
			}
			case 1:
			{
				fileNum++;
				break;
			}
			default:
			{
				break;
			}
		}
		return fileNum2;
	}

	//handlefilenum
	
}
