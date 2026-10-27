package com.aerosaga;

import com.aerosaga.config.TemporalConfig;
import com.aerosaga.workflow.DroneMissionWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/missions")
public class MissionController {


private final WorkflowClient workflowClient;

public MissionController(WorkflowClient workflowClient) {
    this.workflowClient = workflowClient;
}

@PostMapping("/{droneId}")
public ResponseEntity<Map<String, String>> startMission(
        @PathVariable String droneId) {

    String workflowId = "mission-" + droneId;

    DroneMissionWorkflow workflow =
            workflowClient.newWorkflowStub(
                    DroneMissionWorkflow.class,
                    WorkflowOptions.newBuilder()
                            .setWorkflowId(workflowId)
                            .setTaskQueue(
                                    TemporalConfig.TASK_QUEUE)
                            .build()
            );

    WorkflowClient.start(
            workflow::executeMission,
            droneId
    );

    return ResponseEntity.accepted().body(
            Map.of(
                    "message", "Drone mission started successfully",
                    "droneId", droneId,
                    "workflowId", workflowId
            )
    );
}


}
