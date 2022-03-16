package andamp.io.oauthimplicitflowauthserverstub.controller;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;

@RestController
public class Authorization {

    private static final String ACCESS_TOKEN_STUB="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBdXRoIFNlcnZlciBTdHViIiwibmFtZSI6IiZhbXAiLCJpYXQiOjE1MTYyMzkwMjJ9.F8jDm_DMnpOVcpx5cQY5UUOdAMxrqQGv-fXh_V01MEw";

    @GetMapping("/oauth/authorization")
    @ResponseBody
    public static ResponseEntity<Void> authorizeWithResponseEntity(@RequestParam String response_type, @RequestParam String client_id, @RequestParam String redirect_uri, @RequestParam String scope, @RequestParam String state){
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirect_uri+"#state="+state+"&access_token="+ACCESS_TOKEN_STUB+"&token_type=bearer"+
                "&expires_in=900")).build();
    }
}
