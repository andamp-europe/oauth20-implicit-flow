package andamp.io.oauthimplicitflowauthserverstub.controller;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;

@RestController
public class Authorization {


    private static String access_token_stub="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkFtZCBBbXAiLCJpYXQiOjE1MTYyMzkwMjJ9.m3TEL66NgvxZIYWVcooglfucTtdhiUMF1trTGuA_hYI";

    @GetMapping("/oauth2/authorization")
    @ResponseBody
    public ModelAndView authorize(@RequestParam String response_type, @RequestParam String client_id, @RequestParam String redirect_uri, @RequestParam String scope, @RequestParam String state){
        System.out.println("Received params: response_type:" + response_type + "clientID:  "+client_id+" scope: " + scope +" state: "+ state);
        return new ModelAndView( "redirect:"+redirect_uri+"#state="+state+"&access_token="+access_token_stub+"&token_type=bearer"+
                "&expires_in=900");
    }

    @GetMapping("/oauth2/authorization2")
    @ResponseBody
    public ResponseEntity<Void> authorize2(@RequestParam String response_type, @RequestParam String client_id, @RequestParam String redirect_uri, @RequestParam String scope, @RequestParam String state){
        System.out.println("Received params: response_type:" + response_type + "clientID:  "+client_id+" scope: " + scope +" state: "+ state);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirect_uri+"#state="+state+"&access_token="+access_token_stub+"&token_type=bearer"+
                "&expires_in=900")).build();
    }
}
