package com.sgl.backend.service;

import com.sgl.backend.exception.SglException;
import com.sgl.backend.dto.OpacUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OpacService {
    
    private final RestTemplate restTemplate;

    @Value("${opac.base-url}")
    private String opacBaseUrl;

    public OpacUserInfo fetchUserInfo(String code) {
        try {
            String url = opacBaseUrl + "/student/preview/" + code;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<OpacUserInfo> response = restTemplate.exchange(url, HttpMethod.GET, entity, OpacUserInfo.class);
            if (response.getBody() == null) {
                throw new SglException("OPAC user info failed: No response body");
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new SglException("OPAC user info failed: Invalid code " + code, e);
        } catch (Exception e) {
            throw new SglException("OPAC user info error: " + e.getMessage(), e);
        }
    }
}
