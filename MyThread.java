import java.util.concurrent.locks.*;
import java.util.Queue;
import java.util.LinkedList;
import java.io.*;

public class MyThread implements Runnable {
	
	public static long fileNum;
 	String name;

	public MyThread(String n) {
		name = n;
		fileNum = 0;
	}
	public void run() {

	    while(apples.useFrontier(2,null) != null) {
			System.out.println("I AM THREAD " + name); //CHange happening between these two?//ERRRRRROROORORR// I can't make these two atomic
            Tuple<String, Integer> temp = apples.useFrontier(0,null);//Pop
			if(temp.y < 0) {//THIS METHOD IS CHEAP/////////////////////////////////////////////////////////////////////////
					break; //Frontier became empty
			}
            try {
                apples.downloadFile(temp.x, usefileNum(0));
				if (temp.y < apples.hopsAway)
				{
					apples.getLinks(temp.x,temp.y);
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
