This is a simple REST endpoint-based URL summariser service. 

The main class can be run by running `mvn spring-boot:run`, and the service can be accessed by sending an HTTP get to `localhost:8080/summariser`, passing in an optional url parameter (otherwise it defaults to https://bbc.co.uk/news)

Libraries:

Spring Boot was chosen for its annotation-driven approach, plus it seems to be the de-facto choice for REST controller code.

JSoup for easy HTML parsing and CSS selection of metadata tags

Gson for mapping to JSON.

What I'd do given more time:

Add unit tests

Add returning of sensible status codes in JSON format (404s etc) if URL is malformed/not available. 


 
