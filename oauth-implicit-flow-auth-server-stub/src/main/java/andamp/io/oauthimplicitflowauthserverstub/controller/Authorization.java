package andamp.io.oauthimplicitflowauthserverstub.controller;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;

@RestController
public class Authorization {

    private static final String ACCESS_TOKEN_STUB="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJ1c2VyIjoiJmFtcCIsImlzcyI6ImxvY2FsaG9zdDo4MDgwIn0.nAzaO9TIAeG1BTK2j3OvXPz3xIzIDaI8-J1gAl08Qkg";


    @GetMapping("/oauth/authorization")
    @ResponseBody
    public static ResponseEntity<Void> authorizeWithResponseEntity(@RequestParam String response_type, @RequestParam String client_id, @RequestParam String redirect_uri, @RequestParam String scope, @RequestParam String state){
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirect_uri+"#state="+state+"&access_token="+ACCESS_TOKEN_STUB+"&token_type=bearer")).build();
    }
}
