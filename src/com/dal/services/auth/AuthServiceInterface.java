package com.dal.services.auth;

import java.sql.SQLException;
import java.util.Date;

public interface AuthServiceInterface {

    boolean authenticate(String lastName, Date birthDate) throws SQLException, ClassNotFoundException;
}
