package com.aerosaga.activities;

import org.springframework.stereotype.Component;

@Component
public class DroneMissionActivitiesImpl implements DroneMissionActivities {

    @Override
    public String takeoff(String droneId) {
        System.out.println("🚁 " + droneId + " is taking off...");
        return droneId + " has successfully taken off.";
    }

    @Override
    public String navigateToDestination(String droneId) {
        System.out.println("🧭 " + droneId + " is navigating to destination...");
        return droneId + " has reached the destination.";
    }

    @Override
    public String dropPackage(String droneId) {
        System.out.println("📦 " + droneId + " is dropping the package...");

        // Simulate package delivery failure for testing
        if ("DRONE_FAIL".equalsIgnoreCase(droneId)) {
            System.out.println(
                    "❌ Simulated package delivery failure for " + droneId
            );

            throw new IllegalStateException(
                    "Simulated package delivery failure"
            );
        }

        System.out.println("✅ Package delivered successfully by " + droneId);

        return droneId + " has successfully delivered the package.";
    }

    @Override
    public String returnToBase(String droneId) {
        System.out.println("🏠 " + droneId + " is returning to base...");
        System.out.println("✅ " + droneId + " has returned to base successfully.");

        return droneId + " has returned to base successfully.";
    }
}

