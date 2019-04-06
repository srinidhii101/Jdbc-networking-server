package com.dal;

import com.dal.exception.NetworkInputException;
import com.dal.models.InputFrame;
import com.dal.models.OutputFrame;
import com.dal.services.CommandTextUtilities;
import com.dal.services.auth.AuthService;

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
                try {
                    checkAuthToken(inputFrame.getHeaders().get("Cookie"));
                } catch (NetworkInputException e) {
                    outputFrame.setProtocol(inputFrame.getProtocol());
                    outputFrame.setStatusCode("401");
                    outputFrame.setStatus("unauthorised");
                    outputFrame.setHeaderValues(new HashMap<>());
                    break;
                }
                outputFrame.setProtocol(inputFrame.getProtocol());
                outputFrame.setStatusCode("200");
                outputFrame.setStatus("ok");
                AUTH_TOKEN = "";
                break;
            }
            default:
                System.out.println("No Case Matched");
        }
        return outputFrame;
    }

    public static void checkAuthToken(String authToken) throws NetworkInputException {
        if (!authToken.equals(AUTH_TOKEN)) {
            throw new NetworkInputException("Incorrect Credentials");
        }
    }
}
