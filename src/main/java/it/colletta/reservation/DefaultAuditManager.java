package it.colletta.reservation;

import java.util.logging.Logger;

import com.mortbay.iwiki.User;

/**
 * DefaultAuditManager.java
 *
 *
 * Created: Sun Mar 28 17:22:33 2004
 *
 * @author <a href="mailto:janb@wafer">Jan Bartel</a>
 * @version 1.0
 */
public class DefaultAuditManager implements AuditManager
{
    private static final Logger log = Logger.getLogger(DefaultAuditManager.class.getName());

    public void record (Object o, String comment)
    {
        User user = User.getCurrentUser();
        log.info ("AUDIT: "+"["+(user != null?user.getName(): "")+ "]"+"["+ (comment != null? comment: "")  + "]"+ o);
    }
    
} // DefaultAuditManager
