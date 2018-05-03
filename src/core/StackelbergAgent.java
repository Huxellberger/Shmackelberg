// package StackelbergAgent;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;

public final class StackelbergAgent
{
    public static void main(String[] args)
    {
	try
	{
	    new SimpleLeader();
	}
	catch(RemoteException | NotBoundException e)
	{
	    System.out.println("Couldn't even start " + e);
	}
    }
}
