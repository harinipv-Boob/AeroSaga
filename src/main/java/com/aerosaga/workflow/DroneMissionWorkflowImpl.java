package com.aerosaga.workflow;

import com.aerosaga.activities.DroneMissionActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class DroneMissionWorkflowImpl implements DroneMissionWorkflow {

        
private final DroneMissionActivities activities =
        Workflow.newActivityStub(
                DroneMissionActivities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofMinutes(2))
                        .setRetryOptions(
                                RetryOptions.newBuilder()
                                        .setMaximumAttempts(1)
                                        .build()
                        )
                        .build()
        );

@Override
public String executeMission(String droneId) {

    Workflow.getLogger(DroneMissionWorkflowImpl.class)
            .info("Starting mission for drone: " + droneId);

    activities.takeoff(droneId);
    activities.navigateToDestination(droneId);

    try {
        activities.dropPackage(droneId);

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Package delivered successfully for drone: " + droneId);

    } catch (Exception e) {

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .error("Package delivery failed for drone: " + droneId);

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Starting compensation: returning drone to base.");

        activities.returnToBase(droneId);

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Compensation completed. Drone returned to base.");

        return droneId
                + " | DELIVERY_FAILED"
                + " | COMPENSATION_COMPLETED"
                + " | Drone returned to base safely.";
    }

    activities.returnToBase(droneId);

    return droneId
            + " | DELIVERY_SUCCESSFUL"
            + " | MISSION_COMPLETED"
            + " | Drone returned to base.";
}


}
