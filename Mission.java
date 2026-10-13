public class Mission {

    String missionName;
    String location;
    String status;

    void startMission() {
        status = "In Progress";
        System.out.println("Mission Started!");
    }

    void displayMission() {
        System.out.println("Mission Name: " + missionName);
        System.out.println("Location: " + location);
        System.out.println("Status: " + status);
    }

    public static void main(String[] args) {

        Mission mission1 = new Mission();

        mission1.missionName = "Delivery Mission";
        mission1.location = "Chennai";
        mission1.status = "Pending";

        mission1.displayMission();

        mission1.startMission();

        mission1.displayMission();
    }
}