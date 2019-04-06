package com.dal.services.auth;

import com.dal.jdbc.JDBCConnector;
import com.dal.jdbc.JdbcConfig;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuthService implements AuthServiceInterface {

    JdbcConfig jdbcConfig;
    JDBCConnector jdbcConnector;

    public AuthService() {
        this.jdbcConfig = new JdbcConfig();
        this.jdbcConnector = new JDBCConnector();
    }


    @Override
    public boolean authenticate(String lastName, Date birthDate) throws SQLException, ClassNotFoundException {
        Integer isAuth = 0;
        Connection connection = jdbcConnector.connectionProvider(jdbcConfig);
        Statement useDatabaseStatement = connection.createStatement();
        useDatabaseStatement.executeQuery("USE " + jdbcConfig.getDatabase());

        String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(birthDate);

        Statement checkAuthenticationStatement = connection.createStatement();
        ResultSet checkAuthResults = checkAuthenticationStatement.executeQuery("select if(count(*) = 1, 1, 0)\n" +
                "from employees\n" +
                "where employees.LastName=\'" + lastName + "\'\n" +
                "and BirthDate=\'" + dateFormat + "\';");

        ResultSetMetaData resultSetMetaData = checkAuthResults.getMetaData();
        while (checkAuthResults.next()) {
            if (resultSetMetaData.getColumnCount() == 1) {
                Long result = (Long) checkAuthResults.getObject(1);
                isAuth = result.intValue();
            }
        }
        connection.close();
        return isAuth == 1;
    }
}
