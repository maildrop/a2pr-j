// -*- coding: utf-8; -*-
package net.shalab.mit.a2pr;
import java.lang.*;
import java.util.Date;

public final class A2prContext{
    public String name;
    public Date creationDate;
    public Date jobDate;

    public A2prContext(){
        this.name = null;
        this.creationDate = null;
        this.jobDate = null;
    }

    public synchronized A2prContext setName( final String name ){
        this.name = name;
        return this;
    }

    public synchronized A2prContext setCreationDate( final Date creationDate ){
        this.creationDate = creationDate;
        return this;
    }

    public synchronized A2prContext setJobDate( final Date jobDate ){
        this.jobDate = jobDate;
        return this;
    }

};
