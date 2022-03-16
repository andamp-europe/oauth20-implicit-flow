const query_string = require("querystring");

//http://localhost:8080/oauth2/authorization/response_type=token&client_id=photo-app-client&state=8njkfds893ksHSD3bd&redirect_uri=http://localhost:8082/callback&scope=profile

const state = "8njkfds893ksHSD3bd";
const auth_token_endpoint = "http://localhost:8080/";
const query_params = {
  client_id: "client_id_implicit_authentication_app",
  redirect_uri: `http://localhost:8082/login/oauth/callback`,
};
// this objects contains information that will be passed as query params to the auth // token endpoint
const auth_token_params = {
  ...query_params,
  response_type: "token",
  state,
};
// the scopes (portion of user's data) we want to access
const scopes = ["profile"];
// a url formed with the auth token endpoint and the
const request_get_auth_code_url = `${auth_token_endpoint}?${new URLSearchParams(
  auth_token_params
)}&scope=${scopes.join(" ")}`;

module.exports = { request_get_auth_code_url };
