package com.flipkart.flux.examples.benchmark;

import com.flipkart.flux.client.model.Task;

public class ArithmeticOperations {

    @Task(version = 1,retries = 3,timeout = 1000)
    public  EventTypeInteger add(EventTypeInteger a , EventTypeInteger b){
        return new EventTypeInteger(a.getValue()+b.getValue());
    }

    @Task(version = 1,retries = 3,timeout = 1000)
    public  EventTypeInteger subtract(EventTypeInteger a , EventTypeInteger b){
        return new EventTypeInteger(Math.abs(a.getValue()-b.getValue()));
    }

    @Task(version = 1,retries = 3,timeout = 1000)
    public  EventTypeInteger increment(EventTypeInteger a){
        return new EventTypeInteger(a.getValue()+1);
    }

    @Task(version = 1,retries = 3,timeout = 1000)
    public  EventTypeInteger decrement(EventTypeInteger a){
        return new EventTypeInteger(a.getValue()-1);
    }

    @Task(version = 1,retries = 3,timeout = 1000)
    public  EventTypeInteger multiply(EventTypeInteger a , EventTypeInteger b){
        makePositive(a,b);
        return new EventTypeInteger(a.getValue()*b.getValue());
    }

    @Task(version = 1,retries = 3,timeout = 1000)
    public  EventTypeInteger divide(EventTypeInteger a , EventTypeInteger b){
        makePositive(a,b);
        return new EventTypeInteger(a.getValue()/b.getValue());
    }

    @Task(version = 1,retries = 3,timeout = 1000)
    public  EventTypeInteger modulus(EventTypeInteger a , EventTypeInteger b){
        makePositive(a,b);
        return new EventTypeInteger(a.getValue()%b.getValue());
    }

    @Task(version = 1,retries = 3,timeout = 1000)
    public  EventTypeInteger leftShift(EventTypeInteger a){
        return new EventTypeInteger(a.getValue()<<1);
    }

    @Task(version = 1,retries = 3,timeout = 1000)
    public  EventTypeInteger rightShift(EventTypeInteger a ){
        return new EventTypeInteger(a.getValue()>>1);
    }

    @Task(version = 1,retries = 3,timeout = 1000)
    public  EventTypeInteger random(EventTypeInteger a){
        return  new EventTypeInteger((int)(Math.random()*a.getValue()));
    }

    public void makePositive( EventTypeInteger a, EventTypeInteger b){
        if( a.getValue() <= 0)
            a.value *= -1;
        if( b.getValue() <= 0)
            b.value *= -1;
    }

    public void makePositive(EventTypeInteger a){
        if( a.getValue() <= 0)
            a.value *= -1;
    }



}
