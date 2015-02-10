import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.locks.*;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Connection;


class crawler{

    static String directory = null;
    static String seedFile = null;
    static long numPages = 0;
    static long hopsAway = 0;

	static Map<String, Integer> map = new HashMap<String,Integer>();
	public static Queue<Tuple<String, Integer>> frontier = new LinkedList<Tuple<String, Integer>>();

	private static final Lock lock = new ReentrantLock();
	public static boolean frontierLock = false;

	public static void main(String[] args){

        //Check if Input is valid
        if( args.length != 4)
        {
                System.out.println("ERROR: Invalid Argument.");
                System.exit(0);
        }

        //Get Inputs
        seedFile = args[0];
        numPages = Long.valueOf(args[1]).longValue();
        hopsAway = Long.valueOf(args[2]).longValue();
        directory = args[3];
        //System.out.println(directory);

        //Make sure the directory "html_downloads" exists
        createDirectory();
        try {
            downloadFile("http://www.cs.ucr.edu/~cto002/",0);
         } catch (IOException e) {
         }
        

        //Get Seeds and put them in the frontier
        getSeeds();
		System.out.println("GET SEED");
		printFrontier();

        //Traverse webpages and saves them
	    testThread();
		
	}

    //Make sure a directory to put the downloaded files exists
    public static void createDirectory() {

        File file = new File(directory);
        if(!file.exists()) {
            System.out.println("Directory to store downloads does not exist.");
            System.out.println("Creating directory \"" +directory+ "\".");
            if(file.mkdir()) {
                System.out.println("Directory created.");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
     }

    //Puts the seed URLs into the frontier (and ArrayList of URL)
    public static void getSeeds() {

        try (BufferedReader seeds = new BufferedReader(new FileReader(seedFile))) 
		{
            for(String url = seeds.readLine(); url != null; url = seeds.readLine())
            {
				if(hasValidProtocol(url))
				{
					map.put(url,0);
					useFrontier(1,new Tuple<String, Integer>(url,0));
               		//frontier.add(new Tuple<String, Integer>(url,0));
					//testThread();
				}
            }

            seeds.close();
        } 
		catch (IOException e) {
                e.printStackTrace();
        }
    }


    //Clean URL and Normalize URL
    public static boolean isDiscarded(String link) {
		
		String fileType;
	   	if(link.length() < 4)
		{
			fileType = new String("Not an image or pdf");
		}
		else
		{
			fileType= link.substring(link.length()-4);
		}
		if(!link.isEmpty() && link.charAt(0) == '#' || link.equals("/") || fileType.equals(".jpg") 
				|| fileType.equals(".png") || fileType.equals(".gif") 
				|| fileType.equals(".pdf") || link.contains("&") /// Do we avaoid space and ampersands?
				|| link.contains(" ")) {
			return true;
		}
		else
		{
			return false;
		}


    }

	//Gets the current path given a URL
	public static String getPath(String url){
		int cnt = 1;
		int max = url.length();
		char tmp = url.charAt(max-cnt);
		
		//find the position of the last '/'
		while(tmp != '/' && cnt <= max) {
			cnt++;
			tmp = url.charAt(max-cnt);
		}
		return url.substring(0,max-cnt+1); //What if error?

	}
	
	//Checks whether a url has a valid protocol
	public static boolean hasValidProtocol(String url) {
		if(url.startsWith("http://"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	//Normalizes the link
	public static String normalizeURL(String link, String currentPath) { 
		if(!link.isEmpty() && link.charAt(0) == '/') {
			try {
				URL url = new URL(currentPath); //Havet to remove the http
				return url.getProtocol() + "://" + url.getHost() + link;

			} catch (IOException e) {
				e.printStackTrace();

			}
		}
		else if(link.contains("://")) {
			//Already normalized if it has a protocol
			return link;
		}
		else{
			return getPath(currentPath) + link;
		}
	
		return "error";
	}

    //Check if a URL has be crawled already
    
    //Check robots.txt
    
    //Get links
    public static void getLinks(String url, int currentHopLevel) {
        try {
            Document doc = Jsoup.connect(url).get(); 
            Elements links = doc.select("a[href]");
            for(Element link : links) {
				String tmp = link.attr("href");
				//Clean and Normalize URL
				String normalizedURL = normalizeURL(tmp, url);
				if(RobotExclusionUtil.robotsShouldFollow(normalizedURL) && !isDiscarded(tmp) && !map.containsKey(normalizedURL) && hasValidProtocol(normalizedURL)) {
					useFrontier(1, new Tuple<String, Integer>(normalizedURL, currentHopLevel+1));
				}
            }
			System.out.println("GET LINKS");
			printFrontier();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //Download Webpage. This code is an altered version of the code the TA gave provided in his slides.
    public static void downloadFile(String url, long fileNum) throws IOException, 
        MalformedURLException {
		
		try {
			Connection connection = Jsoup.connect(url);
            connection.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101 Firefox/14.0.1");
            connection.setConnectTimeout(5000);
			Document doc = connection.get();
			String htmlContent = doc.html();
			String fileName = "file" + fileNum + ".dld"; 
			BufferedWriter writer = new BufferedWriter(new FileWriter(directory + "\\" + fileName));
			writer.write(htmlContent);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	//A function to handle all access to the frontier. This is to prevent multiple threads from accessing the frontier.
	public static synchronized Tuple<String,Integer> useFrontier(int action, Tuple<String, Integer> link) {

		Tuple<String,Integer> tmp = null;

		switch(action) {
			case 0 : //Pop()
			{	
				if(frontier.peek() != null)
				{	
					tmp = frontier.remove();
				}
				else
				{
					tmp = new Tuple<String, Integer>("EMPTY",-1);
				}
				break;
			}
			case 1 : //Push()
			{
				frontier.add(link);
				break;
			}
			case 2: //Peek()
			{
				tmp = frontier.peek();
			}
			default: 
			{
				break;
			}
		}

		return tmp;
	}

	//A function to handle all access to the Map. This is to prevent multiple threads from accessing the map.
	public static synchronized boolean useMap(int action, String link, int hopLevel) {

		boolean doesContainLink = false;

		switch(action) {
			case 0: //contains()
			{
				doesContainLink = map.containsKey(link);
				break;
			}
			case 1:
			{
				map.put(link,hopLevel);
				break;
			}
			default:
			{
				break;
			}
		}

		return doesContainLink;
	}

	//Function for testing
	public static void printFrontier() {
			System.out.println("\n________________________________________________________\n");
			for(Tuple<String, Integer> s : frontier)
			{
				System.out.println(s.x + " " + s.y);
			}
			System.out.println("\n---------------------------------------------------------\n");
	}

	public static void testThread() {
		Runnable thread = new MyThread("HAI");
		Runnable thread2 = new MyThread("DDD");
		Runnable thread3 = new MyThread("FFF");
		new Thread(thread).start();
		new Thread(thread2).start();
		new Thread(thread3).start();
	}

}

