package StackelbergAgent;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;

public final class Main
{
    public static void main(String[] args)
    {
	try
	{
	    new SupremeLeader();
	}
	catch(RemoteException | NotBoundException e)
	{
	    System.out.println("Couldn't even start " + e);
	}
    }
}