package com.dal.services.order;

import com.dal.jdbc.JDBCConnector;
import com.dal.jdbc.JdbcConfig;
import com.dal.models.CustomerDTO;
import com.dal.models.ProductDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderService implements OrderServiceInterface {

    JdbcConfig jdbcConfig;
    JDBCConnector jdbcConnector;

    public OrderService() {
        this.jdbcConfig = new JdbcConfig();
        this.jdbcConnector = new JDBCConnector();
    }


    @Override
    public List<ProductDTO> getProducts() throws SQLException, ClassNotFoundException {
        List<ProductDTO> listOfProducts = new ArrayList<>();
        Connection connection = jdbcConnector.connectionProvider(jdbcConfig);
        Statement useDatabaseStatement = connection.createStatement();
        useDatabaseStatement.executeQuery("USE " + jdbcConfig.getDatabase());

        Statement listOfProductsStatement = connection.createStatement();
        ResultSet productsResults = listOfProductsStatement.executeQuery("select productID, productName\n" +
                "from products;\n");

        while (productsResults.next()) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductID(productsResults.getString(1));
            productDTO.setProductName(productsResults.getString(2));
            listOfProducts.add(productDTO);
        }
        connection.close();
        return listOfProducts;
    }

    @Override
    public List<CustomerDTO> getCustomers() throws SQLException, ClassNotFoundException {
        List<CustomerDTO> listOfCustomers = new ArrayList<>();
        Connection connection = jdbcConnector.connectionProvider(jdbcConfig);
        Statement useDatabaseStatement = connection.createStatement();
        useDatabaseStatement.executeQuery("USE " + jdbcConfig.getDatabase());

        Statement listOfCustomersStatement = connection.createStatement();
        ResultSet customerResults = listOfCustomersStatement.executeQuery("select customerID, contactName\n" +
                "from customers;");

        while (customerResults.next()) {
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setCustomerID(customerResults.getString(1));
            customerDTO.setCustomerName(customerResults.getString(2));
            listOfCustomers.add(customerDTO);
        }
        connection.close();
        return listOfCustomers;
    }

    @Override
    public Integer getProductStatus(String productId) throws SQLException, ClassNotFoundException {
        int isDiscontinued = 0;
        Connection connection = jdbcConnector.connectionProvider(jdbcConfig);
        Statement useDatabaseStatement = connection.createStatement();
        useDatabaseStatement.executeQuery("USE " + jdbcConfig.getDatabase());

        Statement productStatusStatement = connection.createStatement();
        ResultSet productStatusResults = productStatusStatement.executeQuery("select products.productID, IF(class_3901.products.discontinued = 1, 1, 0)\n" +
                "from products\n" +
                "where products.productID=\'" + productId + "\';");

        ResultSetMetaData resultSetMetaData = productStatusResults.getMetaData();
        while (productStatusResults.next()) {
            if (resultSetMetaData.getColumnCount() == 2) {
                Long result = (Long) productStatusResults.getObject(2);
                isDiscontinued = result.intValue();
            }
        }
        connection.close();
        return isDiscontinued;
    }
}
