package com.example.veterinaria.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class DniApiService { 

    @Value("${api.dni.url}")
    private String apiUrl;

    @Value("${api.dni.token}")
    private String apiToken;

    public Map<String, Object> consultarDni(String dni) {
        String urlCompleta = apiUrl + dni + "&api_token=" + apiToken;
        
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(urlCompleta, Map.class);
    }
}
