javac -cp "gson-2.10.1.jar": Weather.java

# To run program run the following command:
# java -cp gson-2.10.1.jar: Weather <city name>

# WARNING ** please read: You may get a "Error: Server returned HTTP response code: 400 for URL: https://api.openweathermap.org/data/2.5/weather?q=Los Angeles&appid=6c8762751063cc8040e09c8f9408ed6e
           #     Exception in thread "main" java.lang.NullPointerException: Cannot load from double array because the return value of "Weather.getLatLon(String, String)" is null"

        # If you get this error, just run the program again. It will work.


# PLEASE READ: In my getCityActivities method, the API key I am using had to constantly be updated when testing even though I made an account. If this problem occurs please let me know before you put out final grades.