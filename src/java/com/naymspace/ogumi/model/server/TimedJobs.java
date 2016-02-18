
package com.naymspace.ogumi.model.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimedJobs implements ServletContextListener {
    
    private final Timer timer;
    
    public TimedJobs() {
        this.timer = new Timer();
    }
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        /*
        Date now = Calendar.getInstance().getTime();
        // deactivate all ended sessions/ activate not started sessions each minute
        timer.scheduleAtFixedRate(new CheckSession(), now, 60000);
        //send pings to websocket clients each two minutes
        timer.scheduleAtFixedRate(new SendPings(), now, 120000);
        //delete experiments that do not belong to a session each hour
        timer.scheduleAtFixedRate(new DeleteExperiments(), now, 3600000);
        System.out.println("Registered Timed Jobs.");
        */
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
    
    class CheckSession extends TimerTask {
        @Override
        public void run() {
            /*DbConnect.deactivateEndedSessions();
            DbConnect.activateSessions();*/
        }
    }
    
    class SendPings extends TimerTask {
        @Override
        public void run() {
            /*UserInputResource.sendPing();
            UserInputAdminResource.sendPing();
            IncentiveTaskResource.sendPing();*/
        }
    }
    
    class DeleteExperiments extends TimerTask {
        @Override
        public void run() {
            //DbConnect.deleteUnconnectedExperiments();
        }
    }
    
}
