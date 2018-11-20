package edu.oregonstate.mist.textbooksapi.health

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils

import javax.ws.rs.core.UriBuilder

class TextbooksHealthCheck extends HealthCheck {
    private HttpClient httpClient
    private URI coursesURI

    TextbooksHealthCheck(HttpClient httpClient, String verbaCompareURI) {
        this.httpClient = httpClient
        this.coursesURI = UriBuilder.fromPath(verbaCompareURI).path("compare/courses").build()
    }

    @Override
    protected Result check() throws Exception {
        HttpGet req = new HttpGet(coursesURI)
        HttpResponse res = httpClient.execute(req)
        EntityUtils.consumeQuietly(res.entity)
        int status = res.getStatusLine().getStatusCode()

        if (status == HttpStatus.SC_OK) {
            Result.healthy()
        } else {
            Result.unhealthy("Verba compare URI: ${coursesURI} returned status code: ${status}")
        }
    }
}
