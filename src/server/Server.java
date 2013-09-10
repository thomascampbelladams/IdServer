package server;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import client.ServerMessage;


public interface Server extends java.rmi.Remote 
{
	/**
	 * Creates a new user on the server
	 * @param username User's username
	 * @param name User's Real name
	 * @param password User's password
	 * @return The newly created User record
	 * @throws java.rmi.RemoteException
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	User CreateUser(String username, String name, String password) throws RemoteException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException;
	/**
	 * Looks up a user on the server
	 * @param username User's username
	 * @return User's record
	 * @throws java.rmi.RemoteException
	 */
    User Lookup(String username) throws RemoteException;
    /**
     * Looks up a user on the server
     * @param uuid User's UUID
     * @return User's record
     * @throws java.rmi.RemoteException
     */
    User RLookup (String uuid) throws RemoteException;
    /**
     * Modifies a user's login name
     * @param oldloginname User's old login name
     * @param newloginname User's new login name
     * @param password User's password
     * @return
     * @throws java.rmi.RemoteException
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws UnsupportedEncodingException 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    int ModifyUser(String oldloginname, String newloginname, String password) throws RemoteException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException;
    /**
     * Delete's a user from the server
     * @param username User's username
     * @param password User's password
     * @return
     * @throws java.rmi.RemoteException
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws UnsupportedEncodingException 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    int DeleteUser(String username, String password) throws RemoteException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException;
    /**
     * Gets all user records stored on the server
     * @return All user records
     * @throws java.rmi.RemoteException
     */
    Users GetUsers() throws java.rmi.RemoteException;
	/**
	 * Modifies a user's password
	 * @param username User's username
	 * @param oldPassword User's old password
	 * @param newPassword User's new password
	 * @return
	 * @throws java.rmi.RemoteException
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
    int modifyPassword(String username, String oldPassword, String newPassword) throws RemoteException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException;

    void setCallback(ServerMessage sm) throws RemoteException;
    
    void getCoordinator(ServerMessage sm) throws RemoteException;
}

