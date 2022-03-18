require("dotenv").config();
const express = require("express");
const cors = require("cors");
const path = require("path");
const utils = require("./utils");
const router = express();
const passport = require("passport");

const shared_secret = process.env.SHARED_SECRET;
const JwtStrategy = require("passport-jwt").Strategy;
const ExtractJwt = require("passport-jwt").ExtractJwt;
const opts = {};
opts.jwtFromRequest = ExtractJwt.fromUrlQueryParameter("access_token");
opts.secretOrKey = shared_secret;

const state = process.env.STATE;

passport.use(
  new JwtStrategy(opts, function (jwt_payload, done) {
    try {
      return done(null, jwt_payload.user);
    } catch (error) {
      return done(error);
    }
  })
);

const port = 8082;

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

router.get(
  "/login/callback",
  passport.authenticate("jwt", { session: false }),
  async (req, res) => {
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
  }
);

router.listen(port, () => console.log("Running server on port: " + port));
