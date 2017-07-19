package bupt.dao;

import java.awt.List;
import java.util.ArrayList;

import org.omg.CORBA.UnionMember;

import bupt.bean.User;
import bupt.util.JdbcUtil;

public class BaseDao {

	public static boolean Register(String user, String password) {
		String sql = "insert into user(Username,Password)  values('" + user + "','" + password + "')";
		int i = JdbcUtil.execute(sql);
		if (i == 1) {
			return true;
		}
		return false;
	}

	public static boolean checkExist(String user) {

		String sql="select idUser,Username,Password from user where Username='"+user+"'";
        User u=(User) JdbcUtil.findById(sql, User.class);
        
		if(u!=null){
			return true;
		}
		return false;
	}

	public static boolean checkPasswordMatch(String user, String password) {
		
		String sql="select idUser,Username,Password from user where Username='"+user+"' and password='"+password+"'";
        User u=(User) JdbcUtil.findById(sql, User.class);
        
		if(u!=null){
			return true;
		}
		return false;
		 
	}
	
	public static synchronized ArrayList<String> getFriend(String Username){
		ArrayList<String> friendName= new ArrayList<String>();
		  String sql = "select Friendname from friend WHERE Username = '" + Username +"' UNION select Username from friend WHERE Friendname =  '" + Username +"'";
			friendName = (ArrayList<String>) JdbcUtil.getList(sql,"Friendname");       
		return friendName;
	}
	
	 public static synchronized boolean isFriend(String userName1, String userName2){
		 ArrayList<String> friendName= new ArrayList<String>();
		 friendName=getFriend(userName1);
		  while (friendName.listIterator().hasNext()){
              if (userName2.equals(friendName.listIterator().next())){
                  return true;
              }
          }
		  return false;
		
	 }
	 
		
	 public static synchronized boolean createFriend(String userName1, String userName2){
		if(BaseDao.isFriend(userName1, userName2)){
			return false;
		}
		else{
			 String sql = "insert into friend values('" + userName1 + "','" + userName2 + "')";
		     int i = JdbcUtil.execute(sql);
		     if(i==1){
		     return true;
		     }
		     else{
		    	 return false;
		     }
		}
		 
	 }
	
	 public void deleteFriend(String sourceName, String targetName) {
		 String sql = "delete from friend WHERE Username = '" + sourceName +"' and Friendname = '" + targetName + "'";
         JdbcUtil.execute(sql);
         sql="delete from friend WHERE Username = '" + targetName +"' and Friendname = '" + sourceName + "'";
         JdbcUtil.execute(sql);
	 }
		    


}
