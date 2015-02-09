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


class apples{

    static String directory = "html_document";
    static String seedFile = null;
    static long numPages = 0;
    static long hopsAway = 0;

	static Map<String, Integer> map = new HashMap<String,Integer>();
    public static Queue<Tuple<String, Integer>> frontier = new LinkedList<Tuple<String, Integer>>();

	private static final Lock lock = new ReentrantLock();
	public static boolean frontierLock = false;

	public static void main(String[] args){

        //Check if Input is valid
        if( args.length != 3)
        {
                System.out.println("ERROR: Invalid Argument. To be continueed...");///////////////////////////////////////////////////////
                System.exit(0);
        }

        //Get Inputs
        seedFile = args[0];
        numPages = Long.valueOf(args[1]).longValue();
        hopsAway = Long.valueOf(args[2]).longValue();

        //Make sure the directory "html_downloads" exists
        createDirectory();

        //Get Seeds and put them in the frontier
		//System.out.println(useFrontier(2,null) != null);
        getSeeds();
		//Tuple<String, Integer> tmp = useFrontier(2,null);
		//System.out.println(tmp.x + " " + tmp.y + "QQQQQQQQQQQQQQQQQQQQQQQQ");
		System.out.println("GET SEED");
		printFrontier();

		//testThread();
        //Traverse webpages and saves them
        //crawl();
		testThread();
		//testThread();
	
			
	   //printFrontier();	
		
	}

    //Make sure a directory to put the downloaded files exists
    public static void createDirectory() {

        File file = new File(directory);
        if(!file.exists()) {
            System.out.println("Directory to store downloads does not exist.");
            System.out.println("Creating directory \"html_downloads\".");
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
                    //////////////CHECK FOR WHITESPACES AT THE END OF FILE
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
		if(link.charAt(0) == '#' || link.equals("/") || fileType.equals(".jpg") 
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
		if(link.charAt(0) == '/') {
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
            Document doc = Jsoup.connect(url).get(); //This is the same as the one in download. Find a way to get rid of this.You can do it from the downloaded files.
            Elements links = doc.select("a[href]");
            for(Element link : links) {
				String tmp = link.attr("href");
				//Clean and Normalize URL
				String normalizedURL = normalizeURL(tmp, url);
				if(!isDiscarded(tmp) && !map.containsKey(normalizedURL) && hasValidProtocol(normalizedURL)) {
 					//frontier.add(new Tuple<String, Integer>(normalizedURL, currentHopLevel+1));
					useFrontier(1, new Tuple<String, Integer>(normalizedURL, currentHopLevel+1));
				}
            }
			System.out.println("GET LINKS");
			printFrontier();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //Download Webpage. This code is an altered version of the code the TA gave provided in his slides.//////////////////FIX
    public static void downloadFile(String url, long fileNum) throws IOException, 
        MalformedURLException {
		
		try {
			Connection connection = Jsoup.connect(url);
			Document doc = connection.get();
			String htmlContent = doc.html();
			String fileName = "file" + fileNum + ".dld"; // url names?////////////////////////////////////////////////////////////////////////////
			BufferedWriter writer = new BufferedWriter(new FileWriter(directory + "\\" + fileName));
			writer.write(htmlContent);
			writer.close();

			//System.out.println(htmlContent);
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
				frontierLock = false;//Done with frontier. Allow other threads to use frontier.
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
				frontierLock = true;//Don't allow other threads to change frontier until current thread pops
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
		//Runnable thread2 = new MyThread();
		//new Thread(thread).start();

	}

}

