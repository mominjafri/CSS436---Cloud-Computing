/* Momin Jafri
CSS436 - Robert Dimpsey
REST Weather program


You must consume multiple RESTful APIs to get this information.
• The program must be a command line program. It should not have a GUI interface.
• This should be a java class which can run in the Linux Lab
• I am intentionally not specifying what information is displayed back to the user.
However, a useful app is required and will be graded accordingly.
• The OpenWeatherMap is a great free website which allows one to get this information.
I would recommend using this. http://openweathermap.org/api
o You’ll need to signup to get appid for authorization
• The other fact about the city must come from a different web service and it should also
be RESTful
• Your program should have proper re-try logic using appropriate exponential back-off.
• Have Fun!


Momin Jafri
Robert Dimpsey
CSS436
This program uses 4 API calls
One for the weather, one for the population, one for the state and country and one for city hot spots


*/
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.Iterator;
import java.util.List;
import com.google.gson.*;
import java.util.Random;




public class Weather{

    private static final int MAX_RETRIES = 3;
    
    public static void main(String[] args) {
        if (args.length == 0) { // if no arguments are passed in
            System.out.println("Please enter a city name");
            return;
        }

        Scanner scanner = new Scanner(System.in); // create a scanner object
        Random rand = new Random();

        do {
            String city = args[0]; // get the city name from the command line arguments
            if (args.length > 1) {
                city = city + " " + args[1]; // combine city names if more than one argument is passed in, for example Los Angeles becomes LosAngeles
            }

            int retries = 0;
            int retryDelay = 1000;

            while (retries < MAX_RETRIES) { // retry logic
                if (runApp(city)) { 
                    break; // exit the loop if the app runs successfully
                }

                //exponential backoff
                retries++; // increment the retry count
                int delay = (int) (retryDelay * Math.pow(2, retries)); // calculate the delay
                delay += rand.nextInt(retryDelay); // add some randomness to the delay

                System.out.println("Retrying in " + delay + " milliseconds...");

                try {
                    Thread.sleep(delay); // sleep for the calculated delay
                } catch (InterruptedException e) {
                    e.printStackTrace(); // print the exception if it occurs
                }
            }

            System.out.println("Do you want to enter another city? (yes/no)"); 
            String response = scanner.nextLine();
            if (!response.equalsIgnoreCase("yes")) {
                break; // exit the loop if the user doesn't want to enter another city
            }

            System.out.println("Enter another city: ");
            args[0] = scanner.nextLine(); // get the next city name

        } while (true);

        System.out.println("Exiting program..."); 
    }


    public static boolean runApp(String city){
        String url = getURL(city); //get url for weather

        double lat = getLatLon(url, city)[0]; //get lat and lon
        double lon = getLatLon(url, city)[1];

        String pop = getCityInfo(city); //get population
        String[] country = countryCode(lat, lon); //get country and city name

        System.out.println("\nRetrieving information for " + city + "..." + "\n");;
        System.out.println("The weather in " + city + " is " + getWeather(url, city) + "\u00B0 F" + "\n");
        System.out.println(temperatureCheck(getWeather(url, city), city) + "\n");
        System.out.println(city + ", " + country[0] +  ", " + country[1] + " has a current estimated population of " + pop + "\n");

        System.out.println("Here are the hot spots in " + city + ":\n");
        getCityActivities(lat, lon);

        return true;

    }

    //get url for weather with key
    public static String getURL(String city){ 
        return "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=6c8762751063cc8040e09c8f9408ed6e";
    }

    //small function to return a comment on how hot or cold it is
    public static String temperatureCheck(int temp, String city) {
        int cold = 40; //less than 40
        int mid = 60; //between 40 and 60
        int hot = 80; //between 60 and 80
        int veryHot = 100; //over 80
        if(temp < cold) return "It is a cold day in " + city;
        else if(temp < mid) return "It is a mild day in " + city;
        else if(temp < hot) return "It is a warm day in " + city;
        else if(temp < veryHot) return "It is a hot day in " + city;
        else return "Wow, it is a very hot day in " + city;
    
    }

    //get the actual Weather function
    public static int getWeather(String url, String city) {
        StringBuilder builder = new StringBuilder();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection(); //open connection to url
            connection.setRequestMethod("GET"); //set request method to GET
            connection.connect(); 
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); //read in data from url
            String line; 
            while((line = reader.readLine())!= null) { //read in line by line
                String jsonString = line; 

                JsonElement jsonElement = JsonParser.parseString(jsonString); //parse json string to json element
                
                if (jsonElement.isJsonObject()) { //if json element is a json object, get that object
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    if (jsonObject.has("main") && jsonObject.get("main").isJsonObject()) { //if main object is a json object, get that object
                        JsonObject mainObject = jsonObject.getAsJsonObject("main");

                        if (mainObject.has("temp") && mainObject.get("temp").isJsonPrimitive()) {
                            double temperature = mainObject.getAsJsonPrimitive("temp").getAsDouble(); //get temperature
                            int intTemp = (int) temperature; //convert temperature to int

                            double farenTemp = (intTemp - 273.15) * 1.8 + 32; //convert from Kelvin to Farenheit
                            int farenInt = (int) farenTemp; //make it an int

                            return farenInt;
                        } else {
                            System.out.println("No 'temp' field in 'main' object");
                            return 0;
                        }
                    } else {
                        System.out.println("No 'main' object in JSON"); //error messages
                        return 0;
                    }
                } else {
                    System.out.println("Invalid JSON structure");
                    return 0;
                }
            }
        
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return 0;
    }

    public static String getCityInfo(String city) {

        String space = " "; 
        if(city.contains(space)) { //this API doesnt like spaces, replaces them with "%20"
            city = city.replace(space, "%20");
        }
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://opentripmap-places-v1.p.rapidapi.com/en/places/geoname?name=" + city))
            .header("X-RapidAPI-Key", "d272d4da00msh52b06f9140d0eb8p155a9ejsne88400438662")
            .header("X-RapidAPI-Host", "opentripmap-places-v1.p.rapidapi.com")
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            //System.out.println(response.body());

            String jsonString = response.body();

            JsonElement jsonElement = JsonParser.parseString(jsonString);

            // Check if it's an object
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                int population = jsonObject.get("population").getAsInt();

                return String.valueOf(population); //return the population

            } else {
                System.out.println("Not a valid JSON object");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    //get the state and country
    public static String[] countryCode(double lat, double lon){
        try {
            String apiKey = "prj_live_sk_fb9c4a59c7fd595c685234f10fa789b8441def16";  

            // Create the URL for the Radar API request
            URL url = new URL("https://api.radar.io/v1/geocode/reverse?coordinates=" + lat + "," + lon + "&layers=address");

            // Create an HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", apiKey);

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Read the response
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                String jsonResponse = response.toString();

                String state = "N/A";
                String country = "N/A";

                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                JsonArray addressesArray = jsonObject.getAsJsonArray("addresses"); //get 'addresses' array

                if (addressesArray.size() > 0) { // extract information from the first 'address' object (assuming only one address is returned)
                    JsonObject addressObject = addressesArray.get(0).getAsJsonObject();

                    state = addressObject.get("state").getAsString(); // extract state and country
                    country = addressObject.get("country").getAsString();
                }

                return new String[]{state, country}; //return array of state and country
            } else {
                System.out.println("Error: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //Getting lat and lon from weather API
    public static double[] getLatLon(String url, String city) {

        StringBuilder builder = new StringBuilder();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while((line = reader.readLine())!= null) {
                String jsonData = line;
                JsonElement jsonElement = JsonParser.parseString(jsonData);

                // Extract lat and lon
                double lat = jsonElement.getAsJsonObject().getAsJsonObject("coord").get("lat").getAsDouble();
                double lon = jsonElement.getAsJsonObject().getAsJsonObject("coord").get("lon").getAsDouble();

                return new double[] {lat, lon};
            }
               

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return null;
    }

    public static void getCityActivities(double lat, double lon){
        String url = "https://test.api.amadeus.com/v1/shopping/activities?latitude=" + lat + "&longitude=" + lon + "&radius=10";

        try{
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/vnd.amadeus+json");
            connection.setRequestProperty("Authorization", "Bearer Dvn73bDjNDcQyqr6PUYHfU8NmQjg"); //This API needs to constantly be updated idk why
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while((line = reader.readLine())!= null) {
                String jsonString = line;
                JsonElement jsonElement = JsonParser.parseString(jsonString);

                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    if (jsonObject.has("data") && jsonObject.get("data").isJsonArray()) { // Check if the JSON object has a 'data' field and it's an array
                        JsonArray activitiesArray = jsonObject.getAsJsonArray("data"); //get activites array

                        processActivities(activitiesArray);//seperate function to get activites
                    } else {
                        System.out.println("Invalid JSON structure: Missing or invalid 'data' field.");
                    }
                } else {
                    System.out.println("Not a valid JSON object.");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
    }

    //processing the JSON array of activities
    private static void processActivities(JsonArray activitiesArray) {
        int count = 0;

        for (JsonElement activityElement : activitiesArray) {
            if (activityElement.isJsonObject()) {
                JsonObject activityObject = activityElement.getAsJsonObject();

                // Retrieve necessary information
                String name = getActivityName(activityObject);
                String description = removeHtmlTags(getActivityDescription(activityObject));

                System.out.println("HOTSPOT: " + name); //print hot spots
                System.out.println("Type/Description: " + description + "\n"); //include description

                count++;

                if (count >= 3) { //break the loop as we are only including the top 3 activities
                    break;
                }
            }
        }
    }

    //Retrieving name of activities
    private static String getActivityName(JsonObject activityObject) {
        JsonElement nameElement = activityObject.get("name");
        return (nameElement != null && nameElement.isJsonPrimitive()) ? nameElement.getAsString() : "N/A";
    }

    //Retrieving description of activities
    private static String getActivityDescription(JsonObject activityObject) {
        JsonElement descriptionElement = activityObject.get("shortDescription");
        return (descriptionElement != null && descriptionElement.isJsonPrimitive()) ? descriptionElement.getAsString() : "N/A";
    }

    //removes http codes from json
    private static String removeHtmlTags(String input) { 
        return input.replaceAll("\\<.*?\\>", "");
    }   

}