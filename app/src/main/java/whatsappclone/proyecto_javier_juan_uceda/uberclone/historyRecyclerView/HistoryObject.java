package whatsappclone.proyecto_javier_juan_uceda.uberclone.historyRecyclerView;

public class HistoryObject {

   private String rideId;
   private String time;

   public HistoryObject(String rideId, String time){
      this.rideId = rideId;
      this.time = time;
   }

   public String getTime(){return time;}
   public void setTime(String time) {
      this.time = time;
   }
   public String getRideId(){return rideId;}
   public void setRideId(String rideId) {
      this.rideId = rideId;
   }
}
