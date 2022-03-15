package andamp.io.oauthimplicitflowauthserverstub.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class Authorization {

    private static String access_token_stub="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkFtZCBBbXAiLCJpYXQiOjE1MTYyMzkwMjJ9.m3TEL66NgvxZIYWVcooglfucTtdhiUMF1trTGuA_hYI";

    @GetMapping("/oauth2/authorization/")
    @ResponseBody
    public String authorize(@RequestParam String response_type, @RequestParam String client_id, @RequestParam String redirect_uri, @RequestParam String scope, @RequestParam String state){
        return redirect_uri+"#state="+state+"&access_token="+access_token_stub+"&token_type=bearer"+
                "&expires_in=900";
    }
}
