package com.aerosaga;

import com.aerosaga.config.TemporalConfig;
import com.aerosaga.workflow.DroneMissionWorkflow;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.client.WorkflowServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = {
"http://localhost:5173",
"http://localhost:5174"
})
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
                            .setTaskQueue(TemporalConfig.TASK_QUEUE)
                            .build()
            );

    WorkflowClient.start(workflow::executeMission, droneId);

    return ResponseEntity.accepted().body(
            Map.of(
                    "message", "Drone mission started successfully",
                    "droneId", droneId,
                    "workflowId", workflowId
            )
    );
}

@GetMapping("/{workflowId}/status")
public ResponseEntity<Map<String, String>> getMissionStatus(
        @PathVariable String workflowId) {

    try {
        WorkflowStub workflowStub =
                workflowClient.newUntypedWorkflowStub(workflowId);

        WorkflowExecutionStatus status =
                workflowStub.describe().getStatus();

        Map<String, String> response = new HashMap<>();
        response.put("workflowId", workflowId);
        response.put("status", status.name());

        if (status == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED) {

            try {
                String missionResult = workflowStub.getResult(String.class);

                response.put("missionResult", missionResult);

                if (missionResult.contains("DELIVERY_FAILED")) {
                    response.put("deliveryStatus", "DELIVERY_FAILED");
                    response.put("compensationStatus", "COMPENSATION_COMPLETED");
                } else if (missionResult.contains("DELIVERY_SUCCESSFUL")) {
                    response.put("deliveryStatus", "DELIVERY_SUCCESSFUL");
                    response.put("compensationStatus", "NOT_REQUIRED");
                } else {
                    response.put("deliveryStatus", "UNKNOWN");
                    response.put("compensationStatus", "UNKNOWN");
                }

            } catch (Exception resultException) {
                response.put("deliveryStatus", "RESULT_UNAVAILABLE");
                response.put("compensationStatus", "UNKNOWN");
            }
        } else {
            response.put("deliveryStatus", "IN_PROGRESS");
            response.put("compensationStatus", "NOT_CONFIRMED");
        }

        return ResponseEntity.ok(response);

    } catch (WorkflowServiceException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "workflowId", workflowId,
                        "error", "Workflow not found or unavailable"
                )
        );
    }
}


}
