public class Drone {

    String droneName;
    String status;

    void displayDetails() {
        System.out.println("Drone Name: " + droneName);
        System.out.println("Drone Status: " + status);
    }

    public static void main(String[] args) {

        Drone drone1 = new Drone();

        drone1.droneName = "AeroSaga-01";
        drone1.status = "Ready";

        drone1.displayDetails();
    }
}