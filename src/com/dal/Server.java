package com.dal;

import com.dal.exception.NetworkInputException;
import com.dal.models.*;
import com.dal.services.CommandTextUtilities;
import com.dal.services.auth.AuthService;
import com.dal.services.order.OrderService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.dal.models.OutputFrame.prepareOutputFrame;

public class Server {

    public static String AUTH_TOKEN;
    public static OutputFrame outputFrame = new OutputFrame();
    public static boolean currentOrder;
    public static List<OrderDTO> currentOrderList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = null;

        try {
            System.setProperty("java.net.preferIPv4Stack", "true");

            serverSocket = new ServerSocket(20112, 1);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + 20112);
            System.exit(-1);
        }


        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Accept failed: " + 20112);
                System.exit(-1);
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String tempInput;
            List<String> commandText = new ArrayList<>();
            while (true) {
                while (in.ready()) {
                    tempInput = in.readLine();
                    if (tempInput != null)
                        commandText.add(tempInput);
                }
                if (commandText.size() > 0) {
                    outputFrame = commandRecieved(commandText);
                    out.println(prepareOutputFrame(outputFrame));
                    System.out.println(prepareOutputFrame(outputFrame));
                    commandText.clear();
                    break;
                }
            }
        }
    }

    private static OutputFrame commandRecieved(List<String> commandText) throws NetworkInputException, SQLException, ClassNotFoundException, ParseException {
        CommandTextUtilities commandTextUtilities = new CommandTextUtilities();
        InputFrame inputFrame = commandTextUtilities.splitCommand(commandText);
        switch (inputFrame.getOperation()) {
            case "AUTH": {
                AuthService authService = new AuthService();
                Date password = new SimpleDateFormat("yyyy-MM-dd").parse(inputFrame.getHeaders().get("Password"));
                if (authService.authenticate(inputFrame.getTarget(), password)) {
                    outputFrame.setProtocol(inputFrame.getProtocol());
                    outputFrame.setStatusCode("200");
                    outputFrame.setStatus("ok");
                    outputFrame.setHeaderValues(new HashMap<>());
                    AUTH_TOKEN = commandTextUtilities.generateRandom(10);
                    outputFrame.getHeaderValues().put("Set-Cookie", AUTH_TOKEN);
                } else {
                    outputFrame.setProtocol(inputFrame.getProtocol());
                    outputFrame.setStatusCode("401");
                    outputFrame.setStatus("unauthorised");
                    outputFrame.setHeaderValues(new HashMap<>());
                }
                break;
            }
            case "LOGOUT": {
                if (checkauthentication(inputFrame)) break;
                outputFrame.setProtocol(inputFrame.getProtocol());
                outputFrame.setStatusCode("200");
                outputFrame.setStatus("ok");
                AUTH_TOKEN = "";
                break;
            }
            case "LIST": {
                OrderService orderService = new OrderService();
                if (checkauthentication(inputFrame)) break;
                switch (inputFrame.getTarget().toLowerCase()) {
                    case "customer": {
                        outputFrame.setProtocol(inputFrame.getProtocol());
                        outputFrame.setStatusCode("200");
                        outputFrame.setStatus("ok");
                        outputFrame.setHeaderValues(new HashMap<>());
                        List<CustomerDTO> customerList = orderService.getCustomers();
                        StringBuilder responseBody = new StringBuilder();
                        for (CustomerDTO customerDTO : customerList) {
                            responseBody.append(customerDTO.getCustomerID()).append("\t").append(customerDTO.getCustomerName()).append("\n");
                        }
                        outputFrame.setBody(responseBody.toString());
                        outputFrame.setHeaderValues(new HashMap<>());
                        outputFrame.getHeaderValues().put("Content-Length", Integer.toString(responseBody.toString().getBytes().length));
                        break;
                    }
                    case "product": {
                        outputFrame.setProtocol(inputFrame.getProtocol());
                        outputFrame.setStatusCode("200");
                        outputFrame.setStatus("ok");
                        outputFrame.setHeaderValues(new HashMap<>());
                        List<ProductDTO> productDTOList = orderService.getProducts();
                        StringBuilder responseBody = new StringBuilder();
                        for (ProductDTO productDTO : productDTOList) {
                            responseBody.append(productDTO.getProductID()).append("\t").append(productDTO.getProductName()).append("\n");
                        }
                        outputFrame.setBody(responseBody.toString());
                        outputFrame.setHeaderValues(new HashMap<>());
                        outputFrame.getHeaderValues().put("Content-Length", Integer.toString(responseBody.toString().getBytes().length));
                        break;
                    }
                    case "order": {
                        if (!currentOrder) {
                            outputFrame.setProtocol(inputFrame.getProtocol());
                            outputFrame.setStatusCode("402");
                            outputFrame.setStatus("Order not open");
                            outputFrame.setHeaderValues(new HashMap<>());
                            break;
                        }
                        StringBuilder responseBody = new StringBuilder();
                        for (OrderDTO orderDTO : currentOrderList) {
                            responseBody.append(orderDTO.getProductId()).append("\t").append(orderDTO.getQuantity()).append("\n");
                        }
                        outputFrame.setBody(responseBody.toString());
                        outputFrame.setHeaderValues(new HashMap<>());
                        outputFrame.getHeaderValues().put("Content-Length", Integer.toString(responseBody.toString().getBytes().length));
                        break;
                    }
                    default: {
                        outputFrame.setProtocol(inputFrame.getProtocol());
                        outputFrame.setStatusCode("403");
                        outputFrame.setStatus("Bad Target");
                        outputFrame.setHeaderValues(new HashMap<>());
                        break;
                    }
                }
                break;
            }
            case "NEW": {
                if (checkauthentication(inputFrame)) break;
                String address = inputFrame.getHeaders().get("Address");
                String city = inputFrame.getHeaders().get("City");
                String region = inputFrame.getHeaders().get("Region");
                String postalCode = inputFrame.getHeaders().get("PostalCode");
                String country = inputFrame.getHeaders().get("Country");

                if (address == null && city == null && region == null && postalCode == null && country == null) {
                    outputFrame.setProtocol(inputFrame.getProtocol());
                    outputFrame.setStatusCode("200");
                    outputFrame.setStatus("ok");
                    outputFrame.setHeaderValues(new HashMap<>());
                    currentOrder = true;
                    break;
                } else if (address == null || city == null || region == null || postalCode == null || country == null) {
                    outputFrame.setProtocol(inputFrame.getProtocol());
                    outputFrame.setStatusCode("405");
                    outputFrame.setStatus("Incomplete Address");
                    outputFrame.setHeaderValues(new HashMap<>());
                    break;
                } else {
                    outputFrame.setProtocol(inputFrame.getProtocol());
                    outputFrame.setStatusCode("200");
                    outputFrame.setStatus("ok ");
                    outputFrame.setHeaderValues(new HashMap<>());
                    currentOrder = true;
                    break;
                }
            }
            case "ADD": {
//                if (checkauthentication(inputFrame)) break;
                OrderService orderService = new OrderService();
                if (orderService.getProductStatus(inputFrame.getTarget()) == 1) {
                    outputFrame.setProtocol(inputFrame.getProtocol());
                    outputFrame.setStatusCode("407");
                    outputFrame.setStatus("Discontinued Item");
                    outputFrame.setHeaderValues(new HashMap<>());
                    break;
                } else {
                    outputFrame.setProtocol(inputFrame.getProtocol());
                    outputFrame.setStatusCode("200");
                    outputFrame.setStatus("ok ");
                    outputFrame.setHeaderValues(new HashMap<>());
                    OrderDTO orderDTO = new OrderDTO();
                    orderDTO.setProductId(inputFrame.getTarget());
                    orderDTO.setQuantity(inputFrame.getBody());
                    currentOrderList.add(orderDTO);
                    break;
                }
            }
            default:
                outputFrame.setProtocol(inputFrame.getProtocol());
                outputFrame.setStatusCode("403");
                outputFrame.setStatus("Bad Target");
                outputFrame.setHeaderValues(new HashMap<>());
                break;
        }
        return outputFrame;
    }

    private static boolean checkauthentication(InputFrame inputFrame) {
        try {
            checkAuthToken(inputFrame.getHeaders().get("Cookie"));
        } catch (NetworkInputException e) {
            outputFrame.setProtocol(inputFrame.getProtocol());
            outputFrame.setStatusCode("401");
            outputFrame.setStatus("unauthorised");
            outputFrame.setHeaderValues(new HashMap<>());
            return true;
        }
        return false;
    }

    public static void checkAuthToken(String authToken) throws NetworkInputException {
        if (!authToken.equals(AUTH_TOKEN)) {
            throw new NetworkInputException("Incorrect Credentials");
        }
    }
}
