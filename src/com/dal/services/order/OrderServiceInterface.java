package com.dal.services.order;

import com.dal.models.CustomerDTO;
import com.dal.models.ProductDTO;

import java.sql.SQLException;
import java.util.List;

public interface OrderServiceInterface {

    List<ProductDTO> getProducts() throws SQLException, ClassNotFoundException;

    List<CustomerDTO> getCustomers() throws SQLException, ClassNotFoundException;
}
