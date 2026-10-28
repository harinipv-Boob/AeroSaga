import { useEffect, useState } from "react";
import "./App.css";

const initialDrones = [
  {
    droneId: "DRONE001",
    droneName: "Delivery Drone",
    batteryLevel: 85,
    status: "AVAILABLE",
  },
  {
    droneId: "DRONE002",
    droneName: "Survey Drone",
    batteryLevel: 60,
    status: "AVAILABLE",
  },
];

const terminalStatuses = [
  "WORKFLOW_EXECUTION_STATUS_COMPLETED",
  "WORKFLOW_EXECUTION_STATUS_FAILED",
  "WORKFLOW_EXECUTION_STATUS_CANCELED",
  "WORKFLOW_EXECUTION_STATUS_TERMINATED",
  "WORKFLOW_EXECUTION_STATUS_TIMED_OUT",
];

function App() {
  const [drones, setDrones] = useState(initialDrones);
  const [connection, setConnection] = useState("Connecting...");
  const [loadingDrone, setLoadingDrone] = useState("");
  const [message, setMessage] = useState("");
  const [missions, setMissions] = useState({});

  useEffect(() => {
    fetch("http://localhost:8080/api/drones")
      .then((response) => {
        if (!response.ok) {
          throw new Error("Backend request failed");
        }
        return response.json();
      })
      .then((data) => {
        setDrones(data);
        setConnection("Connected");
      })
      .catch(() => {
        setConnection("Offline");
      });
  }, []);

  function updateMission(droneId, missionData) {
    setMissions((previous) => ({
      ...previous,
      [droneId]: {
        ...previous[droneId],
        ...missionData,
      },
    }));
  }

  async function monitorMission(droneId, workflowId) {
    let attempts = 0;
    const maxAttempts = 150;

    while (attempts < maxAttempts) {
      try {
        const response = await fetch(
          `http://localhost:8080/api/missions/${encodeURIComponent(
            workflowId
          )}/status`
        );

        if (!response.ok) {
          throw new Error("Could not retrieve mission status");
        }

        const data = await response.json();

        updateMission(droneId, {
          workflowId,
          status: data.status,
          deliveryStatus: data.deliveryStatus || "UNKNOWN",
          compensationStatus:
            data.compensationStatus || "NOT_CONFIRMED",
          missionResult: data.missionResult || "",
        });

        if (terminalStatuses.includes(data.status)) {
          if (data.deliveryStatus === "DELIVERY_FAILED") {
            setMessage(
              `Delivery failed for ${droneId}, but the drone returned safely.`
            );
          } else if (data.deliveryStatus === "DELIVERY_SUCCESSFUL") {
            setMessage(
              `Package delivered successfully by ${droneId}!`
            );
          } else if (
            data.status === "WORKFLOW_EXECUTION_STATUS_COMPLETED"
          ) {
            setMessage(
              `Workflow completed for ${droneId}. Check the mission result below.`
            );
          } else {
            setMessage(
              `Mission ended with status ${data.status} for ${droneId}.`
            );
          }

          return;
        }
      } catch {
        updateMission(droneId, {
          workflowId,
          status: "STATUS_CHECK_ERROR",
        });
      }

      attempts += 1;
      await new Promise((resolve) => setTimeout(resolve, 2000));
    }

    updateMission(droneId, {
      workflowId,
      status: "STATUS_CHECK_TIMEOUT",
    });

    setMessage(
      `Mission status is still being checked for ${droneId}. Check Temporal UI for the latest status.`
    );
  }

  async function startMission(droneId) {
    setLoadingDrone(droneId);
    setMessage("");

    updateMission(droneId, {
      workflowId: "",
      status: "STARTING",
      deliveryStatus: "NOT_STARTED",
      compensationStatus: "NOT_CONFIRMED",
      missionResult: "",
    });

    try {
      const response = await fetch(
        `http://localhost:8080/api/missions/${encodeURIComponent(
          droneId
        )}`,
        {
          method: "POST",
        }
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(
          data.error || data.message || "Mission could not start"
        );
      }

      const workflowId = data.workflowId;

      updateMission(droneId, {
        workflowId,
        status: "WORKFLOW_EXECUTION_STATUS_RUNNING",
        deliveryStatus: "IN_PROGRESS",
        compensationStatus: "NOT_CONFIRMED",
        missionResult: "",
      });

      setMessage(`Mission started for ${droneId}.`);

      monitorMission(droneId, workflowId);
    } catch (error) {
      updateMission(droneId, {
        workflowId: "",
        status: "START_FAILED",
        deliveryStatus: "UNKNOWN",
        compensationStatus: "NOT_CONFIRMED",
        missionResult: "",
      });

      setMessage(`Could not start mission: ${error.message}`);
    } finally {
      setLoadingDrone("");
    }
  }

  function formatStatus(status) {
    if (!status) {
      return "NOT STARTED";
    }

    return status
      .replace("WORKFLOW_EXECUTION_STATUS_", "")
      .replaceAll("_", " ");
  }

  function statusClass(status) {
    if (!status) {
      return "mission-status-idle";
    }

    if (
      status === "DELIVERY_FAILED" ||
      status === "WORKFLOW_EXECUTION_STATUS_FAILED" ||
      status === "START_FAILED" ||
      status === "STATUS_CHECK_ERROR"
    ) {
      return "mission-status-failed";
    }

    if (
      status === "DELIVERY_SUCCESSFUL" ||
      status === "COMPENSATION_COMPLETED" ||
      status === "WORKFLOW_EXECUTION_STATUS_COMPLETED"
    ) {
      return "mission-status-completed";
    }

    if (
      status === "WORKFLOW_EXECUTION_STATUS_RUNNING" ||
      status === "IN_PROGRESS" ||
      status === "STARTING"
    ) {
      return "mission-status-running";
    }

    return "mission-status-idle";
  }

  return (
    <main className="dashboard">
      <header className="topbar">
        <div className="brand">
          <div className="brand-icon">✦</div>
          <div>
            <h1>AEROSAGA</h1>
            <p>Autonomous Mission Control</p>
          </div>
        </div>

        <div className="connection">
          <span
            className={`status-dot ${
              connection === "Connected" ? "online" : ""
            }`}
          />
          Backend: {connection}
        </div>
      </header>

      <section className="welcome">
        <div>
          <p className="eyebrow">FLEET OVERVIEW / 01</p>
          <h2>Mission Control Center</h2>
          <p className="subtitle">
            Monitor your fleet and launch autonomous drone missions.
          </p>
        </div>

        <div className="date-card">
          <span>FLIGHT SYSTEM</span>
          <strong>AEROSAGA / V1</strong>
          <small>Temporal workflow engine</small>
        </div>
      </section>

      <section className="stats">
        <article className="stat-card">
          <span>Total Drones</span>
          <strong>
            {drones.length.toString().padStart(2, "0")}
          </strong>
          <small>Registered in fleet</small>
        </article>

        <article className="stat-card">
          <span>Available</span>
          <strong>
            {drones
              .filter((drone) => drone.status === "AVAILABLE")
              .length.toString()
              .padStart(2, "0")}
          </strong>
          <small>Ready for missions</small>
        </article>

        <article className="stat-card">
          <span>Average Battery</span>
          <strong>
            {drones.length
              ? Math.round(
                  drones.reduce(
                    (total, drone) =>
                      total + Number(drone.batteryLevel || 0),
                    0
                  ) / drones.length
                )
              : 0}
            %
          </strong>
          <small>Across registered drones</small>
        </article>
      </section>

      <section className="fleet-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">LIVE INVENTORY</p>
            <h2>Drone Fleet</h2>
          </div>

          <span className="fleet-count">
            {drones.length} DRONES
          </span>
        </div>

        {message && <div className="message">{message}</div>}

        <div className="drone-grid">
          {drones.map((drone, index) => {
            const mission = missions[drone.droneId];
            const missionStatus = mission?.status;

            return (
              <article className="drone-card" key={drone.droneId}>
                <div className="drone-card-top">
                  <div className="drone-symbol">✈</div>
                  <span className="available-badge">
                    {drone.status}
                  </span>
                </div>

                <p className="drone-number">
                  UNIT / 0{index + 1}
                </p>

                <h3>{drone.droneName}</h3>
                <p className="drone-id">{drone.droneId}</p>

                <div className="battery-heading">
                  <span>Battery level</span>
                  <strong>{drone.batteryLevel}%</strong>
                </div>

                <div className="battery-track">
                  <div
                    className="battery-fill"
                    style={{
                      width: `${Math.min(
                        100,
                        Math.max(
                          0,
                          Number(drone.batteryLevel) || 0
                        )
                      )}%`,
                    }}
                  />
                </div>

                <div className="mission-info">
                  <span>Mission Status</span>
                  <strong className={statusClass(missionStatus)}>
                    {formatStatus(missionStatus)}
                  </strong>

                  {mission?.workflowId && (
                    <div className="workflow-id">
                      <span>Workflow ID</span>
                      <small>{mission.workflowId}</small>
                    </div>
                  )}

                  {mission && (
                    <div className="mission-details">
                      <div className="mission-detail-row">
                        <span>Delivery Status</span>
                        <strong
                          className={statusClass(
                            mission.deliveryStatus
                          )}
                        >
                          {formatStatus(mission.deliveryStatus)}
                        </strong>
                      </div>

                      <div className="mission-detail-row">
                        <span>Compensation</span>
                        <strong
                          className={statusClass(
                            mission.compensationStatus
                          )}
                        >
                          {formatStatus(
                            mission.compensationStatus
                          )}
                        </strong>
                      </div>

                      {mission.missionResult && (
                        <div className="mission-result">
                          <span>Final Mission Result</span>
                          <p>{mission.missionResult}</p>
                        </div>
                      )}
                    </div>
                  )}
                </div>

                <button
                  className="mission-button"
                  onClick={() => startMission(drone.droneId)}
                  disabled={Boolean(loadingDrone)}
                >
                  {loadingDrone === drone.droneId
                    ? "Launching..."
                    : "▶ Start Mission"}
                </button>
              </article>
            );
          })}
        </div>
      </section>

      <footer>
        <span>AEROSAGA MISSION CONTROL</span>
        <span>Java · Spring Boot · Temporal · React</span>
      </footer>
    </main>
  );
}

export default App;

