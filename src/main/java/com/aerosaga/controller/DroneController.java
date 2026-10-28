package com.aerosaga.controller;

import com.aerosaga.model.Drone;
import com.aerosaga.service.DroneService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174"
})
public class DroneController {

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @GetMapping
    public List<Drone> getAllDrones() {
        return droneService.getAllDrones();
    }

    @PostMapping
    public Drone addDrone(@RequestBody Drone drone) {
        return droneService.addDrone(drone);
    }
}

