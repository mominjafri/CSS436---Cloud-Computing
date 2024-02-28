/* Momin Jafri
CSS436
Robert Dimpsey
Program 1 - Basic Web Crawler

*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class WebCrawl {

  private Set<String> visitedUrls = new HashSet<>(); // Visited URL set to prevent same link from being crawled twice

  public static void main(String[] args) {
    
    //argument checks
    if (args.length < 2) {
      System.out.println("Usage: java WebCrawler <url> <maxHops>");
      
    }

    String stringURL = args[0];

    if (!isUrlValid(stringURL)) {
            System.out.println("Please enter an HTTP URL");
            return;
    }
    
    int maxHops;
    if(args[1].matches("[0-9]+")) {
            maxHops = Integer.parseInt(args[1]);
        } else {
            System.out.println("Please enter a number >=0 for the hop limit");
            return;
        }

    WebCrawl crawler = new WebCrawl(); //initialize crawler
    System.out.println("Crawling " + stringURL + " with max hops " + maxHops); // Print starting URL
    crawler.crawl(stringURL, maxHops, 1); // Crawl starting URL
    System.out.println("Crawling complete. Max hop limit reached"); //end of crawl

  }

  public void crawl(String url, int maxHops, int currentHop) {
    
    while(currentHop <= maxHops) { // Crawl until max hops reached
        url = normalizeUrl(url); // Normalize URL

        if (visitedUrls.contains(url)) { // URL already visited
            return;
        }

        visitedUrls.add(url); // Add URL to visited URLs
        System.out.println("Hop " + currentHop + ": " + url); // Print URL

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection(); // Open connection to URL
            conn.setRequestMethod("GET"); // Set request method to GET

            int responseCode = conn.getResponseCode(); // Get response code

            if (responseCode >= 300 && responseCode < 400) { // Redirect
                // handle redirect
                String newUrl = conn.getHeaderField("Location"); // get redirect url
                url = normalizeUrl(newUrl); // normalize redirect url
                currentHop++; // increment hop count
                return;
            } else if (responseCode >= 400) { // Error
                // handle error
                System.out.println("Error crawling: " + url);
            } else {
                // extract links and crawl
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())); // Get input stream
                String line;
                boolean isLooping = true; // loop until no more links found
                while (isLooping && (line = reader.readLine()) != null) { // while there are links to crawl
                    // extract links from line
                    String[] links = line.split("<a href=\""); // split line into links
                    for (String link : links) {
                        if (link.startsWith("http")) { 
                            link = link.substring(0, link.indexOf("\"")); // get link from split
                            url = normalizeUrl(link); // normalize link
                            if(visitedUrls.contains(url)) { // link already visited
                                continue;
                            } 
                            currentHop++; // increment hop count
                            isLooping = false; // stop loop
                            break;
                        }
                    }

                }
            }

            } catch (Exception e) { // handle errors 
                System.out.println("Error crawling: " + url);
            }
        }
    }


    private String normalizeUrl(String url) { // URL normalization method
        if (url.endsWith("/")) { // remove trailing slash if present
            return url.substring(0, url.length()-1); // return url without trailing slash
        } else {
            return url; // return url without change if no change is needed
        }
    }

   private static boolean isUrlValid(String url) { // URL validation method
        return url.startsWith("http://") || url.startsWith("https://");
    }


}


