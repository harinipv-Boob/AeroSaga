package com.aerosaga.service;

import com.aerosaga.model.Drone;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class DroneService {

private final List<Drone> drones = new ArrayList<>();

public DroneService() {
    drones.add(new Drone("DRONE001", "Delivery Drone", 85.0, "AVAILABLE"));
    drones.add(new Drone("DRONE002", "Survey Drone", 60.0, "AVAILABLE"));
}

public List<Drone> getAllDrones() {
    return drones;
}

public Drone addDrone(Drone drone) {
    drones.add(drone);
    return drone;
}

}
