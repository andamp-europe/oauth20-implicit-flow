const express = require("express");
const utils = require("./utils");
const path = require("path");
const cors = require("cors");
const port = 8082;
const router = express();
router.use(express.static(__dirname + "/public"));
router.use(
  cors({
    origin: ["http://localhost:8080", "http://localhost:8082"],
  })
);

router.get("/auth", async (req, res) => {
  console.log("auth endpoint called");
  try {
    res.redirect(utils.request_get_auth_code_url);
  } catch (error) {
    res.sendStatus(500);
    console.log(error.message);
  }
});

router.get("/redirected", async (req, res) => {});

router.get("/login/oauth/callback", async (req, res) => {
  console.log("serving redirect html" + JSON.stringify(req.query));
  res.sendFile(path.join(__dirname, "/public/OAuthRedirect.html"));
});

router.get("/login/callback", async (req, res) => {
  console.log(JSON.stringify(req.query));
  res.sendFile(path.join(__dirname, "/public/Greetings.html"));
});

router.listen(port, () => console.log("running server on: " + port));
