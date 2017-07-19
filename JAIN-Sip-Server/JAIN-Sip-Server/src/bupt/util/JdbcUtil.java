package bupt.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;

//jdbc工具类
public class JdbcUtil {
	//定义四个变量 来接收资源文件中的数据
	private static String driver = "";
	private static String url = "";
	private static String username = "";
	private static String password = "";
	//定义一个静态代码块 用来只执行一次加载资源文件的操作
	static {
		try {
			//1.创建一个资源文件对象
			Properties p = new Properties();
			//2.加载需要解析的资源文件
			p.load(JdbcUtil.class.getClassLoader().getResourceAsStream("jdbc.properties"));
			//获取资源文件中的数据
			driver = p.getProperty("driver");
			url = p.getProperty("url");
			username = p.getProperty("username");
			password = p.getProperty("password");
			//加载驱动程序
			Class.forName(driver);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 *功能: 获取数据库连接
	 *时间: 2017年5月31日
	 */
	private static Connection getConnection(){
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	/**
	 *功能: 修改查询
	 *时间: 2017年5月31日
	 */
	public static Object findById(String sql,Class<?> clazz){
		System.out.println("sql="+sql);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet re = null;
		Object obj = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			re = ps.executeQuery();
			//获取所有数据
			ResultSetMetaData data = re.getMetaData();
			//获取查询到的总列数
			int count = data.getColumnCount();
			if(re.next()){
				//通过反射实例化对象
				obj = clazz.newInstance();
				for(int i=1;i<=count;i++){
					BeanUtils.copyProperty(obj, data.getColumnName(i), re.getObject(i));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			close(conn, ps, re);
		}
		return obj;
	}
	/** 
	 *功能: 返回生成的主键
	 *时间: 2017年5月31日
	 */
	public static int getGeneratedKeys(String sql){
		System.out.println("sql="+sql);
		Connection conn = null;
		PreparedStatement ps = null;
		int key = 0;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			int num = ps.executeUpdate();
			if(num>0){
				//获取生成主键的set集合
				ResultSet set = ps.getGeneratedKeys();
				if(set.next()){
					key = set.getInt(1);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			close(conn, ps, null);
		}
		return key;
	}
	/**
	 *功能: 增删改
	 *时间: 2017年5月31日
	 */
	public static int execute(String sql){
		System.out.println("sql="+sql);
		int n = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			n = ps.executeUpdate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			close(conn, ps, null);
		}
		return n;
	}
	/**
	 *功能: 列表查询
	 *时间: 2017年5月31日
	 */
	@SuppressWarnings("unchecked")
	public static List<?> getList(String sql,String columnName){
		System.out.println("sql="+sql);
		//获取数据库连接
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet re = null;
		List list = new ArrayList();
		try {
			conn = getConnection();
			//创建sql预编译器
			ps = conn.prepareStatement(sql);
			//执行sql语句
			re = ps.executeQuery();
			//可用于获取关于 ResultSet 对象中列的类型和属性信息的对象
			ResultSetMetaData data = re.getMetaData();
			int count = data.getColumnCount();
			while(re.next()){
				//Object obj = clazz.newInstance();
				//for(int i=1;i<=count;i++){
					//BeanUtils.copyProperty(obj, data.getColumnName(i),re.getObject(i));
				    
				//}
				list.add(re.getString(columnName));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			close(conn, ps, re);
		}
		return list;
	} 
	/**
	 *功能: 关闭资源
	 *时间: 2017年5月31日
	 */
	private static void close(Connection conn,PreparedStatement ps,ResultSet re){
			try {
				if(conn!=null)
					conn.close();
				if(ps!=null)
					ps.close();
				if(re!=null)
					re.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}

