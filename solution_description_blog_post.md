# Fixing Implicit Flow Fragments disappearing with NodeJS back-ends

In this post, we will handle a problem that occurs, when trying to utilize the OAuth2.0 implicit grant flow with a NodeJs   
back-end. The problem arises when the authorization server redirects the user to the specified redirection_uri with the access token inserted into the URI as a fragment parameter. This behavior is specified in [RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749#section-4.2) and can not be altered by the user. If this redirection_uri points to a NodeJS endpoint, which does not serve a HTML front-end, therefore including the Browser in the process,  the fragment part of the URI simply gets omitted and never arrives at our NordeJS back-end.   
 
Since there is no way of altering the implicit flow itself, a workaround needs to be implemented so that we can receive the access_token in our NodeJS back-end. The solution presented here, first points the redirection URI to a NodeJS endpoint that serves a HTML file. The sole purpose of this html page is to replace the fragment in the URI with a question mark and send this edited version to a different NodeJS endpoint which handles further processing of the parameters sent in the URI. 

## Why do we care about implicit flow?
The reason for needing a workaround is that initially, this grant flow was optimized for clients which operated mostly in the browser. It relies on requests being redirected between browser based user agents and serves the access token as a fragment parameter directly in the request from the authorization server. 
Even though this approach may be outdated and authorization code grants  or client credential grant flow preferable, you might have to integrate it into an application which is already in existence and requires you to use this flow. 

## Our solution
For the purpose of this post, we implemented a simple NodeJS back-end which communicates with a very simple authorization server stub. This server takes the request from the client and redirects it to the specified redirect_uri with the access token attached In the URI as a fragment.
We then expose an endpoint on our NodeJS server which serves an HTML file at the specified redirect_uri and starts a script for exchanging the fragment with a question mark. Then a redirection to an endpoint which handles the now valid URI and extracts the access token happens. 

### Start of the flow: The login page
To properly simulate the flow, we start out with a simple login page which calls our `/auth` endpoint when `Login` is pressed. 

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
The corresponding server part, redirects the user to the authorization server which we have implemented ourselves. Usually, you would have to first register your client with the authorization server and get a client_id and secret from this process. Since it is not relevant to our problem, we will simply hard code these values and not care about them on our authentication server stub. In our server.js file, we expose the specified `/auth` endpoint through an express app. We then build the request in a separate function and redirect the caller to the authentication server with the query parameters in place. For the Implicit flow, we have to specify the following query parameters: 
 - `redirect_uri`: The URI to which the authentication server will redirect the request with the access token attached as a fragment parameter
 - `response_type`: We need to set the value of this parameter to "token" to indicate to the authentication server that we want to use the Implicit Grant flow
 - `state`: This value serves as a measure to prevent CSRF attacks. The authentication server will send this value back with the access token. We compare the returned state to our initial state value to ensure that the response was replying to our request and therefore identify it.
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
const  state = process.env.STATE;

router.get("/auth", async (req, res) => {
	try {
		res.redirect(utils.auth_server_request(state));
	} catch (error) {
		res.sendStatus(500);
		console.error(error.message);
	}
});

router.listen(port, () =>  console.log("Running server on port: " + port));
```
We use a utils.js file to separate the server code from the utility function that builds our request. Notice that we pass a static `state` to the authentication server. In a real application this should not be done and this value should be randomly generated. For simplicity sake, we work with the static version for now. 
 
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
For now, we are simply redirecting the User to the authentication server as soon as the Login link is clicked. Now we need to actually implement the solution that solves our initial problem. For this purpose, we first create a new HTML file for receiving the response from the authentication server. Notice that we have defined `login/oauth/callback` as our redirect_uri path. This means that the response from the server will be directed at our  `login/oauth/callback` endpoint in our back-end. This endpoint simply serves a HTML file which handles the actual fix to this problem that we are trying to solve. 
The endpoint which serves the HTML looks as follows and is located in our `server.js` file: 
```javascript
router.get("/login/oauth/callback", async (req, res) => {
	res.sendFile(path.join(__dirname, "/public/OAuthRedirect.html"));
});
```
Now to our simple but powerful HTML file which actually solves our fragment dilemma:

`OAuthRedirect.html`
```html
<!DOCTYPE  html>
<html  lang="en">
	<head>
		<meta  charset="UTF-8"  />
		<title>OAuth 2.0 Implicit Grant flow redirect</title>
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
This simple file does a very important job for us. It takes the fragment from the URI that we received from the authentication server and replaces it with a question mark. Replacing the `oauth/` part is not as important, it simply allows as to call our endpoint for processing the received URI. We then simply set the window location to the new URI, triggering our endpoint and receiving the access token in our NodeJS back-end. 

Now that we are not dealing with a fragment anymore, we can directly process the request without Browser JavaScript. Let's look at what we do with this redirect in our back-end in `server.js`
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
Now we can directly access our query parameters over our request object and access the access_token for further processing. We then show a simply greeting to indicate that we have successfully fulfilled the OAuth2.0 Implicit Grant flow.


## Improvements and Future Work
The main goal of OAuth2.0 is securely authenticating requests. Until now, we concentrated on showing how our solution work so that you can build your secure applications on top of it. Therefore we tried to use as little of dependencies as possible. Let's go a step further and add [passport.js](https://www.npmjs.com/package/passport) to our project. It will authenticate the JWT token that we receive from the server and make sure that conditions that we specify are met and the signature of the token is valid. For this purpose, we need to first download passport and the passport strategy that we will be using called  [passport-jwt](https://www.passportjs.org/packages/passport-jwt/). 
 `npm i passport passport-jwt`
 
 In the server.js file, add the following code at the top: 
```javascript
    const  passport = require("passport");
    const  JwtStrategy = require("passport-jwt").Strategy;
    const  ExtractJwt = require("passport-jwt").ExtractJwt;
    

    const  opts = {};
    const  shared_secret = process.env.SHARED_SECRET;
    opts.jwtFromRequest = ExtractJwt.fromUrlQueryParameter("access_token");
    opts.secretOrKey = shared_secret;
```
The shared secret is exchanged between your client and the authentication server in a previous setup. Passport.js will use this key to verify the signature of the token that has been sent by the server. 

passport.js is our base dependency and we are using the strategy passport-jwt additionally to verify tokens. Now we need to create a new JwtStrategy with our defined options and tell passport.js to use it. We achieve this with the following code:
```javascript
passport.use(
	new  JwtStrategy(opts, function (jwt_payload, done) {
		try {
			return  done(null, jwt_payload.user);
		} catch (error) {
			return  done(error);
		}
	})
);
```
As a last step, we add our configured passport authentication to the corresponding endpoint:

```javascript
router.get("/login/callback", passport.authenticate("jwt", { session:  false }),
	async (req, res, next) => {
				.
				.
				.
	res.sendFile(path.join(__dirname, "/public/Greetings.html"));
	}
)
```
Now, when we receive the token from the authentication server, passport.js will verify its signature with the shared key and ensure it's validity. If a token with an invalid signature is received, passport.js throws an error.  We can add further checks to our opts object and passport.jws will validate against them. 

As a future project, we would like to implement the solution presented in this article into a passport.js strategy. 

## Conclusion
In this blog post we implement a workaround for a problem that arises when receiving an access token using the OAuth2.0 Implicit Grant flow with a NodeJS back-end. Since the Implicit Grant flow relies on redirects between Browser based agents, we need to catch the request in the context of Browser JavaScript. We then replace the fragment with a question mark and call the appropriate endpoint for further processing. 


