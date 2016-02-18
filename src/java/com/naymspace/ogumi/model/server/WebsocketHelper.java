
package com.naymspace.ogumi.model.server;

import javax.json.JsonValue;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebsocketHelper {
    
    public static void addSession(Set<Session> sessions, Session session) {
        sessions.add(session);
        System.out.println("Added Session "+session.getId());
    }

    public static void removeSession(Set<Session> sessions, Session session) {
        sessions.remove(session);
        System.out.println("Removed Session "+session.getId());
    }
    
    protected static void sendToAllConnectedSessions(Set<Session> sessions, JsonValue message) {
        Session[] sessarr = sessions.toArray(new Session[sessions.size()]);
        for (int i = 0; i < sessarr.length; i++) {
            Session session = sessarr[i];
            WebsocketHelper.sendToSession(session, message, sessions);
        }
    }
    
    protected static void sendToSession(Session session, JsonValue message, Set<Session> sessions) {
        try {
            synchronized (session) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message.toString());
                }
            }
        } catch (IOException | IllegalStateException ex) {
            Logger.getLogger(WebsocketHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected static void sendPing(Set<Session> sessions) {
        Session[] sessarr = sessions.toArray(new Session[sessions.size()]);
        for (int i = 0; i < sessarr.length; i++) {
            Session session = sessarr[i];
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText("{\"ping\": \"Still alive\"}");
                    }
                }
            } catch (IOException ex) {
                sessions.remove(session);
                Logger.getLogger(WebsocketHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
