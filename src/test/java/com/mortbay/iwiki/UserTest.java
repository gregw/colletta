/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 16/04/2004
 * $Id: UserTest.java,v 1.1 2006/01/21 17:28:01 gregw Exp $
 * ============================================== */
 
package com.mortbay.iwiki;

import com.mortbay.iwiki.User;

import junit.framework.TestCase;

/* ------------------------------------------------------------------------------- */
/** 
 * 
 * @version $Revision: 1.1 $
 * @author gregw
 */
public class UserTest extends TestCase
{
    /* ------------------------------------------------------------------------------- */
    /** Constructor.
     * 
     */
    public UserTest()
    {
    }
    
    public void testUserFromString()
    {
        User user;
        
        user=User.fromString("Fred", "*:Fred%20Nurke:fred@somewhere:12%20345:en");
        assertEquals("Fred",user.getName());
        assertEquals("12 345",user.getContact());
        assertEquals("en",user.getLang());
        assertTrue(!user.is("DOBERRY"));
        assertTrue(!user.isOwner());
        assertTrue(!user.isManager());
        assertTrue(!user.owns("bc"));
        assertTrue(!user.manages("bc"));

        user=User.fromString("Fred", "secret:Fred%20Nurke:fred@somewhere::en");
        assertEquals("Fred",user.getName());
        assertEquals("en",user.getLang());
        assertEquals(0,user.roles.size());
        assertEquals(0,user.owns.size());
        assertEquals(0,user.manages.size());
        assertTrue(!user.is("DOBERRY"));
        assertTrue(!user.isOwner());
        assertTrue(!user.isManager());
        assertTrue(!user.owns("bc"));
        assertTrue(!user.manages("bc"));
        

        user=User.fromString("Fred", "secret:Fred%20Nurke:fred@somewhere:12345:en:DOBERRY");
        assertEquals("Fred",user.getName());
        assertEquals(1,user.roles.size());
        assertEquals(0,user.owns.size());
        assertEquals(0,user.manages.size());
        assertTrue(user.is("DOBERRY"));
        assertTrue(!user.isOwner());
        assertTrue(!user.isManager());
        assertTrue(!user.owns("bc"));
        assertTrue(!user.manages("bc"));
        assertEquals("Fred Nurke",user.getFullName());
        assertEquals("fred@somewhere",user.getEmail());
        assertEquals("en",user.getLang());
        
        user=User.fromString("Fred", "secret:Fred%20Nurke:fred@somewhere:12345:en:DOBERRY:OWNER");
        assertEquals("Fred",user.getName());
        assertEquals(2,user.roles.size());
        assertEquals(0,user.owns.size());
        assertEquals(0,user.manages.size());
        assertTrue(user.is("DOBERRY"));
        assertTrue(user.isOwner());
        assertTrue(!user.isManager());
        assertTrue(!user.owns("bc"));
        assertTrue(!user.manages("bc"));
        
        user=User.fromString("Fred", "secret:Fred%20Nurke:fred@somewhere:12345:en:DOBERRY:OWNER[]");
        assertEquals("Fred",user.getName());
        assertEquals(2,user.roles.size());
        assertEquals(0,user.owns.size());
        assertEquals(0,user.manages.size());
        assertTrue(user.is("DOBERRY"));
        assertTrue(user.isOwner());
        assertTrue(!user.isManager());
        assertTrue(!user.owns("bc"));
        assertTrue(!user.manages("bc"));
        
        user=User.fromString("Fred", "secret:Fred%20Nurke:fred@somewhere:12345:en:DOBERRY:OWNER[dm]");
        assertEquals("Fred",user.getName());
        assertEquals(2,user.roles.size());
        assertEquals(1,user.owns.size());
        assertEquals(0,user.manages.size());
        assertTrue(user.is("DOBERRY"));
        assertTrue(user.isOwner());
        assertTrue(!user.isManager());
        assertTrue(!user.owns("bc"));
        assertTrue(!user.manages("bc"));
        
        user=User.fromString("Fred", "secret:Fred%20Nurke:fred@somewhere:12345:en:DOBERRY:OWNER[dm,bc]");
        assertEquals("Fred",user.getName());
        assertEquals(2,user.roles.size());
        assertEquals(2,user.owns.size());
        assertEquals(0,user.manages.size());
        assertTrue(user.is("DOBERRY"));
        assertTrue(user.isOwner());
        assertTrue(!user.isManager());
        assertTrue(user.owns("bc"));
        assertTrue(!user.manages("bc"));
        
        user=User.fromString("Fred", "secret:Fred%20Nurke:fred@somewhere:12345:en:DOBERRY:OWNER[dm,bc]:MANAGER");
        assertEquals("Fred",user.getName());
        assertEquals(3,user.roles.size());
        assertEquals(2,user.owns.size());
        assertEquals(0,user.manages.size());
        assertTrue(user.is("DOBERRY"));
        assertTrue(user.isOwner());
        assertTrue(user.isManager());
        assertTrue(user.owns("bc"));
        assertTrue(user.manages("bc"));
        
        user=User.fromString("Fred", "secret:Fred%20Nurke:fred@somewhere:12345:en:DOBERRY:OWNER[dm,bc]:MANAGER[dm]");
        assertEquals("Fred",user.getName());
        assertEquals(3,user.roles.size());
        assertEquals(2,user.owns.size());
        assertEquals(1,user.manages.size());
        assertTrue(user.is("DOBERRY"));
        assertTrue(user.isOwner());
        assertTrue(user.isManager());
        assertTrue(user.owns("bc"));
        assertTrue(!user.manages("bc"));

        user=User.fromString("Fred", "secret:Fred%20Nurke:fred@somewhere:12345:en:DOBERRY:OWNER[dm,bc]:MANAGER[bc,dm]");
        assertEquals("Fred",user.getName());
        assertEquals(3,user.roles.size());
        assertEquals(2,user.owns.size());
        assertEquals(2,user.manages.size());
        assertTrue(user.is("DOBERRY"));
        assertTrue(user.isOwner());
        assertTrue(user.isManager());
        assertTrue(user.owns("bc"));
        assertTrue(user.manages("bc"));
        
    }
    
    public void testCred()
    {
        User user=User.fromString("Fred", User.hash("Fred","secret")+":Fred%20Nurke:fred@somewhere:12345:en");
        assertTrue(!user.checkCredential("wrong"));
        assertTrue(user.checkCredential("secret"));
    }
}
