package com.luisborrayo.catalogo_peliculasseries.beans;

import com.luisborrayo.catalogo_peliculasseries.service.DashboardService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
public class DashboardController {

    @Inject
    private DashboardService dashboardService;

    @GET
    @Path("/stats")
    public Response getDashboardStats() {
        try {
            Map<String, Long> stats = dashboardService.getDashboardStats();
            return Response.ok(stats).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}