# Fixing Implicit Flow Fragments disappearing with NodeJS backends

In this post, we will handle a problem that occurs, when trying to utilize the OAuth2.0 implicit grant flow with a NodeJs   
backend. The problem arises when the authorization server redirects the user to the specified redirection_uri with the access token inserted into the URI as a fragment parameter. This behavior is specified in [RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749#section-4.2) and can not be altered by the user. If this redirection_uri points to a NodeJS endpoint, which does not serve a HTML frontend, therefore including the Browser in the process,  
the fragment part of the URI simply gets omitted and never arrives at our NordeJS backend.   
 
Since there is no way of altering the implicit flow itself, we have to find a workaround to be able to receive the access_token in our NodeJS backend. The solution presented here, first points the redirection URI to a NodeJS endpoint that serves a HTML file. The sole purpose of this html page is to replace the fragment in the URI with a question mark and send this edited version to a different NodeJS endpoint which handles further processing of the parameters sent in the URI. 

## Why do we care about implicit flow?
The reason for needing a workaround is that initially, this grant flow was optimized for clients which operated mostly in the browser. It relies on requests being redirected between browser based user agents and serves the access token directly in the request from the authorization server. 
Even though this approach may be outdated and authorization code grants  or client credential grant flow preferable, you might have to integrate it into an application which is already in existence and requires you to use this flow. 


## Our solution
For the purpose of this post, we implemented a simple NodeJS backend and also implemented a very simple authorization server. This server basically takes the request from the client and redirects it to the specified redirect_uri with the access token attached In the URI as a fragment.
We then expose an endpoint on our NodeJS server which serves an HTML file at the specified redirect_uri and starts a script for exchanging the fragment with a question mark. Then a redirection to an endpoint which handles the now valid URI and extracts the access token happens. 

### Start of the flow: The login page
To properly simulate the flow, we do not simply send the requests ourselves but we serve a simple login page which calls our `/auth` endpoint when `Login` is pressed. 

`index.html`
```html
<!DOCTYPE  html>
<html  lang="en">
	<head>
		<meta  charset="UTF-8"  />
		<title>Andamp OAuth2 Authentication Client</title>
	</head>
	<body>
		<div>
			<h1>Login with OAuth2 over andamp</h1>
			<a  href="/auth">Login</button>
		</div>
	</body>
</html>
```
The corresponding server part, redirects the user to the authorization server which we have implemented ourselves. Usually, you would have to first register your client with the authorization server and get a client_id and secret from this process. Since it is not relevant to our problem, we will simply hardcode these values and not care about them on our auth server stub. In our server.js file, we expose the specified `/auth` endpoint through an Express app. We then build the request in a seperate function and redirect the the caller to the auth server with the query parameters in place. For the implicit flow, we have to specify the following query parameters: 

 - `redirect_uri`: The uri to which the auth server will redirect the request with the access token as a fragment
 - `response_type`: We need to set the value of this parameter to "token" to indicate to the authentication server that we want to use the implicit grant flow
 - `state`: This value serves as a measure to prevent CSRF attacks. The auth server will send this value back with the access token and we can compare it to our initial state value to ensure that the response was replying to our request and identify it.
 - `scope`: We can specify which data of the user (Resource Owner) we would like to access through this parameter.
 - `client_id`: This parameter is relevant when working with an actual authentication server. It is received when registering the client with the authentication server.

With these parameters set, we can send our Request to our custom authentication server which is running on localhost. The endpoint in server.js looks as follows:
`server.js`
```javascript 
const  express = require("express");
const  path = require("path");
const  cors = require("cors");
const  router = express();
const  utils = require("./utils");
const  port = 8082;
 
router.use(express.static(__dirname + "/public"));
router.use(
cors({
origin: ["http://localhost:8080", "http://localhost:8082"]}));

router.get("/auth", async (req, res) => {
	try {
		const  state = "8njkfds893ksHSD3bd";
		res.redirect(utils.auth_server_request(state));
	} catch (error) {
		res.sendStatus(500);
		console.error(error.message);
	}
});

router.listen(port, () =>  console.log("Running server on port: " + port));
```

We use a utils.js file to separate the server code from the utility function that builds our request. Notice that we pass a static state to the auth server. In a real application this should not be done and this value should be randomly generated. For simplicity sake, we work with the static version for now. 
 
`utils.js`
```javascript
const  auth_server_endpoint = "http://localhost:8080/";
const  query_params = {
	client_id:  "client_id_implicit_authentication_app",
	redirect_uri:  `http://localhost:8082/login/oauth/callback`,
};

const  auth_server_request = (state) => {
	const  auth_token_params = {
		...query_params,
		response_type:  "token",
		state,
		scope:  "profile",
	};
	return  `${auth_server_endpoint}?${new  URLSearchParams(auth_token_params)}`;
};
module.exports = { auth_server_request };
```
For now, we are simply redirecting the User to the auth server as soon as the Login link is clicked. Now we need to actually implement the solution that solves our initial problem. For this purpose, we first create a new HTML file for receiving the response from the authentication server. Notice that we have defined `login/oauth/callback` as our redirect_uri. This means that the response from the server will be directed at our  `login/oauth/callback` in our backend. This endpoint simply serves a HTML file which handles the actual fix to this problem that we are trying to solve. 
The endpoint which serves the HTML looks like this and is located in our `server.js` file: 
```javascript
router.get("/login/oauth/callback", async (req, res) => {
	res.sendFile(path.join(__dirname, "/public/OAuthRedirect.html"));
});
```
Now to our simple but powerful HTML file which actual solves our fragment dilemma:

`OAuthRedirect.html`
```html
<!DOCTYPE  html>
<html  lang="en">
	<head>
		<meta  charset="UTF-8"  />
		<title>OAuth 2.0 implicit grant flow redirect</title>
	</head>

	<body>
		<script>
			let  callback = window.location.href;
			callback = callback.replace("#", "?");
			callback = callback.replace("oauth/", "");
			window.location.href = callback;
		</script>
	</body>
</html>
```
This simple files does a very important job for us. It takes the fragment from the URI that we received from the authentication server and replaces it with a question mark. Replacing the oauth/ part is not as important, it simply allows as to call our endpoint for processing the received URI. We then simply set the window location to the new URI, triggering our endpoint and receiving the access token in our NodeJS backend. 

Now that we are not dealing with a fragment anymore, we can directly process the request without Browser javascript. Let's look at what we do with this redirect in our back-end in `server.js`
```javascript
router.get("/login/callback", async (req, res) => {
	const  queryString = req.query;
	if (!req.query) {
		return  res.status(404).send("No Access Token provided");
	}
	  
	if (state !== queryString.state) {
		return  res
		.status(401)
		.send("The state variables did not match. Could not identify response validity.");
	}
	
	let  token_for_further_processes = queryString.access_token;
	res.sendFile(path.join(__dirname, "/public/Greetings.html"));
});
```
Now we can directly access our query parameters over our request object and access the access_token for further processing. We then show a simply greeting to indicate that we have successfully fulfilled the OAuth2.0 implicit grant flow.

## Conclusion
In this blog post we implement a workaround for receiving a access token when using the OAuth2.0 implicit grant flow with a NodeJS backend. Since the Implicit grant flow relies on redirects between Browser based agents, we need to catch the request in the context of Browser Javascript and replace the fragment with a question mark. 


