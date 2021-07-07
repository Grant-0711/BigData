import org.hxl.Mybatis.Employee;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Grant
 * @create 2021-07-08 4:29
 */
public class test {
    private DataSource dataSource;
    private Employee employee;
    public Employee getEmployeeById(long id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select id ,firstname ,lastname from employee where id=?";
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();

            if (rs.next()){
                employee = new Employee();
                employee.setId(rs.getLong("id"));
            }
        }catch (SQLException e){
            e.printStackTrace();;
        }finally {
            if (rs != null){
                try{rs.close();} catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return employee;
    }
}
