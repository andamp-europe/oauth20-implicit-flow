const express = require("express");
const path = require("path");
const cors = require("cors");
const utils = require("./utils");
const router = express();
const port = 8082;

const state = "8njkfds893ksHSD3bd";

router.use(express.static(__dirname + "/public"));
router.use(
  cors({
    origin: ["http://localhost:8080", "http://localhost:8082"],
  })
);

router.get("/auth", async (req, res) => {
  try {
    res.redirect(utils.auth_server_request(state));
  } catch (error) {
    res.sendStatus(500);
    console.error(error.message);
  }
});

router.get("/login/oauth/callback", async (req, res) => {
  res.sendFile(path.join(__dirname, "/public/OAuthRedirect.html"));
});

router.get("/login/callback", async (req, res) => {
  const queryString = req.query;
  if (!req.query) {
    return res.status(404).send("No Access Token provided");
  }

  if (state !== queryString.state) {
    return res
      .status(401)
      .send(
        "The state variables did not match. Could not identify response validity."
      );
  }
  let token_for_further_processes = queryString.access_token;

  res.sendFile(path.join(__dirname, "/public/Greetings.html"));
});

router.listen(port, () => console.log("Running server on port: " + port));
