/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;

/**
 * @author janb
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ReservationDataFilter
{
    /**
     * Test whether this ReservationData object meets a criteria
     * @param rd
     * @return
     */
    public boolean accept (ReservationData rd);
}
