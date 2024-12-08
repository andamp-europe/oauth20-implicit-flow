const auth_server_endpoint = "http://localhost:8080/";
const query_params = {
  client_id: "client_id_implicit_authentication_app",
  redirect_uri: `http://localhost:8082/login/oauth/callback`,
};

const auth_server_request = (state) => {
  const auth_token_params = {
    ...query_params,
    response_type: "token",
    state,
    scope: "profile",
  };

  return `${auth_server_endpoint}?${new URLSearchParams(auth_token_params)}`;
};

module.exports = { auth_server_request };
