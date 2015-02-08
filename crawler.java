import java.net.*;
import java.io.*;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


class apples{

    static String directory = "html_document";
    static String seedFile = null;
    static long numPages = 0;
    static long hopsAway = 0;

	static Map<String, Integer> map = new HashMap<String, Integer> (500000);
	public static void main(String[] args){

        //Check if Input is valid
        if( args.length != 3)
        {
                System.out.println("ERROR: Invalid Argument. To be continueed...");
                System.exit(0);
        }

        //Get Inputs
        seedFile = args[0];
        numPages = Long.valueOf(args[1]).longValue();
        hopsAway = Long.valueOf(args[2]).longValue();

        //Make sure the directory "html_downloads" exists
        createDirectory();

        //Get Seeds and put them in the frontier
        getSeeds();

        //Traverse webpages and saves them
        crawl();
       
        cleanURL("www.cs.ucr.edu/~ltadi001/");

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

        try (BufferedReader seeds = new BufferedReader(new FileReader(seedFile))) {
            for(String url = seeds.readLine(); url != null; url = seeds.readLine())
            {
		map.put(url,0);
            }
/*          	
		for(Map.Entry<String, Integer> entry : map.entrySet())
		{
			String getUrl = entry.getKey();
			System.out.print(getUrl + "\n");
		}*/ 
            seeds.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
    }


    //Clean URL and Normalize URL
    public static void cleanURL(String url) {
        try {
        URL urlObj = new URL(url); //Havet to remove the http
        System.out.println("HHHHHHHHH\n");
        System.out.println(urlObj.getHost());
        System.out.println(urlObj.getProtocol());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //Check if a URL has be crawled already
    
    //Check robots.txt
    
    //Get links
    public static void getLinks(String url) {
        try {
            Document doc = Jsoup.connect(url).get(); //This is the same as the one in download. Find a way to get rid of this.You can do it from the downloaded files.
            Elements links = doc.select("a[href]");
            for(Element link : links) {
                System.out.println("\nlink : " + link.attr("href"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //Traverse webpages starting from the seed URLs.
    public static void crawl() {
        long fileNum = 0; ///What happens to files that already exists, do they just get replaced?
            try {
		for(Map.Entry<String, Integer> entry : map.entrySet())
		{
                	downloadFile(entry.getKey(), fileNum);
                	System.out.println(entry.getKey());
			getLinks( "http://" + entry.getKey());
		}
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileNum++;
        }
    

    //Download Webpage. This code is an altered version of the code the TA gave provided in his slides.//////////////////FIX
    public static String downloadFile(String url, long fileNum) throws IOException, 
        MalformedURLException {

        URL urlObj = new URL(/*"http://" +*/ url); //Havet to remove the http
        String fileName = "file" + fileNum + ".dld";
        BufferedWriter writer = new BufferedWriter(new FileWriter(directory + 
                                "\\" + fileName));
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                urlObj.openConnection().getInputStream()));
        for(String line = reader.readLine(); line != null; line = 
            reader.readLine()) {

            //System.out.println(line);
            writer.write(line + "\n");
        }
        
        reader.close();
        writer.close();

        return fileName;
    }

}
