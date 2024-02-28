javac WebCrawl.java
java WebCrawl http://www.uwb.edu 20
java WebCrawl http://wikipedia.org 100
java WebCrawl http://courses.washington.edu/css502/dimpsey 100
echo " "
echo "FAILURE CASES"
java WebCrawl wrong_num_args
java WebCrawl http://NO
